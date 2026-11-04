import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";
import { test } from "node:test";
import { HttpsError } from "firebase-functions/v2/https";
import { addMonthsJst } from "../src/jstDate";
import type { PlayBillingClient, ProductPurchase } from "../src/playBilling";
import { PlayHttpError } from "../src/playBilling";
import { handleVerifyPurchase, purchaseIdOf, TICKET_PRODUCT_ID } from "../src/verifyPurchase";
import { daysLater, emulatorDb, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();

interface FakeOptions {
  purchase?: () => Promise<ProductPurchase>;
  consume?: () => Promise<void>;
}

function fakeDeps(now: Date, options: FakeOptions = {}) {
  const calls = { get: 0, consume: 0 };
  const play: PlayBillingClient = {
    getProductPurchase: async () => {
      calls.get++;
      return options.purchase
        ? options.purchase()
        : { purchaseState: 0, consumptionState: 0, orderId: "GPA.1234", purchaseTimeMillis: String(now.getTime()) };
    },
    consumeProductPurchase: async () => {
      calls.consume++;
      if (options.consume) await options.consume();
    },
    listVoidedPurchases: async () => ({ voidedPurchases: [] }),
  };
  return { deps: { db, play, now: () => now }, calls };
}

function requestFor(purchaseToken: string) {
  return { productId: TICKET_PRODUCT_ID, purchaseToken };
}

async function rejectionReason(work: Promise<unknown>): Promise<string> {
  try {
    await work;
  } catch (error) {
    assert.ok(error instanceof HttpsError);
    return (error.details as { reason: string }).reason;
  }
  assert.fail("断られなかった");
}

async function userOf(uid: string) {
  return (await db.collection("users").doc(uid).get()).data();
}

test("購入済みなら5回足し、期限は購入から6か月で、消費済みにする", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const token = randomUUID();
  const { deps, calls } = fakeDeps(now);

  const response = await handleVerifyPurchase(uid, requestFor(token), deps);

  const expected = addMonthsJst(now, 6);
  assert.deepEqual(response, {
    ticketsAdded: 5,
    ticketsRemaining: 5,
    expiresAt: expected.toISOString(),
    alreadyGranted: false,
  });
  const purchaseId = purchaseIdOf(token);
  const pack = (await userOf(uid))?.packs[purchaseId];
  assert.equal(pack.remaining, 5);
  assert.equal(pack.expiresAt.toDate().toISOString(), expected.toISOString());
  const record = (await db.collection("purchases").doc(purchaseId).get()).data();
  assert.equal(record?.uid, uid);
  assert.equal(record?.orderId, "GPA.1234");
  assert.equal(record?.consumed, true);
  assert.equal(calls.consume, 1);
});

test("前からある回数券の残りも合わせて返す（期限切れは数えない）", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({
    packs: { old: { remaining: 2, expiresAt: daysLater(now, 30) }, expired: { remaining: 3, expiresAt: daysLater(now, -1) } },
  });

  const response = await handleVerifyPurchase(uid, requestFor(randomUUID()), fakeDeps(now).deps);

  assert.equal(response.ticketsRemaining, 7);
});

test("同じ購入を2回送っても、足すのは1回だけ。2回目は Play に聞かずに足したときの内容を返す", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const token = randomUUID();
  const { deps, calls } = fakeDeps(now);

  const first = await handleVerifyPurchase(uid, requestFor(token), deps);
  const second = await handleVerifyPurchase(uid, requestFor(token), deps);

  assert.deepEqual(second, { ...first, alreadyGranted: true });
  assert.equal((await userOf(uid))?.packs[purchaseIdOf(token)].remaining, 5);
  assert.equal(calls.get, 1);
  assert.equal(calls.consume, 1);
});

test("同じ購入が同時に届いても、足すのは1回だけ", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const token = randomUUID();
  const { deps } = fakeDeps(now);

  const responses = await Promise.all([1, 2, 3].map(() => handleVerifyPurchase(uid, requestFor(token), deps)));

  assert.equal(responses.filter((response) => !response.alreadyGranted).length, 1);
  assert.deepEqual(
    responses.map((response) => response.ticketsRemaining),
    [5, 5, 5],
  );
});

test("ほかの利用者が受け取り済みの購入は owned_by_other で断り、回数は足さない", async () => {
  const now = uniqueDay();
  const token = randomUUID();
  const { deps } = fakeDeps(now);
  await handleVerifyPurchase(uniqueUid(), requestFor(token), deps);

  const other = uniqueUid();
  assert.equal(await rejectionReason(handleVerifyPurchase(other, requestFor(token), deps)), "owned_by_other");
  assert.equal(await userOf(other), undefined);
});

test("支払い待ちは pending、取り消された購入は not_purchased で断り、記録も残さない", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const pendingToken = randomUUID();
  const canceledToken = randomUUID();

  const pending = fakeDeps(now, { purchase: async () => ({ purchaseState: 2 }) }).deps;
  const canceled = fakeDeps(now, { purchase: async () => ({ purchaseState: 1 }) }).deps;

  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(pendingToken), pending)), "pending");
  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(canceledToken), canceled)), "not_purchased");
  assert.equal((await db.collection("purchases").doc(purchaseIdOf(pendingToken)).get()).exists, false);
  assert.equal(await userOf(uid), undefined);
});

test("Play が知らないトークンは not_purchased、権限がない・障害は verification_failed（やり直せる）", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const unknown = fakeDeps(now, { purchase: () => Promise.reject(new PlayHttpError(404, "NOT_FOUND")) }).deps;
  const denied = fakeDeps(now, { purchase: () => Promise.reject(new PlayHttpError(403, "PERMISSION_DENIED")) }).deps;
  const down = fakeDeps(now, { purchase: () => Promise.reject(new Error("timeout")) }).deps;

  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(randomUUID()), unknown)), "not_purchased");
  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(randomUUID()), denied)), "verification_failed");
  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(randomUUID()), down)), "verification_failed");
  assert.equal(await userOf(uid), undefined);
});

test("消費に失敗しても足した回数は残し、同じ購入がまた届いたら消費をやり直す", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const token = randomUUID();
  let consumeFails = true;
  const { deps, calls } = fakeDeps(now, {
    consume: async () => {
      if (consumeFails) throw new PlayHttpError(503);
    },
  });

  const first = await handleVerifyPurchase(uid, requestFor(token), deps);
  const purchaseRef = db.collection("purchases").doc(purchaseIdOf(token));
  assert.equal(first.ticketsAdded, 5);
  assert.equal((await purchaseRef.get()).get("consumed"), false);

  consumeFails = false;
  const second = await handleVerifyPurchase(uid, requestFor(token), deps);
  assert.equal(second.alreadyGranted, true);
  assert.equal((await purchaseRef.get()).get("consumed"), true);
  assert.equal(calls.consume, 2);
});

test("Play で消費済みの購入は、足すが消費は呼ばない", async () => {
  const now = uniqueDay();
  const { deps, calls } = fakeDeps(now, {
    purchase: async () => ({ purchaseState: 0, consumptionState: 1, purchaseTimeMillis: String(now.getTime()) }),
  });

  const response = await handleVerifyPurchase(uniqueUid(), requestFor(randomUUID()), deps);

  assert.equal(response.ticketsAdded, 5);
  assert.equal(calls.consume, 0);
});

test("返金で取り消し済みの購入は、同じ利用者が送り直しても not_purchased", async () => {
  const now = uniqueDay();
  const uid = uniqueUid();
  const token = randomUUID();
  const { deps } = fakeDeps(now);
  await handleVerifyPurchase(uid, requestFor(token), deps);
  await db.collection("purchases").doc(purchaseIdOf(token)).update({ voidedAt: daysLater(now, 1) });

  assert.equal(await rejectionReason(handleVerifyPurchase(uid, requestFor(token), deps)), "not_purchased");
});

test("ログインしていない・アイテムが違う・トークンがないときは Play に聞かずに断る", async () => {
  const now = uniqueDay();
  const { deps, calls } = fakeDeps(now);

  assert.equal(await rejectionReason(handleVerifyPurchase(undefined, requestFor("t"), deps)), "unauthenticated");
  const wrongProduct = { productId: "other_item", purchaseToken: "t" };
  assert.equal(await rejectionReason(handleVerifyPurchase(uniqueUid(), wrongProduct, deps)), "invalid_purchase");
  const noToken = { productId: TICKET_PRODUCT_ID, purchaseToken: "" };
  assert.equal(await rejectionReason(handleVerifyPurchase(uniqueUid(), noToken, deps)), "invalid_purchase");
  assert.equal(calls.get, 0);
});
