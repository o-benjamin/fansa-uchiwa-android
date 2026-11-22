import { FieldPath, FieldValue, Timestamp } from "firebase-admin/firestore";
import type { DocumentData, DocumentReference, DocumentSnapshot, Firestore, Transaction } from "firebase-admin/firestore";
import { jstDateKey } from "./jstDate";

/*
 * 回数の持ち方（書けるのはサーバーだけ。firestore.rules でアプリからは読み書きできない）
 *
 * users/{uid}
 *   freeUsed: boolean           無料の1回を使ったか（匿名ログインの ID ごとに1回。日ごとには戻らない）。
 *                               アプリを入れ直すと ID が変わり、もう1回使えるので、無料の総数は下の全体の1日の上限
 *                               （無料の非常ブレーキ）で抑える
 *   packs: {                    回数券。キーは購入ごとの ID（購入トークンの SHA-256。verifyPurchase.ts が足し、
 *                               返金されたら voidedPurchases.ts が消す。購入の記録は purchases/{同じ ID}）
 *     <packId>: { remaining: number, expiresAt: Timestamp }   期限は購入から6か月
 *   }
 *   lastRequest: {              回数を使った最後の頼み（#387）。届いたか分からなかった頼みを、アプリが同じ番号でもう一度送ったら、
 *                               回数を使わずに1回だけ作り直す（reserveCreditForRequest）
 *     id: string                頼みの番号。アプリが「作る」で作り、素材を受け取るまで同じ番号で送り直す
 *     state: RequestState       generating（作っている）→ done（作れた）→ redoing（作り直している）→ redone（作り直せた）。
 *                               作り直しに失敗したら done に戻す。断った・失敗して回数を戻したら lastRequest ごと消す
 *     paidWith: "free" | "ticket"   使った回数の種類（作り直しの応答の paidWith）
 *     reservedAt: Timestamp     回数を使った時刻。作り直せるのはここから REDO_WINDOW_MS まで
 *     updatedAt: Timestamp      state を変えた時刻。generating・redoing のまま STALE_CALL_MS たったら、その呼び出しは止まったとみなす
 *   }
 *
 * dailyUsage/{日本時間の日付}
 *   free: number                その日に無料で作った回数（全体）
 *   ticket: number              その日に回数券で作った回数（全体）
 *                               どちらも、断った・失敗して戻した分は数えない（refundCredit が引く）
 *   totalLimitAlerted: boolean  全体の安全上限に当たったことを Discord に知らせたか（1日1回だけ知らせる）
 *
 * 呼び方
 *   credit   生成1回分。無料の1回か、回数券の1回（reserveCredit / refundCredit、断りの理由 no_credits）
 *   pack     1回の購入で足した回数券のまとまり。packId は購入 ID で、verifyPurchase.ts・voidedPurchases.ts の purchaseId と同じ値
 *   ticket   回数券の1回分。ticketsRemaining は使える pack の remaining の合計
 *   Reservation.kind  今回使ったもの（free / ticket）。応答では paidWith、アプリでは AiMaterialPayment
 *   request  アプリの「作る」1回分の頼み。届かなかったときに同じ番号で送り直される（requestId）
 */

export const USERS_COLLECTION = "users";
export const DAILY_USAGE_COLLECTION = "dailyUsage";

/** 届いたか分からなかった頼みを作り直せるのは、回数を使ってからこの時間まで（#387） */
export const REDO_WINDOW_MS = 15 * 60 * 1000;
/** generating・redoing のままこの時間がたったら、その呼び出しは止まったとみなす。関数の timeoutSeconds（120秒）より長くする */
export const STALE_CALL_MS = 3 * 60 * 1000;

export interface DailyLimits {
  freeDailyLimit: number;
  totalDailyLimit: number;
}

/** 1回分をおさえた記録。失敗したときに、これを渡して同じところへ戻す。 */
export type Reservation =
  | { kind: "free"; dateKey: string }
  | { kind: "ticket"; dateKey: string; packId: string };

export type ReserveOutcome =
  | { ok: true; reservation: Reservation; ticketsRemaining: number }
  | {
      ok: false;
      /**
       * no_credits：無料を使い終え、回数券もない（購入画面へ）
       * free_daily_limit：無料の1回は残っているが、無料の非常ブレーキに当たった（明日また試せる）
       * total_daily_limit：全体の安全上限に当たった（無料も回数券も止める）
       */
      reason: "no_credits" | "free_daily_limit" | "total_daily_limit";
      /** 全体の安全上限に、この呼び出しで初めて当たった（Discord に知らせる） */
      firstTotalLimitHit: boolean;
    };

export type RequestState = "generating" | "done" | "redoing" | "redone";

/**
 * 頼みの番号つきでおさえたときの結果。[ReserveOutcome] に次の2つが足される
 * - redo：同じ番号の前の頼みで回数を使って作れていたので、回数を使わずに作り直す。paidWith は前の頼みで使ったもの
 * - in_progress：同じ番号の前の頼みをまだ作っている（回数は使わずに断る）
 */
export type RequestOutcome =
  | ReserveOutcome
  | { ok: true; redo: true; paidWith: Reservation["kind"]; ticketsRemaining: number }
  | { ok: false; reason: "in_progress"; firstTotalLimitHit: false };

interface Pack {
  id: string;
  remaining: number;
  expiresAt: Timestamp;
}

/** 使える回数券（残りがあり期限内）を、期限の近い順に返す。 */
function usablePacks(userData: DocumentData | undefined, now: Date): Pack[] {
  const packs = (userData?.packs ?? {}) as Record<string, { remaining?: unknown; expiresAt?: unknown }>;
  return Object.entries(packs)
    .filter(([, pack]) => typeof pack.remaining === "number" && pack.remaining > 0)
    .filter(([, pack]) => pack.expiresAt instanceof Timestamp && pack.expiresAt.toMillis() > now.getTime())
    .map(([id, pack]) => ({ id, remaining: pack.remaining as number, expiresAt: pack.expiresAt as Timestamp }))
    .sort((a, b) => a.expiresAt.toMillis() - b.expiresAt.toMillis());
}

function sumRemaining(packs: Pack[]): number {
  return packs.reduce((sum, pack) => sum + pack.remaining, 0);
}

/** 使える回数券の残り（期限内のものの合計）。 */
export function countUsableTickets(userData: DocumentData | undefined, now: Date): number {
  return sumRemaining(usablePacks(userData, now));
}

/**
 * 1回分をおさえる。無料の1回が残っていて非常ブレーキの手前なら無料を、そうでなければ期限の近い回数券を使う。
 * 無料の1回が残っていてもブレーキに当たっていれば、回数券があれば回数券を使う（ブレーキ中に買った人が待たずに済むように）。
 * 二重に使えないよう、利用者と全体の回数を1つのトランザクションで読み書きする。
 */
export async function reserveCredit(
  db: Firestore,
  uid: string,
  limits: DailyLimits,
  now: Date,
): Promise<ReserveOutcome> {
  return db.runTransaction(async (transaction) => {
    const target = await readReserveTarget(db, transaction, uid, now);
    return reserveInTransaction(transaction, target, limits, now);
  });
}

/**
 * 頼みの番号つきで1回分をおさえる（#387）。同じ番号の前の頼みで回数を使っていれば、次のとおり回数を使わない。
 * - 前の頼みをまだ作っている（generating・redoing で STALE_CALL_MS 以内）→ in_progress で断る
 * - 前の頼みが作れていて（done。generating・redoing のまま止まったものも含む）、回数を使ってから REDO_WINDOW_MS 以内 →
 *   作り直す（redoing にする）。回数も全体の回数も動かさない。1回の頼みにつき1回までなので、Gemini の費用は多くて2倍
 * - そのほか（番号が違う・作り直し済み・時間を過ぎた・前の頼みは回数を戻した）→ reserveCredit と同じくおさえ、頼みを覚える
 * 届いたか分からなかった利用者が押し直すと、作った絵は前と同じではなく同じ頼みで新しく作ったものになる（サーバーに絵は残さない）。
 * 作り直しの文章と元画像が前と同じかは確かめない（前の頼みは受け取れていないので、変えても1回分で1枚しか渡らない）
 */
export async function reserveCreditForRequest(
  db: Firestore,
  uid: string,
  limits: DailyLimits,
  now: Date,
  requestId: string,
): Promise<RequestOutcome> {
  return db.runTransaction(async (transaction) => {
    const target = await readReserveTarget(db, transaction, uid, now);
    const userData = target.userSnapshot.data();
    const previous = checkPreviousRequest(userData?.lastRequest, requestId, now);
    if (previous === "in_progress") return { ok: false, reason: "in_progress", firstTotalLimitHit: false };
    if (previous !== "new") {
      transaction.update(target.userRef, {
        "lastRequest.state": "redoing" satisfies RequestState,
        "lastRequest.updatedAt": Timestamp.fromDate(now),
      });
      return { ok: true, redo: true, paidWith: previous.redo, ticketsRemaining: countUsableTickets(userData, now) };
    }

    const outcome = reserveInTransaction(transaction, target, limits, now);
    if (outcome.ok) {
      const at = Timestamp.fromDate(now);
      const lastRequest = {
        id: requestId,
        state: "generating" satisfies RequestState,
        paidWith: outcome.reservation.kind,
        reservedAt: at,
        updatedAt: at,
      };
      transaction.set(target.userRef, { lastRequest }, { merge: true });
    }
    return outcome;
  });
}

interface ReserveTarget {
  dateKey: string;
  userRef: DocumentReference;
  usageRef: DocumentReference;
  userSnapshot: DocumentSnapshot;
  usageSnapshot: DocumentSnapshot;
}

async function readReserveTarget(
  db: Firestore,
  transaction: Transaction,
  uid: string,
  now: Date,
): Promise<ReserveTarget> {
  const dateKey = jstDateKey(now);
  const userRef = db.collection(USERS_COLLECTION).doc(uid);
  const usageRef = db.collection(DAILY_USAGE_COLLECTION).doc(dateKey);
  const [userSnapshot, usageSnapshot] = await transaction.getAll(userRef, usageRef);
  return { dateKey, userRef, usageRef, userSnapshot, usageSnapshot };
}

function reserveInTransaction(
  transaction: Transaction,
  target: ReserveTarget,
  limits: DailyLimits,
  now: Date,
): ReserveOutcome {
  const { dateKey, userRef, usageRef } = target;
  const usage = target.usageSnapshot.data() ?? {};
  const freeToday = typeof usage.free === "number" ? usage.free : 0;
  const ticketToday = typeof usage.ticket === "number" ? usage.ticket : 0;

  if (freeToday + ticketToday >= limits.totalDailyLimit) {
    const firstTotalLimitHit = usage.totalLimitAlerted !== true;
    if (firstTotalLimitHit) transaction.set(usageRef, { totalLimitAlerted: true }, { merge: true });
    return { ok: false, reason: "total_daily_limit", firstTotalLimitHit };
  }

  const userData = target.userSnapshot.data();
  const freeAvailable = userData?.freeUsed !== true;
  const packs = usablePacks(userData, now);

  if (freeAvailable && freeToday < limits.freeDailyLimit) {
    transaction.set(userRef, { freeUsed: true }, { merge: true });
    transaction.set(usageRef, { free: FieldValue.increment(1) }, { merge: true });
    return { ok: true, reservation: { kind: "free", dateKey }, ticketsRemaining: sumRemaining(packs) };
  }

  const pack = packs[0];
  if (pack) {
    transaction.update(userRef, new FieldPath("packs", pack.id, "remaining"), FieldValue.increment(-1));
    transaction.set(usageRef, { ticket: FieldValue.increment(1) }, { merge: true });
    return {
      ok: true,
      reservation: { kind: "ticket", dateKey, packId: pack.id },
      ticketsRemaining: sumRemaining(packs) - 1,
    };
  }

  return { ok: false, reason: freeAvailable ? "free_daily_limit" : "no_credits", firstTotalLimitHit: false };
}

/** 同じ番号の前の頼みを見て、回数を使わずに断るか（in_progress）・作り直すか（redo）・ふつうにおさえるか（new）を決める。 */
function checkPreviousRequest(
  lastRequest: unknown,
  requestId: string,
  now: Date,
): "new" | "in_progress" | { redo: Reservation["kind"] } {
  const last = (lastRequest ?? {}) as {
    id?: unknown;
    state?: unknown;
    paidWith?: unknown;
    reservedAt?: unknown;
    updatedAt?: unknown;
  };
  if (last.id !== requestId) return "new";
  if (!(last.reservedAt instanceof Timestamp) || !(last.updatedAt instanceof Timestamp)) return "new";
  if (last.paidWith !== "free" && last.paidWith !== "ticket") return "new";

  const busy = last.state === "generating" || last.state === "redoing";
  if (busy && now.getTime() - last.updatedAt.toMillis() < STALE_CALL_MS) return "in_progress";
  // ここでの busy は、止まった呼び出し。generating なら回数を使ったまま戻していないので、作れたときと同じく作り直せる
  const finished = last.state === "done" || busy;
  if (finished && now.getTime() - last.reservedAt.toMillis() <= REDO_WINDOW_MS) return { redo: last.paidWith };
  return "new";
}

/**
 * 頼みの状態を進める（作れた：generating → done、作り直せた：redoing → redone、作り直しに失敗：redoing → done）。
 * 番号か状態が違えば（あとの頼みで上書きされたなど）何もしない。
 */
export async function updateRequestState(
  db: Firestore,
  uid: string,
  requestId: string,
  from: RequestState,
  to: RequestState,
  now: Date,
): Promise<void> {
  const userRef = db.collection(USERS_COLLECTION).doc(uid);
  await db.runTransaction(async (transaction) => {
    const last = (await transaction.get(userRef)).get("lastRequest") as { id?: unknown; state?: unknown } | undefined;
    if (last?.id !== requestId || last?.state !== from) return;
    transaction.update(userRef, { "lastRequest.state": to, "lastRequest.updatedAt": Timestamp.fromDate(now) });
  });
}

/**
 * おさえた1回分を戻す（生成に失敗した・審査で断った・安全フィルターで止まったとき）。
 * 回数券は、購入の取り消しなどで消えていれば戻さない。全体の回数は、おさえた日の分を戻す。
 * [requestId] の頼みを覚えていれば消す（同じ番号でもう一度来たら、作り直しではなくふつうにおさえる）。
 * 利用者の回数を戻したら true、回数券が消えていて戻さなかったら false を返す。
 */
export async function refundCredit(
  db: Firestore,
  uid: string,
  reservation: Reservation,
  requestId?: string,
): Promise<boolean> {
  const userRef = db.collection(USERS_COLLECTION).doc(uid);
  const usageRef = db.collection(DAILY_USAGE_COLLECTION).doc(reservation.dateKey);

  return db.runTransaction(async (transaction) => {
    const userSnapshot = await transaction.get(userRef);
    if (requestId !== undefined && userSnapshot.get("lastRequest.id") === requestId) {
      transaction.set(userRef, { lastRequest: FieldValue.delete() }, { merge: true });
    }
    if (reservation.kind === "free") {
      transaction.set(userRef, { freeUsed: false }, { merge: true });
      transaction.set(usageRef, { free: FieldValue.increment(-1) }, { merge: true });
      return true;
    }
    const packExists = userSnapshot.get(new FieldPath("packs", reservation.packId)) !== undefined;
    if (packExists) {
      transaction.update(userRef, new FieldPath("packs", reservation.packId, "remaining"), FieldValue.increment(1));
    }
    transaction.set(usageRef, { ticket: FieldValue.increment(-1) }, { merge: true });
    return packExists;
  });
}
