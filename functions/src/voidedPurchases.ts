import { logger } from "firebase-functions";
import { FieldPath, FieldValue, Timestamp } from "firebase-admin/firestore";
import type { Firestore } from "firebase-admin/firestore";
import type { PlayBillingClient, VoidedPurchase } from "./playBilling";
import { USERS_COLLECTION } from "./quota";
import { PURCHASES_COLLECTION, purchaseIdOf } from "./verifyPurchase";

/** 1回の実行でたどるページの上限（1ページ最大1000件）。返金がこれほど多いことは想定しないが、止まらなくなるのを防ぐ */
const MAX_PAGES = 20;

export interface RevokeSummary {
  /** Play が返した返金・取り消しの件数（過去30日分。前の日に取り消した分も入る） */
  voided: number;
  /** 今回、回数券を取り消した件数 */
  revoked: number;
  /** 購入の記録がなかった件数（回数を足す前に返金された、など） */
  noRecord: number;
  /** 取り消せなかった件数（ERROR ログに注文番号。翌日の実行でやり直す） */
  failed: number;
}

export interface RevokeVoidedPurchasesDeps {
  db: Firestore;
  play: PlayBillingClient;
  now: () => Date;
}

/**
 * 返金・取り消しされた購入の回数券を取り消す（#348。毎日の定期実行、index.ts）。
 * Play が返すのは過去30日分なので、毎日すべてを見直し、取り消し済みは飛ばす（止まっても30日以内なら次に動いたときに拾える）。
 * 取り消すのは残りの回数だけで、使った分は戻さない。1件が失敗しても、ほかの件は続ける。
 */
export async function handleRevokeVoidedPurchases(deps: RevokeVoidedPurchasesDeps): Promise<RevokeSummary> {
  const summary: RevokeSummary = { voided: 0, revoked: 0, noRecord: 0, failed: 0 };
  let pageToken: string | undefined;
  for (let page = 0; page < MAX_PAGES; page++) {
    const result = await deps.play.listVoidedPurchases(pageToken);
    for (const voided of result.voidedPurchases) {
      summary.voided++;
      const outcome = await revokeOne(deps, voided).catch((error: unknown) => {
        logger.error("返金された回数券を取り消せなかった", { orderId: voided.orderId, error: String(error) });
        return "failed" as const;
      });
      if (outcome === "revoked") summary.revoked++;
      if (outcome === "no_record") summary.noRecord++;
      if (outcome === "failed") summary.failed++;
    }
    pageToken = result.nextPageToken;
    if (!pageToken) break;
  }
  if (pageToken) logger.error("返金の一覧をたどりきれなかった", { maxPages: MAX_PAGES });
  logger.info("revokeVoidedPurchases", { ...summary });
  return summary;
}

/** 1件分。購入の記録に取り消した印を付け、利用者の回数券を消す（1つのトランザクションで）。 */
async function revokeOne(
  deps: RevokeVoidedPurchasesDeps,
  voided: VoidedPurchase,
): Promise<"revoked" | "already_revoked" | "no_record"> {
  const purchaseId = purchaseIdOf(voided.purchaseToken);
  const purchaseRef = deps.db.collection(PURCHASES_COLLECTION).doc(purchaseId);

  const outcome = await deps.db.runTransaction(async (transaction) => {
    const purchase = await transaction.get(purchaseRef);
    if (!purchase.exists) return "no_record" as const;
    if (purchase.get("voidedAt")) return "already_revoked" as const;

    const userRef = deps.db.collection(USERS_COLLECTION).doc(purchase.get("uid") as string);
    const user = await transaction.get(userRef);
    const remaining = user.get(new FieldPath("packs", purchaseId, "remaining"));
    const ticketsRevoked = typeof remaining === "number" ? remaining : 0;

    if (user.exists) transaction.update(userRef, new FieldPath("packs", purchaseId), FieldValue.delete());
    transaction.update(purchaseRef, {
      voidedAt: voided.voidedTimeMillis
        ? Timestamp.fromMillis(Number(voided.voidedTimeMillis))
        : Timestamp.fromDate(deps.now()),
      voidedReason: voided.voidedReason ?? null,
      voidedSource: voided.voidedSource ?? null,
      ticketsRevoked,
    });
    return "revoked" as const;
  });

  // 問い合わせで追えるよう、注文番号を残す（購入トークンは書かない）
  if (outcome !== "already_revoked") {
    logger.info("revokeVoidedPurchases", { result: outcome, purchaseId, orderId: voided.orderId });
  }
  return outcome;
}
