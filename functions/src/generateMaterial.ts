import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { FunctionsErrorCode } from "firebase-functions/v2/https";
import type { Firestore } from "firebase-admin/firestore";
import type { GeminiClient, ImageResult, JudgeResult, SourceImage } from "./gemini";
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

/** 断る。アプリが文言を選べるよう、理由を `details.reason` に入れて投げる。 */
function refuse(reason: RejectReason, details: { category?: JudgeCategory } = {}): never {
  // ログに頼んだ文章は書かない（回数と結果の種類だけ）
  logger.info("generateMaterial", { result: reason, ...details });
  throw new HttpsError(ERROR_CODES[reason], reason, { reason, ...details });
}

/** アプリから届いた中身を確かめる。文章は前後の空白を除いて1〜200文字、元画像は任意で4MBまで。 */
export function parseRequest(data: unknown): GenerateMaterialRequest {
  const body = (data ?? {}) as { prompt?: unknown; sourceImage?: unknown };
  const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
  // 文字数は見た目の文字で数える（絵文字などのサロゲートペアも1文字）
  if (prompt.length === 0 || [...prompt].length > MAX_PROMPT_LENGTH) refuse("invalid_prompt");
  if (body.sourceImage === undefined || body.sourceImage === null) return { prompt };

  const image = body.sourceImage as { mimeType?: unknown; data?: unknown };
  const isValid =
    typeof image.mimeType === "string" &&
    SOURCE_IMAGE_TYPES.has(image.mimeType) &&
    typeof image.data === "string" &&
    image.data.length > 0 &&
    Buffer.byteLength(image.data, "base64") <= MAX_SOURCE_IMAGE_BYTES;
  if (!isValid) refuse("invalid_image");
  return { prompt, sourceImage: { mimeType: image.mimeType as string, data: image.data as string } };
}

/**
 * AI 素材を1枚作る。順番：ログインの確認 → 中身の確認 → 入口スイッチ → 名前の一覧 → 1回分おさえる →
 * 審査 → 画像の生成。おさえたあとに断った・失敗したときは、回数を戻してから断る。
 * App Check の確認は `onCall` の `enforceAppCheck` が先に行う（index.ts）。
 */
export async function handleGenerateMaterial(
  uid: string | undefined,
  data: unknown,
  deps: GenerateMaterialDeps,
): Promise<GenerateMaterialResponse> {
  if (!uid) refuse("unauthenticated");
  const request = parseRequest(data);
  if (!(await deps.isEntryOpen())) refuse("disabled");

  const settings = await refuseIfThrows(deps.loadSettings(), "設定を読めなかった");
  if (findBlockedName(request.prompt, settings.blockedNames)) refuse("blocked_name");

  const outcome = await refuseIfThrows(reserveCredit(deps.db, uid, settings, deps.now()), "回数をおさえられなかった");
  if (!outcome.ok) {
    if (outcome.firstTotalLimitHit) await notifyTotalLimitHit(deps, settings.totalDailyLimit);
    refuse(outcome.reason);
  }

  const startedAt = Date.now();
  const sticker = await createSticker(deps.gemini, settings, request);
  if (!sticker.ok) {
    await refund(deps.db, uid, outcome.reservation);
    refuse(sticker.reason, sticker.details);
  }

  logger.info("generateMaterial", {
    result: "ok",
    paidWith: outcome.reservation.kind,
    model: settings.imageModel,
    elapsedMs: Date.now() - startedAt,
  });
  return {
    image: sticker.image,
    mimeType: sticker.mimeType,
    paidWith: outcome.reservation.kind,
    ticketsRemaining: outcome.ticketsRemaining,
  };
}

/**
 * Firestore を読み書きできなかった（一時的な障害など）ときも、アプリが文言を選べるよう `generation_failed` で断る。
 * 回数をおさえる前に使うので、戻すものはない。
 */
async function refuseIfThrows<T>(work: Promise<T>, message: string): Promise<T> {
  try {
    return await work;
  } catch (error) {
    logger.error(message, { error: String(error) });
    refuse("generation_failed");
  }
}

async function notifyTotalLimitHit(deps: GenerateMaterialDeps, totalDailyLimit: number): Promise<void> {
  await deps
    .notifyOwner(
      `🚨 AI素材：全体の1日の上限（${totalDailyLimit}回）に当たったので、今日の生成を止めた。` +
        "不具合か利用の急増かを確かめる（上限は Firestore の config/aiMaterial.totalDailyLimit）",
    )
    .catch((error) => logger.error("Discord に知らせられなかった", { error: String(error) }));
}

type StickerResult =
  | { ok: true; image: string; mimeType: string }
  | { ok: false; reason: "moderation" | "safety_blocked" | "generation_failed"; details?: { category: JudgeCategory } };

/**
 * 審査してから画像を作る。断る・失敗するときも例外を投げずに理由を返す（呼ぶ側が回数を1か所で戻せるように）。
 * ログには、どの段階（審査か画像か）で、どのモデルが、なぜ失敗したかを残す。
 */
async function createSticker(
  gemini: GeminiClient,
  settings: AiMaterialSettings,
  request: GenerateMaterialRequest,
): Promise<StickerResult> {
  let verdict: JudgeResult;
  try {
    verdict = await gemini.judge(settings.judgeModel, request.prompt, request.sourceImage);
  } catch (error) {
    logger.error("審査のモデルを呼べなかった", { model: settings.judgeModel, error: String(error) });
    return { ok: false, reason: "generation_failed" };
  }
  if (verdict === undefined) {
    // 利用者の文章のせいではないので、moderation ではなく generation_failed（やり直せる）にする
    logger.warn("審査の答えを読めなかった", { model: settings.judgeModel });
    return { ok: false, reason: "generation_failed" };
  }
  if (verdict === "safety_blocked") return { ok: false, reason: "safety_blocked" };
  if (verdict !== "none") return { ok: false, reason: "moderation", details: { category: verdict } };

  let result: ImageResult;
  try {
    result = await gemini.generateImage(settings.imageModel, request.prompt, request.sourceImage);
  } catch (error) {
    logger.error("画像のモデルを呼べなかった", { model: settings.imageModel, error: String(error) });
    return { ok: false, reason: "generation_failed" };
  }
  if (result.kind === "safety_blocked") {
    logger.info("画像が安全フィルターで止まった", { model: settings.imageModel, finishReason: result.finishReason });
    return { ok: false, reason: "safety_blocked" };
  }
  if (result.kind === "no_image") {
    logger.warn("画像が返らなかった", { model: settings.imageModel, finishReason: result.finishReason });
    return { ok: false, reason: "generation_failed" };
  }
  return { ok: true, image: result.data, mimeType: result.mimeType };
}

/**
 * 戻せなかったときはログに残す（利用者には断った理由のまま返す）。
 * 手で戻す手順は README の「困ったとき」。uid と reservation はそのために書く。
 */
async function refund(db: Firestore, uid: string, reservation: Reservation): Promise<void> {
  try {
    await refundCredit(db, uid, reservation);
  } catch (error) {
    logger.error("回数を戻せなかった", { uid, reservation, error: String(error) });
  }
}
