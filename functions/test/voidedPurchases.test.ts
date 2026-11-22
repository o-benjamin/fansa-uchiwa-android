import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";
import { test } from "node:test";
import { FieldPath, FieldValue } from "firebase-admin/firestore";
import type { PlayBillingClient, VoidedPurchase, VoidedPurchasesPage } from "../src/playBilling";
import { reserveCredit, refundCredit } from "../src/quota";
import { handleVerifyPurchase, purchaseIdOf, TICKET_PRODUCT_ID } from "../src/verifyPurchase";
import { handleRevokeVoidedPurchases } from "../src/voidedPurchases";
import { emulatorDb, LIMITS, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();

/** 購入の確認はいつも「購入済み」、返金の一覧は渡したページを順に返す Play の偽物。 */
function fakePlay(now: Date, pages: VoidedPurchasesPage[] = []) {
  const pageTokens: (string | undefined)[] = [];
  const play: PlayBillingClient = {
    getProductPurchase: async () => ({ purchaseState: 0, consumptionState: 1, purchaseTimeMillis: String(now.getTime()) }),
    consumeProductPurchase: async () => {},
    listVoidedPurchases: async (pageToken) => {
      pageTokens.push(pageToken);
      return pages[pageTokens.length - 1] ?? { voidedPurchases: [] };
    },
  };
  return { play, pageTokens };
}

/** 回数券を1つ買った利用者を作る。 */
async function buyTicket(now: Date) {
  const uid = uniqueUid();
  const purchaseToken = randomUUID();
  await handleVerifyPurchase(uid, { productId: TICKET_PRODUCT_ID, purchaseToken }, { db, play: fakePlay(now).play, now: () => now });
  return { uid, purchaseToken, purchaseId: purchaseIdOf(purchaseToken) };
}

function voided(purchaseToken: string, voidedAt: Date): VoidedPurchase {
  return { purchaseToken, orderId: "GPA.9", voidedTimeMillis: String(voidedAt.getTime()), voidedSource: 0, voidedReason: 1 };
}

test("返金された購入の回数券を消し、残っていた回数と返金の時刻を記録に残す", async () => {
  const now = uniqueDay();
  const { uid, purchaseToken, purchaseId } = await buyTicket(now);
  await db.collection("users").doc(uid).update(new FieldPath("packs", purchaseId, "remaining"), FieldValue.increment(-2));
  const { play } = fakePlay(now, [{ voidedPurchases: [voided(purchaseToken, now)] }]);

  const summary = await handleRevokeVoidedPurchases({ db, play, now: () => now });

  assert.deepEqual(summary, { voided: 1, revoked: 1, noRecord: 0, failed: 0 });
  const user = (await db.collection("users").doc(uid).get()).data();
  assert.equal(user?.packs[purchaseId], undefined);
  const record = (await db.collection("purchases").doc(purchaseId).get()).data();
  assert.equal(record?.ticketsRevoked, 3);
  assert.equal(record?.voidedReason, 1);
  assert.equal(record?.voidedAt.toMillis(), now.getTime());
});

test("毎日同じ返金が返ってきても、2回目からは飛ばす", async () => {
  const now = uniqueDay();
  const { purchaseToken } = await buyTicket(now);
  const page = { voidedPurchases: [voided(purchaseToken, now)] };

  await handleRevokeVoidedPurchases({ db, play: fakePlay(now, [page]).play, now: () => now });
  const second = await handleRevokeVoidedPurchases({ db, play: fakePlay(now, [page]).play, now: () => now });

  assert.deepEqual(second, { voided: 1, revoked: 0, noRecord: 0, failed: 0 });
});

test("記録のない購入は数えるだけで、ほかの利用者の回数券には触らない", async () => {
  const now = uniqueDay();
  const { uid, purchaseId } = await buyTicket(now);
  const { play } = fakePlay(now, [{ voidedPurchases: [voided(randomUUID(), now)] }]);

  const summary = await handleRevokeVoidedPurchases({ db, play, now: () => now });

  assert.equal(summary.noRecord, 1);
  assert.equal((await db.collection("users").doc(uid).get()).get(new FieldPath("packs", purchaseId, "remaining")), 5);
});

test("1件が取り消せなくても、ほかの件は取り消す", async () => {
  const now = uniqueDay();
  const broken = await buyTicket(now);
  const healthy = await buyTicket(now);
  // 時刻が数値でないと、記録に書く Timestamp を作れずトランザクションが落ちる
  const brokenEntry = { ...voided(broken.purchaseToken, now), voidedTimeMillis: "not-a-number" };
  const { play } = fakePlay(now, [{ voidedPurchases: [brokenEntry, voided(healthy.purchaseToken, now)] }]);

  const summary = await handleRevokeVoidedPurchases({ db, play, now: () => now });

  assert.deepEqual(summary, { voided: 2, revoked: 1, noRecord: 0, failed: 1 });
  assert.equal((await db.collection("users").doc(healthy.uid).get()).get(new FieldPath("packs", healthy.purchaseId)), undefined);
  assert.equal((await db.collection("users").doc(broken.uid).get()).get(new FieldPath("packs", broken.purchaseId, "remaining")), 5);
});

test("次のページがある間はたどる", async () => {
  const now = uniqueDay();
  const first = await buyTicket(now);
  const second = await buyTicket(now);
  const { play, pageTokens } = fakePlay(now, [
    { voidedPurchases: [voided(first.purchaseToken, now)], nextPageToken: "page-2" },
    { voidedPurchases: [voided(second.purchaseToken, now)] },
  ]);

  const summary = await handleRevokeVoidedPurchases({ db, play, now: () => now });

  assert.deepEqual(pageTokens, [undefined, "page-2"]);
  assert.equal(summary.revoked, 2);
});

test("取り消したあとは、作る途中でおさえた1回が失敗しても回数券に戻さない", async () => {
  const now = uniqueDay();
  const { uid, purchaseToken, purchaseId } = await buyTicket(now);
  await db.collection("users").doc(uid).set({ freeUsed: true }, { merge: true });
  const reserved = await reserveCredit(db, uid, LIMITS, now);
  assert.ok(reserved.ok);

  const { play } = fakePlay(now, [{ voidedPurchases: [voided(purchaseToken, now)] }]);
  await handleRevokeVoidedPurchases({ db, play, now: () => now });
  await refundCredit(db, uid, reserved.reservation);

  assert.equal((await db.collection("users").doc(uid).get()).get(new FieldPath("packs", purchaseId)), undefined);
  assert.equal((await db.collection("purchases").doc(purchaseId).get()).get("ticketsRevoked"), 4);
});
