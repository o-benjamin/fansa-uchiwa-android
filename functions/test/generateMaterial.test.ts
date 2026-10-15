import assert from "node:assert/strict";
import { test } from "node:test";
import { HttpsError } from "firebase-functions/v2/https";
import type { GeminiClient, ImageResult } from "../src/gemini";
import { handleGenerateMaterial } from "../src/generateMaterial";
import type { GenerateMaterialDeps } from "../src/generateMaterial";
import { jstDateKey } from "../src/jstDate";
import type { JudgeCategory } from "../src/moderation";
import { DEFAULT_SETTINGS } from "../src/settings";
import type { AiMaterialSettings } from "../src/settings";
import { daysLater, emulatorDb, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();
const IMAGE: ImageResult = { kind: "image", mimeType: "image/png", data: "AAAA" };

interface FakeOptions {
  open?: boolean;
  settings?: Partial<AiMaterialSettings>;
  judge?: () => Promise<JudgeCategory | undefined>;
  image?: () => Promise<ImageResult>;
}

function fakeDeps(day: Date, options: FakeOptions = {}) {
  const calls = { judge: 0, image: 0, notifications: [] as string[] };
  const gemini: GeminiClient = {
    judge: async () => {
      calls.judge++;
      return options.judge ? options.judge() : "none";
    },
    generateImage: async () => {
      calls.image++;
      return options.image ? options.image() : IMAGE;
    },
  };
  const deps: GenerateMaterialDeps = {
    db,
    isEntryOpen: async () => options.open ?? true,
    loadSettings: async () => ({ ...DEFAULT_SETTINGS, ...options.settings }),
    gemini,
    notifyOwner: async (message) => {
      calls.notifications.push(message);
    },
    now: () => day,
  };
  return { deps, calls };
}

function rejectsWith(reason: string, code: string) {
  return (error: unknown) => {
    assert.ok(error instanceof HttpsError);
    assert.equal(error.code, code);
    assert.equal((error.details as { reason: string }).reason, reason);
    return true;
  };
}

async function freeUsed(uid: string) {
  return (await db.collection("users").doc(uid).get()).get("freeUsed");
}

test("無料の1回で画像を返す", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay());
  const response = await handleGenerateMaterial(uid, { prompt: " 王冠のうさぎ " }, deps);

  assert.deepEqual(response, { image: "AAAA", mimeType: "image/png", paidWith: "free", ticketsRemaining: 0 });
  assert.equal(calls.judge, 1);
  assert.equal(await freeUsed(uid), true);
});

test("回数券で作ったら、残りの回数を返す", async () => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  const { deps } = fakeDeps(day);

  const response = await handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps);
  assert.equal(response.paidWith, "ticket");
  assert.equal(response.ticketsRemaining, 4);
});

test("ログインしていなければ断る", async () => {
  const { deps } = fakeDeps(uniqueDay());
  await assert.rejects(handleGenerateMaterial(undefined, { prompt: "うさぎ" }, deps), rejectsWith("unauthenticated", "unauthenticated"));
});

test("文章が空・長すぎる、元画像の形が違うときは断る", async () => {
  const { deps, calls } = fakeDeps(uniqueDay());
  const uid = uniqueUid();
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "  " }, deps), rejectsWith("invalid_prompt", "invalid-argument"));
  await assert.rejects(handleGenerateMaterial(uid, {}, deps), rejectsWith("invalid_prompt", "invalid-argument"));
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "あ".repeat(201) }, deps),
    rejectsWith("invalid_prompt", "invalid-argument"),
  );
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "うさぎ", sourceImage: { mimeType: "image/gif", data: "AAAA" } }, deps),
    rejectsWith("invalid_image", "invalid-argument"),
  );
  const tooLarge = Buffer.alloc(4 * 1024 * 1024 + 1).toString("base64");
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "うさぎ", sourceImage: { mimeType: "image/png", data: tooLarge } }, deps),
    rejectsWith("invalid_image", "invalid-argument"),
  );
  assert.equal(calls.judge, 0);
  assert.equal(await freeUsed(uid), undefined);
});

test("200文字ちょうどと、元画像つきは通す", async () => {
  const { deps } = fakeDeps(uniqueDay());
  await handleGenerateMaterial(uniqueUid(), { prompt: "あ".repeat(200) }, deps);
  await handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ", sourceImage: { mimeType: "image/jpeg", data: "BBBB" } }, deps);
});

test("入口スイッチが閉じていたら、回数を使わずに断る", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay(), { open: false });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("disabled", "failed-precondition"));
  assert.equal(calls.judge, 0);
  assert.equal(await freeUsed(uid), undefined);
});

test("名前の一覧に当たったら、回数を使わず、審査のモデルも呼ばずに断る", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay(), { settings: { blockedNames: ["山田太郎"] } });
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "山田太郎の似顔絵" }, deps),
    rejectsWith("blocked_name", "permission-denied"),
  );
  assert.equal(calls.judge, 0);
  assert.equal(await freeUsed(uid), undefined);
});

test("審査で断ったら、回数を戻し、画像は作らない", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay(), { judge: async () => "real_person" });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "だれか" }, deps), (error: unknown) => {
    assert.ok(error instanceof HttpsError);
    assert.deepEqual(error.details, { reason: "moderation", category: "real_person" });
    return true;
  });
  assert.equal(calls.image, 0);
  assert.equal(await freeUsed(uid), false);
});

test("審査の答えを読めなければ、断って回数を戻す", async () => {
  const uid = uniqueUid();
  const { deps } = fakeDeps(uniqueDay(), { judge: async () => undefined });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("moderation", "permission-denied"));
  assert.equal(await freeUsed(uid), false);
});

test("安全フィルターで止まったら safety_blocked で断り、回数を戻す", async () => {
  const uid = uniqueUid();
  const { deps } = fakeDeps(uniqueDay(), { image: async () => ({ kind: "safety_blocked" }) });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("safety_blocked", "permission-denied"));
  assert.equal(await freeUsed(uid), false);
});

test("画像が返らない・API が失敗したら generation_failed で断り、回数を戻す", async () => {
  const day = uniqueDay();
  const first = uniqueUid();
  const { deps } = fakeDeps(day, { image: async () => ({ kind: "no_image" }) });
  await assert.rejects(handleGenerateMaterial(first, { prompt: "うさぎ" }, deps), rejectsWith("generation_failed", "unavailable"));
  assert.equal(await freeUsed(first), false);

  const second = uniqueUid();
  const throwing = fakeDeps(day, {
    image: async () => {
      throw new Error("timeout");
    },
  });
  await assert.rejects(
    handleGenerateMaterial(second, { prompt: "うさぎ" }, throwing.deps),
    rejectsWith("generation_failed", "unavailable"),
  );
  assert.equal(await freeUsed(second), false);
  assert.equal((await db.collection("spend").doc(jstDateKey(day)).get()).get("free"), 0);
});

test("無料を使い終えて回数券がなければ no_credits", async () => {
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({ freeUsed: true });
  const { deps, calls } = fakeDeps(uniqueDay());
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("no_credits", "resource-exhausted"));
  assert.equal(calls.judge, 0);
});

test("全体の安全上限に当たったら断り、オーナーへの知らせはその日1回だけ", async () => {
  const day = uniqueDay();
  await db.collection("spend").doc(jstDateKey(day)).set({ free: 0, paid: 3 });
  const { deps, calls } = fakeDeps(day, { settings: { totalDailyLimit: 3 } });

  await assert.rejects(
    handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ" }, deps),
    rejectsWith("total_daily_limit", "resource-exhausted"),
  );
  await assert.rejects(
    handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ" }, deps),
    rejectsWith("total_daily_limit", "resource-exhausted"),
  );
  assert.equal(calls.notifications.length, 1);
  assert.match(calls.notifications[0], /3回/);
});

test("知らせに失敗しても、断る理由は変わらない", async () => {
  const day = uniqueDay();
  await db.collection("spend").doc(jstDateKey(day)).set({ free: 0, paid: 1 });
  const { deps } = fakeDeps(day, { settings: { totalDailyLimit: 1 } });
  deps.notifyOwner = async () => {
    throw new Error("discord down");
  };
  await assert.rejects(
    handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ" }, deps),
    rejectsWith("total_daily_limit", "resource-exhausted"),
  );
});
