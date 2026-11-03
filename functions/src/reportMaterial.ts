import { Timestamp } from "firebase-admin/firestore";
import type { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { FunctionsErrorCode } from "firebase-functions/v2/https";
import { MAX_PROMPT_LENGTH } from "./generateMaterial";
import { jstDateKey } from "./jstDate";

/*
 * 報告の持ち方（書けるのはサーバーだけ。firestore.rules でアプリからは読み書きできない）
 *
 * reports/{自動の ID}
 *   uid: string          報告した人の匿名ログインの ID（同じ人の報告をまとめて見るため）
 *   prompt: string       素材を頼んだ文章（報告の前に「文章と理由を送る」と見せて同意を取る。画像は送らない）
 *   reason: ReportReason 報告の理由
 *   model: string        素材を作ったモデル（generateMaterial の応答の model をアプリが返す）
 *   createdAt: Timestamp
 *   expireAt: Timestamp  これを過ぎたら消す（Firestore の TTL ポリシー。functions/README.md）
 *
 * reportUsage/{uid}
 *   dateKey: string      日本時間の日付
 *   count: number        その日に送った報告の数（1人1日の上限を数える）
 */

export const REPORTS_COLLECTION = "reports";
export const REPORT_USAGE_COLLECTION = "reportUsage";

/** 報告の理由。アプリの選択肢と同じ値（`AiMaterialReportReason`）。 */
export const REPORT_REASONS = ["inappropriate", "real_person", "rights", "other"] as const;
export type ReportReason = (typeof REPORT_REASONS)[number];

const REASON_LABELS: Record<ReportReason, string> = {
  inappropriate: "不快・不適切",
  real_person: "実在の人に似ている",
  rights: "ほかの作品やキャラクターに似ている",
  other: "その他",
};

/** 1人1日の報告の上限。不具合や連打で Discord への知らせが続かないように抑える。 */
export const MAX_REPORTS_PER_DAY = 10;
/** 報告を残す日数。頼んだ文章が入るので、見直しに要る間だけ残す。 */
export const REPORT_RETENTION_DAYS = 180;
const MAX_MODEL_LENGTH = 100;
const MODEL_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._-]*$/;
const DAY_MS = 24 * 60 * 60 * 1000;

export interface ReportMaterialRequest {
  prompt: string;
  reason: ReportReason;
  model: string;
}

export interface ReportMaterialDeps {
  db: Firestore;
  /** オーナーへの知らせ（Discord）。失敗しても報告の受け付けには響かせない */
  notifyOwner: (message: string) => Promise<void>;
  now: () => Date;
}

/** 断ったときに `HttpsError` の `details.reason` に入れる理由。 */
export type ReportRejectReason = "unauthenticated" | "invalid_report" | "too_many_reports";

const ERROR_CODES: Record<ReportRejectReason, FunctionsErrorCode> = {
  unauthenticated: "unauthenticated",
  invalid_report: "invalid-argument",
  too_many_reports: "resource-exhausted",
};

function refuse(reason: ReportRejectReason): never {
  logger.info("reportMaterial", { result: reason });
  throw new HttpsError(ERROR_CODES[reason], reason, { reason });
}

function isReportReason(value: unknown): value is ReportReason {
  return typeof value === "string" && (REPORT_REASONS as readonly string[]).includes(value);
}

/** アプリから届いた中身を確かめる。文章は generateMaterial と同じ1〜200文字、モデル名は英数字と `.` `_` `-`。 */
export function parseReportRequest(data: unknown): ReportMaterialRequest {
  const body = (data ?? {}) as { prompt?: unknown; reason?: unknown; model?: unknown };
  const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
  const model = typeof body.model === "string" ? body.model.trim() : "";
  const isValid =
    prompt.length > 0 &&
    [...prompt].length <= MAX_PROMPT_LENGTH &&
    isReportReason(body.reason) &&
    model.length <= MAX_MODEL_LENGTH &&
    MODEL_PATTERN.test(model);
  if (!isValid) refuse("invalid_report");
  return { prompt, reason: body.reason as ReportReason, model };
}

/**
 * 利用者が、作った素材を報告する（Google Play の AI 生成コンテンツのポリシーで、アプリの中から報告できることが要る）。
 * 入口スイッチが閉じていても受け付ける（閉じる前に作った素材も報告できるように）。
 * App Check の確認は `onCall` の `enforceAppCheck` が先に行う（index.ts）。
 */
export async function handleReportMaterial(uid: string | undefined, data: unknown, deps: ReportMaterialDeps): Promise<void> {
  if (!uid) refuse("unauthenticated");
  const request = parseReportRequest(data);
  const now = deps.now();
  const dateKey = jstDateKey(now);
  const reportRef = deps.db.collection(REPORTS_COLLECTION).doc();
  const usageRef = deps.db.collection(REPORT_USAGE_COLLECTION).doc(uid);

  const accepted = await deps.db.runTransaction(async (tx) => {
    const usage = (await tx.get(usageRef)).data();
    const count = usage?.dateKey === dateKey && typeof usage.count === "number" ? usage.count : 0;
    if (count >= MAX_REPORTS_PER_DAY) return false;
    tx.set(reportRef, {
      uid,
      prompt: request.prompt,
      reason: request.reason,
      model: request.model,
      createdAt: Timestamp.fromDate(now),
      expireAt: Timestamp.fromMillis(now.getTime() + REPORT_RETENTION_DAYS * DAY_MS),
    });
    tx.set(usageRef, { dateKey, count: count + 1 });
    return true;
  });
  if (!accepted) refuse("too_many_reports");

  // ログと Discord には頼んだ文章を書かない（中身は Firestore で見る）
  logger.info("reportMaterial", { result: "ok", reason: request.reason, model: request.model, reportId: reportRef.id });
  await deps
    .notifyOwner(
      `📣 AI素材の報告が1件届いた（理由：${REASON_LABELS[request.reason]}、モデル：${request.model}）。` +
        `Firestore の ${REPORTS_COLLECTION}/${reportRef.id} を見て、断る名前の一覧と審査で防げるかを確かめる`,
    )
    .catch((error) => logger.error("Discord に知らせられなかった", { error: String(error) }));
}
