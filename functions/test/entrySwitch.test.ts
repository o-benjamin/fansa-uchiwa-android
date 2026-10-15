import assert from "node:assert/strict";
import { test } from "node:test";
import type { RemoteConfigTemplate } from "firebase-admin/remote-config";
import { createEntrySwitch, isEntryOpen } from "../src/entrySwitch";

function template(parameters: object): RemoteConfigTemplate {
  return { parameters, conditions: [], parameterGroups: {}, etag: "etag" } as unknown as RemoteConfigTemplate;
}

test("パラメータがなければ閉じている", () => {
  assert.equal(isEntryOpen(template({})), false);
});

test("初期値が true なら開いている。false なら閉じている", () => {
  assert.equal(isEntryOpen(template({ ai_material_enabled: { defaultValue: { value: "true" } } })), true);
  assert.equal(isEntryOpen(template({ ai_material_enabled: { defaultValue: { value: "false" } } })), false);
  assert.equal(isEntryOpen(template({ ai_material_enabled: { defaultValue: { useInAppDefault: true } } })), false);
});

test("どれか1つの条件の値が true なら開いている", () => {
  const parameter = {
    defaultValue: { value: "false" },
    conditionalValues: { testers: { value: "true" } },
  };
  assert.equal(isEntryOpen(template({ ai_material_enabled: parameter })), true);
});

test("1分は覚えておき、読めなければ前回の値を使う", async () => {
  let now = 0;
  let calls = 0;
  let fail = false;
  const open = createEntrySwitch(
    async () => {
      calls++;
      if (fail) throw new Error("network");
      return template({ ai_material_enabled: { defaultValue: { value: "true" } } });
    },
    60_000,
    () => now,
  );
  assert.equal(await open(), true);
  now = 59_000;
  assert.equal(await open(), true);
  assert.equal(calls, 1);
  now = 61_000;
  fail = true;
  assert.equal(await open(), true);
  assert.equal(calls, 2);
});

test("一度も読めていなければ閉じているとみなす", async () => {
  const open = createEntrySwitch(async () => {
    throw new Error("network");
  });
  assert.equal(await open(), false);
});
