import assert from "node:assert/strict";
import { test } from "node:test";
import { findBlockedName, parseJudgeAnswer } from "../src/moderation";

const NAMES = ["山田太郎", "ABC", "Star Light"];

test("全角・半角、大文字・小文字、空白の違いがあっても名前に当てる", () => {
  assert.equal(findBlockedName("山田 太郎のうちわ", NAMES), "山田太郎");
  assert.equal(findBlockedName("ＡＢＣのロゴ", NAMES), "ABC");
  assert.equal(findBlockedName("abc推し", NAMES), "ABC");
  assert.equal(findBlockedName("STARLIGHTの星", NAMES), "Star Light");
});

test("英字の名前は、英文の中でも当て、ほかの英単語の一部には当てない", () => {
  assert.equal(findBlockedName("I love ABC", NAMES), "ABC");
  assert.equal(findBlockedName("star-light のうちわ", NAMES), "Star Light");
  assert.equal(findBlockedName("abcdefと書いたふきだし", NAMES), undefined);
  assert.equal(findBlockedName("xabc", NAMES), undefined);
  assert.equal(findBlockedName("xabc と ABC", NAMES), "ABC");
  assert.equal(findBlockedName("King & Prince", ["King & Prince"]), "King & Prince");
});

test("一覧になければ通す", () => {
  assert.equal(findBlockedName("王冠をかぶったうさぎ", NAMES), undefined);
  assert.equal(findBlockedName("うさぎ", []), undefined);
});

test("審査のモデルの答えを読む", () => {
  assert.equal(parseJudgeAnswer('{"category":"none"}'), "none");
  assert.equal(parseJudgeAnswer('{"category":"real_person"}'), "real_person");
  assert.equal(parseJudgeAnswer('{"category":"other"}'), undefined);
  assert.equal(parseJudgeAnswer("読めない"), undefined);
  assert.equal(parseJudgeAnswer(""), undefined);
});
