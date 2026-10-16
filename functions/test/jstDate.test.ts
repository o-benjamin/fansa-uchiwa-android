import assert from "node:assert/strict";
import { test } from "node:test";
import { jstDateKey } from "../src/jstDate";

test("日本時間の0時で日付が変わる", () => {
  assert.equal(jstDateKey(new Date("2026-10-16T14:59:59Z")), "2026-10-16");
  assert.equal(jstDateKey(new Date("2026-10-16T15:00:00Z")), "2026-10-17");
});
