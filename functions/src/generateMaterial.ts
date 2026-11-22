import { randomUUID } from "node:crypto";
import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { FunctionsErrorCode } from "firebase-functions/v2/https";
import type { Firestore } from "firebase-admin/firestore";
import type { GeminiClient, ImageResult, JudgeResult, SourceImage } from "./gemini";
import type { JudgeCategory } from "./moderation";
import { findBlockedName } from "./moderation";
import type { RequestOutcome, RequestState, Reservation } from "./quota";
import { refundCredit, reserveCredit, reserveCreditForRequest, updateRequestState } from "./quota";
import type { AiMaterialSettings } from "./settings";

export const MAX_PROMPT_LENGTH = 200;
export const MAX_SOURCE_IMAGE_BYTES = 4 * 1024 * 1024;
const SOURCE_IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);
// アプリは UUID を送る。形が違う番号は使わない（作り直しなしで、ふつうに回数を使う）
const REQUEST_ID_PATTERN = /^[A-Za-z0-9-]{1,64}$/;

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
  | "generation_failed"
  | "in_progress";

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
  in_progress: "aborted",
};

export interface GenerateMaterialRequest {
  prompt: string;
  sourceImage?: SourceImage;
  /**
   * 頼みの番号（#387）。アプリが「作る」で作り、素材を受け取るまで同じ番号で送り直す。
   * 同じ番号の前の頼みで回数を使っていれば、回数を使わずに作り直す（quota.ts の reserveCreditForRequest）。古いアプリは送らない
   */
  requestId?: string;
}

export interface GenerateMaterialResponse {
  /** base64 の画像。背景は単色なので、アプリが ML Kit で切り抜いてから置く */
  image: string;
  mimeType: string;
  /** 今回使ったのが無料の1回か回数券か */
  paidWith: "free" | "ticket";
  /** 今回の分を引いたあとの、使える回数券の残り */
  ticketsRemaining: number;
  /** 画像を作ったモデル。利用者が報告するとき、アプリがこの値を reportMaterial に返す */
  model: string;
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
  const body = (data ?? {}) as { prompt?: unknown; sourceImage?: unknown; requestId?: unknown };
  const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
  // 文字数は見た目の文字で数える（絵文字などのサロゲートペアも1文字）
  if (prompt.length === 0 || [...prompt].length > MAX_PROMPT_LENGTH) refuse("invalid_prompt");
  const requestId = parseRequestId(body.requestId);
  const withRequestId = requestId === undefined ? {} : { requestId };
  if (body.sourceImage === undefined || body.sourceImage === null) return { prompt, ...withRequestId };

  const image = body.sourceImage as { mimeType?: unknown; data?: unknown };
  const isValid =
    typeof image.mimeType === "string" &&
    SOURCE_IMAGE_TYPES.has(image.mimeType) &&
    typeof image.data === "string" &&
    image.data.length > 0 &&
    Buffer.byteLength(image.data, "base64") <= MAX_SOURCE_IMAGE_BYTES;
  if (!isValid) refuse("invalid_image");
  return {
    prompt,
    sourceImage: { mimeType: image.mimeType as string, data: image.data as string },
    ...withRequestId,
  };
}

function parseRequestId(value: unknown): string | undefined {
  if (value === undefined || value === null) return undefined;
  if (typeof value === "string" && REQUEST_ID_PATTERN.test(value)) return value;
  logger.warn("頼みの番号の形が違うので、作り直しには使わない");
  return undefined;
}

/**
 * AI 素材を1枚作る。順番：ログインの確認 → 中身の確認 → 入口スイッチ → 名前の一覧 → 1回分おさえる →
 * 審査 → 画像の生成。おさえたあとに断った・失敗したときは、回数を戻してから断る。
 * 頼みの番号つきで、同じ番号の前の頼みで回数を使って作れていたら、回数を使わずに作り直す（#387）。
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

  const ids: CallIds = {
    uid,
    callId: randomUUID(),
    ...(request.requestId === undefined ? {} : { requestId: request.requestId }),
  };
  const outcome: RequestOutcome = await refuseIfThrows(
    request.requestId === undefined
      ? reserveCredit(deps.db, uid, settings, deps.now())
      : reserveCreditForRequest(deps.db, uid, settings, deps.now(), request.requestId),
    "回数をおさえられなかった",
    ids,
  );
  if (!outcome.ok) {
    if (outcome.firstTotalLimitHit) await notifyTotalLimitHit(deps, settings.totalDailyLimit);
    refuse(outcome.reason);
  }
  if ("redo" in outcome) return redo(deps, settings, request, ids, outcome);
  logger.info("回数をおさえた", { ...ids, reservation: outcome.reservation });

  const startedAt = Date.now();
  const sticker = await createSticker(deps.gemini, settings, request);
  if (!sticker.ok) {
    await refund(deps.db, ids, outcome.reservation);
    refuse(sticker.reason, sticker.details);
  }
  await moveRequest(deps, ids, "generating", "done");

  logger.info("generateMaterial", {
    result: "ok",
    ...ids,
    reservation: outcome.reservation,
    model: settings.imageModel,
    elapsedMs: Date.now() - startedAt,
  });
  return {
    image: sticker.image,
    mimeType: sticker.mimeType,
    paidWith: outcome.reservation.kind,
    ticketsRemaining: outcome.ticketsRemaining,
    model: settings.imageModel,
  };
}

/**
 * 回数を使わずに作り直す（#387）。失敗したら、時間内ならもう一度作り直せるよう done に戻す（回数は動かしていないので戻さない）。
 */
async function redo(
  deps: GenerateMaterialDeps,
  settings: AiMaterialSettings,
  request: GenerateMaterialRequest,
  ids: CallIds,
  outcome: Extract<RequestOutcome, { redo: true }>,
): Promise<GenerateMaterialResponse> {
  logger.info("回数を使わずに作り直す", { ...ids, paidWith: outcome.paidWith });

  const startedAt = Date.now();
  const sticker = await createSticker(deps.gemini, settings, request);
  if (!sticker.ok) {
    await moveRequest(deps, ids, "redoing", "done");
    refuse(sticker.reason, sticker.details);
  }
  await moveRequest(deps, ids, "redoing", "redone");

  logger.info("generateMaterial", {
    result: "ok",
    ...ids,
    redo: true,
    paidWith: outcome.paidWith,
    model: settings.imageModel,
    elapsedMs: Date.now() - startedAt,
  });
  return {
    image: sticker.image,
    mimeType: sticker.mimeType,
    paidWith: outcome.paidWith,
    ticketsRemaining: outcome.ticketsRemaining,
    model: settings.imageModel,
  };
}

/**
 * 回数を動かすログ（おさえた・おさえられなかった・戻した・戻さなかった・戻せなかった・作れた）に書く ID。
 * 「作れたか確かめられませんでした」と出た人から連絡が来たとき、その uid の回数が減ったままかをログで確かめるため。
 * `reservation` は1回ごとに違う値ではない（同じ日に同じ回数券で作ると同じ）ので、1回の呼び出しのログは `callId` で組にする
 * （README の generateMaterial の「困ったとき（Cloud Logging で探す）」）。
 */
interface CallIds {
  uid: string;
  callId: string;
  /** 頼みの番号（アプリが送ったときだけ）。作り直した呼び出しを、前の呼び出しと組にするため */
  requestId?: string;
}

/**
 * Firestore を読み書きできなかった（一時的な障害など）ときも、アプリが文言を選べるよう `generation_failed` で断る。
 * 回数をおさえる前と、おさえるときに使う。おさえるときに投げても書き込めていることがまれにあるので、
 * そのときは `logFields` に uid と callId を渡してログに残す。
 */
async function refuseIfThrows<T>(work: Promise<T>, message: string, logFields: Partial<CallIds> = {}): Promise<T> {
  try {
    return await work;
  } catch (error) {
    logger.error(message, { ...logFields, error: String(error) });
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
 * 戻したか・戻さなかったか・戻せなかったかをログに残す（利用者には断った理由のまま返す）。
 * uid・callId・reservation は、問い合わせのときに戻したかを確かめ、戻せなかった分を手で戻すために書く
 * （README の generateMaterial の「困ったとき（Cloud Logging で探す）」）。
 */
async function refund(db: Firestore, ids: CallIds, reservation: Reservation): Promise<void> {
  try {
    const restored = await refundCredit(db, ids.uid, reservation, ids.requestId);
    logger.info(restored ? "回数を戻した" : "回数券が消えていて戻さなかった", { ...ids, reservation });
  } catch (error) {
    logger.error("回数を戻せなかった", { ...ids, reservation, error: String(error) });
  }
}

/**
 * 頼みの状態を進める。書けなくても利用者への応答は変えない（generating・redoing のまま残っても、
 * STALE_CALL_MS たてば止まった呼び出しとして扱われ、時間内なら作り直せる）。
 */
async function moveRequest(deps: GenerateMaterialDeps, ids: CallIds, from: RequestState, to: RequestState): Promise<void> {
  if (ids.requestId === undefined) return;
  try {
    await updateRequestState(deps.db, ids.uid, ids.requestId, from, to, deps.now());
  } catch (error) {
    logger.error("頼みの状態を書けなかった", { ...ids, from, to, error: String(error) });
  }
}
