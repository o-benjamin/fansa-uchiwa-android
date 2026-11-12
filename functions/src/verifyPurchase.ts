import { createHash } from "node:crypto";
import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { FunctionsErrorCode } from "firebase-functions/v2/https";
import { Timestamp } from "firebase-admin/firestore";
import type { DocumentData, Firestore } from "firebase-admin/firestore";
import { addMonthsJst } from "./jstDate";
import type { PlayBillingClient, ProductPurchase } from "./playBilling";
import { ConsumptionState, PlayHttpError, PurchaseState } from "./playBilling";
import { countUsableTickets, USERS_COLLECTION } from "./quota";

/**
 * 回数券のアイテム ID（Play Console のアプリ内アイテム。消費型）。値段は Play Console で決め、ここには書かない
 * （公開後に Play の価格実験で値段を変えるため）。
 * この定数と TICKETS_PER_PURCHASE・TICKET_VALID_MONTHS は、アプリにも同じ値を書いて購入の案内に出している（README の「verifyPurchase」）。変えるときはアプリも直す。
 */
export const TICKET_PRODUCT_ID = "ai_material_ticket_5";
/**
 * 1購入（数量1）で足す回数。支払った額（価格実験で変わる）によらず同じ。数量の付いた購入は数量の分だけ足すが、
 * Play Console で数量の選択は有効にしない（数量の一部だけの返金を、返金の取り消しで拾わないため）
 */
export const TICKETS_PER_PURCHASE = 5;
/** 回数券を使える期間（購入から）。6か月以内にするのは資金決済法の対象外にするため */
export const TICKET_VALID_MONTHS = 6;

const MAX_PURCHASE_TOKEN_LENGTH = 4096;

export const PURCHASES_COLLECTION = "purchases";

/*
 * 購入の記録（書けるのはサーバーだけ）
 *
 * purchases/{購入 ID（購入トークンの SHA-256）}
 *   uid, orderId, productId     だれが・どの注文で・どのアイテムを買ったか（問い合わせは orderId で探す）
 *   tickets: number             足した回数
 *   purchasedAt, expiresAt      購入した時刻と、回数券の期限
 *   grantedAt                   回数を足した時刻
 *   consumed: boolean           Play で消費済みにしたか（false のままなら、次の確認でやり直す）
 *   voidedAt ほか               返金で取り消したとき（voidedPurchases.ts）
 *
 * トークンをそのまま ID にしないのは、長さと使える文字が決まっていないため。トークン自体は保存しない。
 */

/** 断ったときに `HttpsError` の `details.reason` に入れる理由。アプリはこの値で出す文言を選ぶ。 */
export type PurchaseRejectReason =
  | "unauthenticated"
  | "invalid_purchase"
  | "pending"
  | "not_purchased"
  | "owned_by_other"
  | "verification_failed";

const ERROR_CODES: Record<PurchaseRejectReason, FunctionsErrorCode> = {
  unauthenticated: "unauthenticated",
  invalid_purchase: "invalid-argument",
  pending: "failed-precondition",
  not_purchased: "permission-denied",
  owned_by_other: "permission-denied",
  verification_failed: "unavailable",
};

export interface VerifyPurchaseRequest {
  productId: string;
  purchaseToken: string;
}

export interface VerifyPurchaseResponse {
  /** この購入で足した回数 */
  ticketsAdded: number;
  /** 足したあとの、使える回数券の残り */
  ticketsRemaining: number;
  /** この購入の回数券の期限（ISO 8601） */
  expiresAt: string;
  /** 前の呼び出しで足し済みだった（応答が届かずアプリが送り直したときなど）。足した回数は変わらない */
  alreadyGranted: boolean;
}

export interface VerifyPurchaseDeps {
  db: Firestore;
  play: PlayBillingClient;
  now: () => Date;
}

/**
 * 断る。アプリには理由だけを返し、`logFields`（購入 ID・注文番号・Play の状態など）はログにだけ書く
 * （generateMaterial.ts の `refuse` と違い、`details` に足さない）。
 */
function refuse(reason: PurchaseRejectReason, logFields: Record<string, unknown> = {}): never {
  logger.info("verifyPurchase", { result: reason, ...logFields });
  throw new HttpsError(ERROR_CODES[reason], reason, { reason });
}

export function parseRequest(data: unknown): VerifyPurchaseRequest {
  const body = (data ?? {}) as { productId?: unknown; purchaseToken?: unknown };
  const isValid =
    body.productId === TICKET_PRODUCT_ID &&
    typeof body.purchaseToken === "string" &&
    body.purchaseToken.length > 0 &&
    body.purchaseToken.length <= MAX_PURCHASE_TOKEN_LENGTH;
  if (!isValid) refuse("invalid_purchase");
  return { productId: TICKET_PRODUCT_ID, purchaseToken: body.purchaseToken as string };
}

export function purchaseIdOf(purchaseToken: string): string {
  return createHash("sha256").update(purchaseToken).digest("hex");
}

/**
 * 回数券の購入を確かめて、回数を足す（#348）。順番：ログインの確認 → 中身の確認 → 足し済みか →
 * Play で購入を確かめる → 回数を足す → Play で消費済みにする。
 * 足してから消費する（逆だと、足す前に落ちたとき、払ったのに回数がなく、アプリからも購入が消える）。
 * 消費に失敗しても足した回数はそのままにし、アプリが同じ購入を送り直したときに消費をやり直す。
 * 入口スイッチ（`ai_material_enabled`）は見ない。閉じたあとでも、払った人には回数を渡す。
 */
export async function handleVerifyPurchase(
  uid: string | undefined,
  data: unknown,
  deps: VerifyPurchaseDeps,
): Promise<VerifyPurchaseResponse> {
  if (!uid) refuse("unauthenticated");
  const request = parseRequest(data);
  const purchaseId = purchaseIdOf(request.purchaseToken);
  const purchaseRef = deps.db.collection(PURCHASES_COLLECTION).doc(purchaseId);

  const existing = await refuseIfThrows(purchaseRef.get(), "購入の記録を読めなかった", purchaseId);
  if (existing.exists) return answerAlreadyGranted(uid, purchaseId, existing.data() ?? {}, request, deps);

  const purchase = await fetchPurchase(deps.play, request, purchaseId);
  const purchaseState = purchase.purchaseState ?? PurchaseState.PURCHASED;
  const logFields = { purchaseId, orderId: purchase.orderId };
  if (purchaseState === PurchaseState.PENDING) refuse("pending", logFields);
  if (purchaseState !== PurchaseState.PURCHASED) refuse("not_purchased", { ...logFields, purchaseState });

  const granted = await refuseIfThrows(grantTickets(deps, uid, purchaseId, request, purchase), "回数を足せなかった", purchaseId);
  if (!granted.ok) return answerAlreadyGranted(uid, purchaseId, granted.existing, request, deps);

  if (purchase.consumptionState !== ConsumptionState.CONSUMED) await consume(deps, request, purchaseId);
  logger.info("verifyPurchase", { result: "granted", purchaseId, orderId: purchase.orderId, tickets: granted.tickets });
  return {
    ticketsAdded: granted.tickets,
    ticketsRemaining: granted.ticketsRemaining,
    expiresAt: granted.expiresAt.toISOString(),
    alreadyGranted: false,
  };
}

/**
 * Play で購入を確かめる。トークンが違う・知らない購入（400・404・410）は `not_purchased`、
 * それ以外の失敗（権限がない・Play の障害）は、やり直せるよう `verification_failed` で断る。
 */
async function fetchPurchase(
  play: PlayBillingClient,
  request: VerifyPurchaseRequest,
  purchaseId: string,
): Promise<ProductPurchase> {
  try {
    return await play.getProductPurchase(request.productId, request.purchaseToken);
  } catch (error) {
    if (error instanceof PlayHttpError && [400, 404, 410].includes(error.status)) {
      refuse("not_purchased", { purchaseId, status: error.status, apiStatus: error.apiStatus });
    }
    logger.error("Play で購入を確かめられなかった", { purchaseId, error: String(error) });
    refuse("verification_failed");
  }
}

type GrantResult =
  | { ok: true; tickets: number; ticketsRemaining: number; expiresAt: Date }
  | { ok: false; existing: DocumentData };

/** 購入の記録と回数券を1つのトランザクションで書く。同じ購入が同時に届いても、足すのは1回だけ。 */
async function grantTickets(
  deps: VerifyPurchaseDeps,
  uid: string,
  purchaseId: string,
  request: VerifyPurchaseRequest,
  purchase: ProductPurchase,
): Promise<GrantResult> {
  const now = deps.now();
  const purchasedAt = purchase.purchaseTimeMillis ? new Date(Number(purchase.purchaseTimeMillis)) : now;
  const expiresAt = addMonthsJst(purchasedAt, TICKET_VALID_MONTHS);
  const tickets = TICKETS_PER_PURCHASE * (purchase.quantity ?? 1);
  const purchaseRef = deps.db.collection(PURCHASES_COLLECTION).doc(purchaseId);
  const userRef = deps.db.collection(USERS_COLLECTION).doc(uid);

  return deps.db.runTransaction(async (transaction) => {
    const [purchaseSnapshot, userSnapshot] = await transaction.getAll(purchaseRef, userRef);
    if (purchaseSnapshot.exists) return { ok: false, existing: purchaseSnapshot.data() ?? {} };

    transaction.create(purchaseRef, {
      uid,
      orderId: purchase.orderId ?? null,
      productId: request.productId,
      tickets,
      purchasedAt: Timestamp.fromDate(purchasedAt),
      expiresAt: Timestamp.fromDate(expiresAt),
      grantedAt: Timestamp.fromDate(now),
      consumed: purchase.consumptionState === ConsumptionState.CONSUMED,
    });
    transaction.set(
      userRef,
      { packs: { [purchaseId]: { remaining: tickets, expiresAt: Timestamp.fromDate(expiresAt) } } },
      { merge: true },
    );
    // 足す購入は期限内（消費しないまま3日たつと Play が返金するので、6か月前の未消費の購入は届かない）
    const before = countUsableTickets(userSnapshot.data(), now);
    return { ok: true, tickets, ticketsRemaining: before + tickets, expiresAt };
  });
}

/**
 * 足し済みの購入がまた届いたとき。同じ利用者なら、まだなら消費をやり直して、足したときの内容を返す。
 * 別の利用者（別の端末・入れ直して ID が変わった）には渡さず、消費もしない。消費しないままなら3日で Play が返金し、
 * revokeVoidedPurchases が前の ID の回数を消す（ここで消費すると、払った人は回数も返金も受け取れない）。
 * 返金で取り消し済みなら `not_purchased`。
 */
async function answerAlreadyGranted(
  uid: string,
  purchaseId: string,
  record: DocumentData,
  request: VerifyPurchaseRequest,
  deps: VerifyPurchaseDeps,
): Promise<VerifyPurchaseResponse> {
  // 問い合わせで回数券を手で直せるよう、注文番号と両方の uid を残す（generateMaterial の「回数を戻せなかった」と同じ扱い）
  if (record.uid !== uid) refuse("owned_by_other", { purchaseId, orderId: record.orderId, uid, ownerUid: record.uid });
  if (record.voidedAt) refuse("not_purchased", { purchaseId, orderId: record.orderId, voided: true });
  if (record.consumed !== true) await consume(deps, request, purchaseId);

  const user = await refuseIfThrows(deps.db.collection(USERS_COLLECTION).doc(uid).get(), "回数を読めなかった", purchaseId);
  logger.info("verifyPurchase", { result: "already_granted", purchaseId });
  return {
    ticketsAdded: record.tickets,
    ticketsRemaining: countUsableTickets(user.data(), deps.now()),
    expiresAt: (record.expiresAt as Timestamp).toDate().toISOString(),
    alreadyGranted: true,
  };
}

/**
 * Play で消費済みにする。失敗しても断らない（回数は足してある）。消費しないまま3日たつと Play が自動で返金するので、
 * ログの「消費できなかった」が続くときは README の「困ったとき」を見る。
 */
async function consume(deps: VerifyPurchaseDeps, request: VerifyPurchaseRequest, purchaseId: string): Promise<void> {
  try {
    await deps.play.consumeProductPurchase(request.productId, request.purchaseToken);
    await deps.db.collection(PURCHASES_COLLECTION).doc(purchaseId).update({ consumed: true });
  } catch (error) {
    logger.error("消費できなかった", { purchaseId, error: String(error) });
  }
}

/** Firestore を読み書きできなかったときは、やり直せるよう `verification_failed` で断る。 */
async function refuseIfThrows<T>(work: Promise<T>, message: string, purchaseId: string): Promise<T> {
  try {
    return await work;
  } catch (error) {
    logger.error(message, { purchaseId, error: String(error) });
    refuse("verification_failed");
  }
}
