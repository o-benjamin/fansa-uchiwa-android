import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { FunctionsErrorCode } from "firebase-functions/v2/https";
import type { Firestore } from "firebase-admin/firestore";
import type { GeminiClient, SourceImage } from "./gemini";
import type { JudgeCategory } from "./moderation";
import { findBlockedName } from "./moderation";
import type { Reservation } from "./quota";
import { refundCredit, reserveCredit } from "./quota";
import type { AiMaterialSettings } from "./settings";

export const MAX_PROMPT_LENGTH = 200;
export const MAX_SOURCE_IMAGE_BYTES = 4 * 1024 * 1024;
const SOURCE_IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

/**
 * 断ったときに `HttpsError` の `details.reason` に入れる理由。アプリはこの値で出す文言を選び、
 * GA4 の `generate_ai_material` の「断った理由」にもこの値を送る。
 */
export type RejectReason =
  | "unauthenticated"
  | "invalid_prompt"
  | "invalid_image"
  | "disabled"
  | "blocked_name"
  | "moderation"
  | "no_credits"
  | "free_daily_limit"
  | "total_daily_limit"
  | "safety_blocked"
  | "generation_failed";

const ERROR_CODES: Record<RejectReason, FunctionsErrorCode> = {
  unauthenticated: "unauthenticated",
  invalid_prompt: "invalid-argument",
  invalid_image: "invalid-argument",
  disabled: "failed-precondition",
  blocked_name: "permission-denied",
  moderation: "permission-denied",
  no_credits: "resource-exhausted",
  free_daily_limit: "resource-exhausted",
  total_daily_limit: "resource-exhausted",
  safety_blocked: "permission-denied",
  generation_failed: "unavailable",
};

export interface GenerateMaterialRequest {
  prompt: string;
  sourceImage?: SourceImage;
}

export interface GenerateMaterialResponse {
  /** base64 の画像。背景は単色なので、アプリが ML Kit で切り抜いてから置く */
  image: string;
  mimeType: string;
  /** 今回使ったのが無料の1回か回数券か */
  paidWith: "free" | "ticket";
  /** 今回の分を引いたあとの、使える回数券の残り */
  ticketsRemaining: number;
}

export interface GenerateMaterialDeps {
  db: Firestore;
  isEntryOpen: () => Promise<boolean>;
  loadSettings: () => Promise<AiMaterialSettings>;
  gemini: GeminiClient;
  /** オーナーへの知らせ（Discord）。失敗しても生成の結果には響かせない */
  notifyOwner: (message: string) => Promise<void>;
  now: () => Date;
}

function reject(reason: RejectReason, extra: { category?: JudgeCategory } = {}): never {
  // ログに頼んだ文章は書かない（回数と結果の種類だけ）
  logger.info("generateMaterial", { result: reason, ...extra });
  throw new HttpsError(ERROR_CODES[reason], reason, { reason, ...extra });
}

/** アプリから届いた中身を確かめる。文章は前後の空白を除いて1〜200文字、元画像は任意で4MBまで。 */
export function parseRequest(data: unknown): GenerateMaterialRequest {
  const body = (data ?? {}) as { prompt?: unknown; sourceImage?: unknown };
  const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
  if (prompt.length === 0 || [...prompt].length > MAX_PROMPT_LENGTH) reject("invalid_prompt");
  if (body.sourceImage === undefined || body.sourceImage === null) return { prompt };

  const image = body.sourceImage as { mimeType?: unknown; data?: unknown };
  const isValid =
    typeof image.mimeType === "string" &&
    SOURCE_IMAGE_TYPES.has(image.mimeType) &&
    typeof image.data === "string" &&
    image.data.length > 0 &&
    Buffer.byteLength(image.data, "base64") <= MAX_SOURCE_IMAGE_BYTES;
  if (!isValid) reject("invalid_image");
  return { prompt, sourceImage: { mimeType: image.mimeType as string, data: image.data as string } };
}

/**
 * AI 素材を1枚作る。順番：ログインの確認 → 中身の確認 → 入口スイッチ → 名前の一覧 → 1回分おさえる →
 * 文章の審査 → 画像の生成。おさえたあとに断った・失敗したときは、回数を戻してから断る。
 * App Check の確認は `onCall` の `enforceAppCheck` が先に行う（index.ts）。
 */
export async function handleGenerateMaterial(
  uid: string | undefined,
  data: unknown,
  deps: GenerateMaterialDeps,
): Promise<GenerateMaterialResponse> {
  if (!uid) reject("unauthenticated");
  const request = parseRequest(data);
  if (!(await deps.isEntryOpen())) reject("disabled");

  const settings = await deps.loadSettings();
  if (findBlockedName(request.prompt, settings.blockedNames)) reject("blocked_name");

  const outcome = await reserveCredit(deps.db, uid, settings, deps.now());
  if (!outcome.ok) {
    if (outcome.firstTotalLimitHit) {
      await deps
        .notifyOwner(
          `🚨 AI素材：全体の1日の上限（${settings.totalDailyLimit}回）に当たったので、今日の生成を止めた。` +
            "不具合か利用の急増かを確かめる（上限は Firestore の config/aiMaterial.totalDailyLimit）",
        )
        .catch((error) => logger.error("Discord に知らせられなかった", { error: String(error) }));
    }
    reject(outcome.reason);
  }

  const { reservation } = outcome;
  const startedAt = Date.now();
  try {
    const category = await deps.gemini.judge(settings.judgeModel, request.prompt);
    if (category !== "none") {
      await refund(deps.db, uid, reservation);
      reject("moderation", { category });
    }

    const result = await deps.gemini.generateImage(settings.imageModel, request.prompt, request.sourceImage);
    if (result.kind !== "image") {
      await refund(deps.db, uid, reservation);
      reject(result.kind === "safety_blocked" ? "safety_blocked" : "generation_failed");
    }

    logger.info("generateMaterial", {
      result: "ok",
      paidWith: reservation.kind,
      model: settings.imageModel,
      elapsedMs: Date.now() - startedAt,
    });
    return {
      image: result.data,
      mimeType: result.mimeType,
      paidWith: reservation.kind,
      ticketsRemaining: outcome.ticketsRemaining,
    };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    logger.error("画像を作れなかった", { error: String(error), model: settings.imageModel });
    await refund(deps.db, uid, reservation);
    reject("generation_failed");
  }
}

/** 戻せなかったときはログに残す（利用者には生成の失敗として返す。回数は手で戻す）。 */
async function refund(db: Firestore, uid: string, reservation: Reservation): Promise<void> {
  try {
    await refundCredit(db, uid, reservation);
  } catch (error) {
    logger.error("回数を戻せなかった", { uid, reservation, error: String(error) });
  }
}
