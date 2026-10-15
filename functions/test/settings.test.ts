import assert from "node:assert/strict";
import { test } from "node:test";
import { DEFAULT_SETTINGS, parseSettings } from "../src/settings";

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
