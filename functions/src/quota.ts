import { FieldPath, FieldValue, Timestamp } from "firebase-admin/firestore";
import type { DocumentData, Firestore } from "firebase-admin/firestore";
import { jstDateKey } from "./jstDate";

/*
 * 回数の持ち方（書けるのはサーバーだけ。firestore.rules でアプリからは読み書きできない）
 *
 * users/{uid}
 *   freeUsed: boolean           無料の1回を使ったか（匿名ログインの ID ごとに1回。日ごとには戻らない）
 *   packs: {                    回数券。キーは購入ごとの ID（Play の注文番号。購入の確認 #348 が足す）
 *     <packId>: { remaining: number, expiresAt: Timestamp }   期限は購入から6ヶ月
 *   }
 *
 * spend/{日本時間の日付}
 *   free: number                その日に無料で作った回数（全体）
 *   paid: number                その日に回数券で作った回数（全体）
 *   totalLimitAlerted: boolean  全体の安全上限に当たったことを Discord に知らせたか（1日1回だけ知らせる）
 */

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
  const dateKey = jstDateKey(now);
  const userRef = db.collection("users").doc(uid);
  const spendRef = db.collection("spend").doc(dateKey);

  return db.runTransaction(async (transaction) => {
    const [userSnapshot, spendSnapshot] = await transaction.getAll(userRef, spendRef);
    const spend = spendSnapshot.data() ?? {};
    const freeToday = typeof spend.free === "number" ? spend.free : 0;
    const paidToday = typeof spend.paid === "number" ? spend.paid : 0;

    if (freeToday + paidToday >= limits.totalDailyLimit) {
      const firstTotalLimitHit = spend.totalLimitAlerted !== true;
      if (firstTotalLimitHit) transaction.set(spendRef, { totalLimitAlerted: true }, { merge: true });
      return { ok: false, reason: "total_daily_limit", firstTotalLimitHit };
    }

    const userData = userSnapshot.data();
    const freeAvailable = userData?.freeUsed !== true;
    const packs = usablePacks(userData, now);

    if (freeAvailable && freeToday < limits.freeDailyLimit) {
      transaction.set(userRef, { freeUsed: true }, { merge: true });
      transaction.set(spendRef, { free: FieldValue.increment(1) }, { merge: true });
      return { ok: true, reservation: { kind: "free", dateKey }, ticketsRemaining: sumRemaining(packs) };
    }

    const pack = packs[0];
    if (pack) {
      transaction.update(userRef, new FieldPath("packs", pack.id, "remaining"), FieldValue.increment(-1));
      transaction.set(spendRef, { paid: FieldValue.increment(1) }, { merge: true });
      return {
        ok: true,
        reservation: { kind: "ticket", dateKey, packId: pack.id },
        ticketsRemaining: sumRemaining(packs) - 1,
      };
    }

    return { ok: false, reason: freeAvailable ? "free_daily_limit" : "no_credits", firstTotalLimitHit: false };
  });
}

/**
 * おさえた1回分を戻す（生成に失敗した・審査で断った・安全フィルターで止まったとき）。
 * 回数券は、購入の取り消しなどで消えていれば戻さない。全体の回数は、おさえた日の分を戻す。
 */
export async function refundCredit(db: Firestore, uid: string, reservation: Reservation): Promise<void> {
  const userRef = db.collection("users").doc(uid);
  const spendRef = db.collection("spend").doc(reservation.dateKey);

  await db.runTransaction(async (transaction) => {
    const userSnapshot = await transaction.get(userRef);
    if (reservation.kind === "free") {
      transaction.set(userRef, { freeUsed: false }, { merge: true });
      transaction.set(spendRef, { free: FieldValue.increment(-1) }, { merge: true });
      return;
    }
    const packExists = userSnapshot.get(new FieldPath("packs", reservation.packId)) !== undefined;
    if (packExists) {
      transaction.update(userRef, new FieldPath("packs", reservation.packId, "remaining"), FieldValue.increment(1));
    }
    transaction.set(spendRef, { paid: FieldValue.increment(-1) }, { merge: true });
  });
}
