import assert from "node:assert/strict";
import { test } from "node:test";
import { addMonthsJst, jstDateKey } from "../src/jstDate";

test("日本時間の0時で日付が変わる", () => {
  assert.equal(jstDateKey(new Date("2026-10-16T14:59:59Z")), "2026-10-16");
  assert.equal(jstDateKey(new Date("2026-10-16T15:00:00Z")), "2026-10-17");
});

test("6か月後は日本時間の暦で数え、同じ日がない月は月末にする", () => {
  assert.equal(addMonthsJst(new Date("2026-11-04T01:00:00Z"), 6).toISOString(), "2027-05-04T01:00:00.000Z");
  // 日本時間の 8/31 5:00（UTC では 8/30）。UTC で数えると 3/1 になり、6か月を超える
  assert.equal(addMonthsJst(new Date("2026-08-30T20:00:00Z"), 6).toISOString(), "2027-02-27T20:00:00.000Z");
  assert.equal(addMonthsJst(new Date("2027-08-31T03:00:00Z"), 6).toISOString(), "2028-02-29T03:00:00.000Z");
});
