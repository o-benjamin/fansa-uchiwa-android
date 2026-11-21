import assert from "node:assert/strict";
import { test } from "node:test";
import type { Firestore } from "firebase-admin/firestore";
import { createSettingsLoader, DEFAULT_SETTINGS, parseSettings } from "../src/settings";

/** `config/aiMaterial` を読むたびに `read` を呼ぶ、Firestore の偽物。 */
function fakeDb(read: () => Record<string, unknown>) {
  return { doc: () => ({ get: async () => ({ data: read }) }) } as unknown as Firestore;
}

test("設定のドキュメントがなければ初期値", () => {
  assert.deepEqual(parseSettings(undefined), DEFAULT_SETTINGS);
});

test("書いた値を使い、型の合わない値は初期値に戻す", () => {
  const settings = parseSettings({
    freeDailyLimit: 0,
    totalDailyLimit: "300",
    imageModel: "gemini-3.1-flash-image",
    judgeModel: " ",
    blockedNames: ["名前A", 1, "", "Group B"],
  });
  assert.equal(settings.freeDailyLimit, 0);
  assert.equal(settings.totalDailyLimit, DEFAULT_SETTINGS.totalDailyLimit);
  assert.equal(settings.imageModel, "gemini-3.1-flash-image");
  assert.equal(settings.judgeModel, DEFAULT_SETTINGS.judgeModel);
  assert.deepEqual(settings.blockedNames, ["名前A", "Group B"]);
});

test("回数の上限に負の数や小数は使わない", () => {
  const settings = parseSettings({ freeDailyLimit: -1, totalDailyLimit: 1.5 });
  assert.equal(settings.freeDailyLimit, DEFAULT_SETTINGS.freeDailyLimit);
  assert.equal(settings.totalDailyLimit, DEFAULT_SETTINGS.totalDailyLimit);
});

test("1分は覚えておき、読めなければ前回の値を使い、1分は読み直さない", async () => {
  let now = 0;
  let reads = 0;
  let fail = false;
  const load = createSettingsLoader(
    fakeDb(() => {
      reads++;
      if (fail) throw new Error("unavailable");
      return { blockedNames: ["名前A"] };
    }),
    60_000,
    () => now,
  );
  assert.deepEqual((await load()).blockedNames, ["名前A"]);
  now = 61_000;
  fail = true;
  assert.deepEqual((await load()).blockedNames, ["名前A"]);
  assert.equal(reads, 2);
  now = 62_000;
  await load();
  assert.equal(reads, 2);
});

test("一度も読めていなければ、初期値（名前の一覧が空）で動かさずに例外を投げる", async () => {
  const load = createSettingsLoader(
    fakeDb(() => {
      throw new Error("unavailable");
    }),
  );
  await assert.rejects(load(), /unavailable/);
});
