import assert from "node:assert/strict";
import { test } from "node:test";
import { HttpsError } from "firebase-functions/v2/https";
import { MAX_PROMPT_LENGTH } from "../src/generateMaterial";
import {
  handleReportMaterial,
  MAX_REPORTS_PER_DAY,
  REPORT_RETENTION_DAYS,
  REPORTS_COLLECTION,
} from "../src/reportMaterial";
import type { ReportMaterialDeps } from "../src/reportMaterial";
import { daysLater, emulatorDb, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();
const REPORT = { prompt: " 王冠のうさぎ ", reason: "inappropriate", model: "gemini-3.1-flash-lite-image" };

function fakeDeps(day: Date, notifyOwner?: (message: string) => Promise<void>) {
  const notifications: string[] = [];
  const deps: ReportMaterialDeps = {
    db,
    notifyOwner:
      notifyOwner ??
      (async (message) => {
        notifications.push(message);
      }),
    now: () => day,
  };
  return { deps, notifications };
}

function rejectsWith(reason: string, code: string) {
  return (error: unknown) => {
    assert.ok(error instanceof HttpsError);
    assert.equal(error.code, code);
    assert.equal((error.details as { reason: string }).reason, reason);
    return true;
  };
}

async function reportsOf(uid: string) {
  return (await db.collection(REPORTS_COLLECTION).where("uid", "==", uid).get()).docs.map((doc) => doc.data());
}

test("報告を残し、文章を書かずにオーナーへ知らせる", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const { deps, notifications } = fakeDeps(day);
  await handleReportMaterial(uid, REPORT, deps);

  const reports = await reportsOf(uid);
  assert.equal(reports.length, 1);
  assert.equal(reports[0].prompt, "王冠のうさぎ");
  assert.equal(reports[0].reason, "inappropriate");
  assert.equal(reports[0].model, "gemini-3.1-flash-lite-image");
  assert.equal(reports[0].createdAt.toMillis(), day.getTime());
  assert.equal(reports[0].expireAt.toMillis(), daysLater(day, REPORT_RETENTION_DAYS).toMillis());
  assert.equal(notifications.length, 1);
  assert.match(notifications[0], /不快・不適切/);
  assert.doesNotMatch(notifications[0], /うさぎ/);
});

test("ログインしていなければ断る", async () => {
  const { deps } = fakeDeps(uniqueDay());
  await assert.rejects(handleReportMaterial(undefined, REPORT, deps), rejectsWith("unauthenticated", "unauthenticated"));
});

test("文章・理由・モデル名の形が違えば断り、何も残さない", async () => {
  const uid = uniqueUid();
  const { deps, notifications } = fakeDeps(uniqueDay());
  const invalids = [
    { ...REPORT, prompt: "  " },
    { ...REPORT, prompt: "あ".repeat(MAX_PROMPT_LENGTH + 1) },
    { ...REPORT, reason: "boring" },
    { ...REPORT, model: "" },
    { ...REPORT, model: "<script>" },
    { prompt: REPORT.prompt, reason: REPORT.reason },
    null,
  ];
  for (const data of invalids) {
    await assert.rejects(handleReportMaterial(uid, data, deps), rejectsWith("invalid_report", "invalid-argument"));
  }
  assert.equal((await reportsOf(uid)).length, 0);
  assert.equal(notifications.length, 0);
});

test("1人1日の上限を超えたら断り、次の日はまた受け付ける", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const { deps, notifications } = fakeDeps(day);
  for (let i = 0; i < MAX_REPORTS_PER_DAY; i++) await handleReportMaterial(uid, REPORT, deps);

  await assert.rejects(handleReportMaterial(uid, REPORT, deps), rejectsWith("too_many_reports", "resource-exhausted"));
  assert.equal((await reportsOf(uid)).length, MAX_REPORTS_PER_DAY);
  assert.equal(notifications.length, MAX_REPORTS_PER_DAY);

  const nextDay = new Date(day.getTime() + 24 * 60 * 60 * 1000);
  await handleReportMaterial(uid, REPORT, fakeDeps(nextDay).deps);
  assert.equal((await reportsOf(uid)).length, MAX_REPORTS_PER_DAY + 1);
});

test("Discord に知らせられなくても、報告は受け付ける", async () => {
  const uid = uniqueUid();
  const { deps } = fakeDeps(uniqueDay(), async () => {
    throw new Error("Discord が 500 を返した");
  });
  await handleReportMaterial(uid, REPORT, deps);
  assert.equal((await reportsOf(uid)).length, 1);
});
