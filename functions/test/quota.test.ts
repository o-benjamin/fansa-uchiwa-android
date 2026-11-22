import assert from "node:assert/strict";
import { test } from "node:test";
import {
  REDO_WINDOW_MS,
  refundCredit,
  reserveCredit,
  reserveCreditForRequest,
  STALE_CALL_MS,
  updateRequestState,
} from "../src/quota";
import { jstDateKey } from "../src/jstDate";
import { daysLater, emulatorDb, LIMITS, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();

async function usageOf(day: Date) {
  return (await db.collection("dailyUsage").doc(jstDateKey(day)).get()).data() ?? {};
}

async function userOf(uid: string) {
  return (await db.collection("users").doc(uid).get()).data() ?? {};
}

function msLater(base: Date, ms: number): Date {
  return new Date(base.getTime() + ms);
}

/** 回数券5回を持ち、無料の1回は使い終えた利用者 */
async function ticketHolder(day: Date): Promise<string> {
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  return uid;
}

test("無料は1人1回だけ。2回目は回数券がなければ no_credits", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();

  const first = await reserveCredit(db, uid, LIMITS, day);
  assert.deepEqual(first, { ok: true, reservation: { kind: "free", dateKey: jstDateKey(day) }, ticketsRemaining: 0 });

  const nextDay = new Date(day.getTime() + 24 * 60 * 60 * 1000);
  const second = await reserveCredit(db, uid, LIMITS, nextDay);
  assert.deepEqual(second, { ok: false, reason: "no_credits", firstTotalLimitHit: false });
  assert.equal((await db.collection("users").doc(uid).get()).get("freeUsed"), true);
  assert.equal((await usageOf(day)).free, 1);
});

test("無料の非常ブレーキに当たったら、回数券がない人には free_daily_limit", async () => {
  const day = uniqueDay();
  const limits = { freeDailyLimit: 2, totalDailyLimit: 300 };
  assert.equal((await reserveCredit(db, uniqueUid(), limits, day)).ok, true);
  assert.equal((await reserveCredit(db, uniqueUid(), limits, day)).ok, true);

  const third = await reserveCredit(db, uniqueUid(), limits, day);
  assert.deepEqual(third, { ok: false, reason: "free_daily_limit", firstTotalLimitHit: false });
});

test("ブレーキ中でも、回数券があれば回数券で作れる（無料の1回は残る）", async () => {
  const day = uniqueDay();
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({ packs: { "GPA.1111-2222": { remaining: 5, expiresAt: daysLater(day, 180) } } });

  const outcome = await reserveCredit(db, uid, { freeDailyLimit: 0, totalDailyLimit: 300 }, day);
  assert.deepEqual(outcome, {
    ok: true,
    reservation: { kind: "ticket", dateKey: jstDateKey(day), packId: "GPA.1111-2222" },
    ticketsRemaining: 4,
  });
  const user = (await db.collection("users").doc(uid).get()).data();
  assert.equal(user?.freeUsed, undefined);
  assert.equal(user?.packs["GPA.1111-2222"].remaining, 4);
  assert.equal((await usageOf(day)).ticket, 1);
});

test("回数券は期限の近いものから使い、期限切れと使い切ったものは使わない", async () => {
  const day = uniqueDay();
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({
    freeUsed: true,
    packs: {
      expired: { remaining: 5, expiresAt: daysLater(day, -1) },
      empty: { remaining: 0, expiresAt: daysLater(day, 10) },
      later: { remaining: 5, expiresAt: daysLater(day, 100) },
      sooner: { remaining: 2, expiresAt: daysLater(day, 20) },
    },
  });

  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.deepEqual(outcome, {
    ok: true,
    reservation: { kind: "ticket", dateKey: jstDateKey(day), packId: "sooner" },
    ticketsRemaining: 6,
  });
});

test("全体の安全上限に当たったら、無料も回数券も止め、知らせるのはその日の最初の1回だけ", async () => {
  const day = uniqueDay();
  const limits = { freeDailyLimit: 30, totalDailyLimit: 2 };
  await db.collection("dailyUsage").doc(jstDateKey(day)).set({ free: 1, ticket: 1 });
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({ packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });

  assert.deepEqual(await reserveCredit(db, uid, limits, day), {
    ok: false,
    reason: "total_daily_limit",
    firstTotalLimitHit: true,
  });
  assert.deepEqual(await reserveCredit(db, uniqueUid(), limits, day), {
    ok: false,
    reason: "total_daily_limit",
    firstTotalLimitHit: false,
  });
});

test("同じ人が同時に何回呼んでも、無料は1回しか使えない", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const outcomes = await Promise.all(Array.from({ length: 5 }, () => reserveCredit(db, uid, LIMITS, day)));

  assert.equal(outcomes.filter((outcome) => outcome.ok).length, 1);
  assert.equal((await usageOf(day)).free, 1);
});

test("無料の1回を戻すと、もう一度無料で作れる", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.ok(outcome.ok);

  assert.equal(await refundCredit(db, uid, outcome.reservation), true);
  assert.equal((await db.collection("users").doc(uid).get()).get("freeUsed"), false);
  assert.equal((await usageOf(day)).free, 0);
  assert.equal((await reserveCredit(db, uid, LIMITS, day)).ok, true);
});

test("回数券の1回を戻すと、同じ回数券の残りに戻る（注文番号に . があっても）", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({
    freeUsed: true,
    packs: { "GPA.3333-4444-5555": { remaining: 1, expiresAt: daysLater(day, 180) } },
  });
  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.ok(outcome.ok);

  assert.equal(await refundCredit(db, uid, outcome.reservation), true);
  const user = (await db.collection("users").doc(uid).get()).data();
  assert.equal(user?.packs["GPA.3333-4444-5555"].remaining, 1);
  assert.equal((await usageOf(day)).ticket, 0);
});

test("戻す前に回数券が消えていたら（返金の取り消しなど）、回数券は作り直さない", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 1, expiresAt: daysLater(day, 180) } } });
  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.ok(outcome.ok);
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: {} });

  assert.equal(await refundCredit(db, uid, outcome.reservation), false);
  assert.deepEqual((await db.collection("users").doc(uid).get()).get("packs"), {});
  assert.equal((await usageOf(day)).ticket, 0);
});

test("頼みの番号つきでおさえると、ふつうにおさえて、頼みを generating で覚える", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  const outcome = await reserveCreditForRequest(db, uid, LIMITS, day, "r1");

  assert.deepEqual(outcome, {
    ok: true,
    reservation: { kind: "ticket", dateKey: jstDateKey(day), packId: "p" },
    ticketsRemaining: 4,
  });
  const { lastRequest } = await userOf(uid);
  assert.equal(lastRequest.id, "r1");
  assert.equal(lastRequest.state, "generating");
  assert.equal(lastRequest.paidWith, "ticket");
  assert.equal(lastRequest.reservedAt.toMillis(), day.getTime());
  assert.equal(lastRequest.updatedAt.toMillis(), day.getTime());
});

test("同じ番号の前の頼みを作っている途中なら、回数を使わずに in_progress で断る", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");

  const outcome = await reserveCreditForRequest(db, uid, LIMITS, msLater(day, STALE_CALL_MS - 1), "r1");
  assert.deepEqual(outcome, { ok: false, reason: "in_progress", firstTotalLimitHit: false });
  assert.equal((await userOf(uid)).packs.p.remaining, 4);
  assert.equal((await usageOf(day)).ticket, 1);
});

test("同じ番号の前の頼みが作れていたら、15分以内は回数も全体の回数も使わずに作り直し、作り直しは1回まで", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");
  await updateRequestState(db, uid, "r1", "generating", "done", day);

  const redoAt = msLater(day, REDO_WINDOW_MS);
  const redo = await reserveCreditForRequest(db, uid, LIMITS, redoAt, "r1");
  assert.deepEqual(redo, { ok: true, redo: true, paidWith: "ticket", ticketsRemaining: 4 });
  assert.equal((await userOf(uid)).lastRequest.state, "redoing");
  assert.equal((await userOf(uid)).packs.p.remaining, 4);
  assert.equal((await usageOf(day)).ticket, 1);

  await updateRequestState(db, uid, "r1", "redoing", "redone", redoAt);
  const again = await reserveCreditForRequest(db, uid, LIMITS, msLater(redoAt, 1), "r1");
  assert.equal(again.ok && !("redo" in again), true);
  assert.equal((await userOf(uid)).packs.p.remaining, 3);
  assert.equal((await userOf(uid)).lastRequest.state, "generating");
});

test("作り直しに失敗して done に戻ったら、15分以内はもう一度作り直せる", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");
  await updateRequestState(db, uid, "r1", "generating", "done", day);
  await reserveCreditForRequest(db, uid, LIMITS, msLater(day, 1000), "r1");
  await updateRequestState(db, uid, "r1", "redoing", "done", msLater(day, 2000));

  const outcome = await reserveCreditForRequest(db, uid, LIMITS, msLater(day, 3000), "r1");
  assert.deepEqual(outcome, { ok: true, redo: true, paidWith: "ticket", ticketsRemaining: 4 });
});

test("15分を過ぎた・番号が違う・前の頼みは回数を戻したときは、作り直さずにふつうに回数を使う", async () => {
  const day = uniqueDay();

  const late = await ticketHolder(day);
  await reserveCreditForRequest(db, late, LIMITS, day, "r1");
  await updateRequestState(db, late, "r1", "generating", "done", day);
  const lateOutcome = await reserveCreditForRequest(db, late, LIMITS, msLater(day, REDO_WINDOW_MS + 1), "r1");
  assert.equal(lateOutcome.ok && !("redo" in lateOutcome), true);
  assert.equal((await userOf(late)).packs.p.remaining, 3);

  const other = await ticketHolder(day);
  await reserveCreditForRequest(db, other, LIMITS, day, "r1");
  await updateRequestState(db, other, "r1", "generating", "done", day);
  const otherOutcome = await reserveCreditForRequest(db, other, LIMITS, msLater(day, 1000), "r2");
  assert.equal(otherOutcome.ok && !("redo" in otherOutcome), true);
  assert.equal((await userOf(other)).lastRequest.id, "r2");

  const refunded = await ticketHolder(day);
  const first = await reserveCreditForRequest(db, refunded, LIMITS, day, "r1");
  assert.ok(first.ok && "reservation" in first);
  assert.equal(await refundCredit(db, refunded, first.reservation, "r1"), true);
  assert.equal((await userOf(refunded)).lastRequest, undefined);
  const afterRefund = await reserveCreditForRequest(db, refunded, LIMITS, msLater(day, 1000), "r1");
  assert.equal(afterRefund.ok && !("redo" in afterRefund), true);
  assert.equal((await userOf(refunded)).packs.p.remaining, 4);
});

test("generating・redoing のまま3分たったら止まった呼び出しとみなし、15分以内なら作り直せる", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");

  const redoAt = msLater(day, STALE_CALL_MS);
  assert.deepEqual(await reserveCreditForRequest(db, uid, LIMITS, redoAt, "r1"), {
    ok: true,
    redo: true,
    paidWith: "ticket",
    ticketsRemaining: 4,
  });
  assert.deepEqual(await reserveCreditForRequest(db, uid, LIMITS, msLater(redoAt, STALE_CALL_MS - 1), "r1"), {
    ok: false,
    reason: "in_progress",
    firstTotalLimitHit: false,
  });
  assert.deepEqual(await reserveCreditForRequest(db, uid, LIMITS, msLater(redoAt, STALE_CALL_MS), "r1"), {
    ok: true,
    redo: true,
    paidWith: "ticket",
    ticketsRemaining: 4,
  });
});

test("作り直しは、全体の安全上限に当たっていても止めない（回数を使った頼みの分なので）", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");
  await updateRequestState(db, uid, "r1", "generating", "done", day);
  await db.collection("dailyUsage").doc(jstDateKey(day)).set({ ticket: LIMITS.totalDailyLimit });

  const outcome = await reserveCreditForRequest(db, uid, LIMITS, msLater(day, 1000), "r1");
  assert.deepEqual(outcome, { ok: true, redo: true, paidWith: "ticket", ticketsRemaining: 4 });
});

test("頼みの状態は、番号と今の状態が合うときだけ進める", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await reserveCreditForRequest(db, uid, LIMITS, day, "r1");

  await updateRequestState(db, uid, "r2", "generating", "done", day);
  await updateRequestState(db, uid, "r1", "redoing", "redone", day);
  assert.equal((await userOf(uid)).lastRequest.state, "generating");
  await updateRequestState(db, uid, "r1", "generating", "done", day);
  assert.equal((await userOf(uid)).lastRequest.state, "done");
});

test("回数を戻すとき、番号が違う頼みは消さない", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  const outcome = await reserveCreditForRequest(db, uid, LIMITS, day, "r1");
  assert.ok(outcome.ok && "reservation" in outcome);

  await refundCredit(db, uid, outcome.reservation, "r2");
  assert.equal((await userOf(uid)).lastRequest.id, "r1");
});
