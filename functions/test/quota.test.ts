import assert from "node:assert/strict";
import { test } from "node:test";
import { refundCredit, reserveCredit } from "../src/quota";
import { jstDateKey } from "../src/jstDate";
import { daysLater, emulatorDb, LIMITS, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();

async function spendOf(day: Date) {
  return (await db.collection("spend").doc(jstDateKey(day)).get()).data() ?? {};
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
  assert.equal((await spendOf(day)).free, 1);
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
  assert.equal((await spendOf(day)).paid, 1);
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
  await db.collection("spend").doc(jstDateKey(day)).set({ free: 1, paid: 1 });
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
  assert.equal((await spendOf(day)).free, 1);
});

test("無料の1回を戻すと、もう一度無料で作れる", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.ok(outcome.ok);

  await refundCredit(db, uid, outcome.reservation);
  assert.equal((await db.collection("users").doc(uid).get()).get("freeUsed"), false);
  assert.equal((await spendOf(day)).free, 0);
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

  await refundCredit(db, uid, outcome.reservation);
  const user = (await db.collection("users").doc(uid).get()).data();
  assert.equal(user?.packs["GPA.3333-4444-5555"].remaining, 1);
  assert.equal((await spendOf(day)).paid, 0);
});

test("戻す前に回数券が消えていたら（返金の取り消しなど）、回数券は作り直さない", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 1, expiresAt: daysLater(day, 180) } } });
  const outcome = await reserveCredit(db, uid, LIMITS, day);
  assert.ok(outcome.ok);
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: {} });

  await refundCredit(db, uid, outcome.reservation);
  assert.deepEqual((await db.collection("users").doc(uid).get()).get("packs"), {});
  assert.equal((await spendOf(day)).paid, 0);
});
