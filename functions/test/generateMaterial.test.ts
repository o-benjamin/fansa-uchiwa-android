import assert from "node:assert/strict";
import { test } from "node:test";
import type { TestContext } from "node:test";
import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import type { GeminiClient, ImageResult, JudgeResult, SourceImage } from "../src/gemini";
import { handleGenerateMaterial, MAX_PROMPT_LENGTH, MAX_SOURCE_IMAGE_BYTES } from "../src/generateMaterial";
import type { GenerateMaterialDeps } from "../src/generateMaterial";
import { jstDateKey } from "../src/jstDate";
import { DEFAULT_SETTINGS } from "../src/settings";
import type { AiMaterialSettings } from "../src/settings";
import { daysLater, emulatorDb, rejectsWith, uniqueDay, uniqueUid } from "./helpers";

const db = emulatorDb();
const IMAGE: ImageResult = { kind: "image", mimeType: "image/png", data: "AAAA" };

interface FakeOptions {
  open?: boolean;
  settings?: Partial<AiMaterialSettings>;
  judge?: () => Promise<JudgeResult>;
  image?: () => Promise<ImageResult>;
  loadSettings?: () => Promise<AiMaterialSettings>;
}

function fakeDeps(day: Date, options: FakeOptions = {}) {
  const calls = { judge: 0, image: 0, judgedImages: [] as (SourceImage | undefined)[], notifications: [] as string[] };
  const gemini: GeminiClient = {
    judge: async (_model, _prompt, sourceImage) => {
      calls.judge++;
      calls.judgedImages.push(sourceImage);
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
    loadSettings: options.loadSettings ?? (async () => ({ ...DEFAULT_SETTINGS, ...options.settings })),
    gemini,
    notifyOwner: async (message) => {
      calls.notifications.push(message);
    },
    now: () => day,
  };
  return { deps, calls };
}

async function freeUsed(uid: string) {
  return (await db.collection("users").doc(uid).get()).get("freeUsed");
}

/** `logger.info` に書いた [メッセージ, 中身] を集める（テストが終わると元に戻る） */
function captureInfoLogs(t: TestContext) {
  const info = t.mock.method(logger, "info", () => {});
  return () => info.mock.calls.map((call) => call.arguments as [string, Record<string, unknown>]);
}

test("無料の1回で画像を返す", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay());
  const response = await handleGenerateMaterial(uid, { prompt: " 王冠のうさぎ " }, deps);

  assert.deepEqual(response, {
    image: "AAAA",
    mimeType: "image/png",
    paidWith: "free",
    ticketsRemaining: 0,
    model: DEFAULT_SETTINGS.imageModel,
  });
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
    handleGenerateMaterial(uid, { prompt: "あ".repeat(MAX_PROMPT_LENGTH + 1) }, deps),
    rejectsWith("invalid_prompt", "invalid-argument"),
  );
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "うさぎ", sourceImage: { mimeType: "image/gif", data: "AAAA" } }, deps),
    rejectsWith("invalid_image", "invalid-argument"),
  );
  const tooLarge = Buffer.alloc(MAX_SOURCE_IMAGE_BYTES + 1).toString("base64");
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "うさぎ", sourceImage: { mimeType: "image/png", data: tooLarge } }, deps),
    rejectsWith("invalid_image", "invalid-argument"),
  );
  assert.equal(calls.judge, 0);
  assert.equal(await freeUsed(uid), undefined);
});

test("上限ちょうどの文字数と、元画像つきは通す。元画像は審査にも渡す", async () => {
  const { deps, calls } = fakeDeps(uniqueDay());
  await handleGenerateMaterial(uniqueUid(), { prompt: "あ".repeat(MAX_PROMPT_LENGTH) }, deps);
  await handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ", sourceImage: { mimeType: "image/jpeg", data: "BBBB" } }, deps);
  assert.deepEqual(calls.judgedImages, [undefined, { mimeType: "image/jpeg", data: "BBBB" }]);
});

test("設定を読めなければ、回数を使わずに generation_failed で断る", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay(), {
    loadSettings: async () => {
      throw new Error("firestore unavailable");
    },
  });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("generation_failed", "unavailable"));
  assert.equal(calls.judge, 0);
  assert.equal(await freeUsed(uid), undefined);
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

test("審査の答えを読めない・審査のモデルを呼べなければ、画像を作らずに generation_failed で断り、回数を戻す", async () => {
  const day = uniqueDay();
  const unreadable = fakeDeps(day, { judge: async () => undefined });
  const first = uniqueUid();
  await assert.rejects(
    handleGenerateMaterial(first, { prompt: "うさぎ" }, unreadable.deps),
    rejectsWith("generation_failed", "unavailable"),
  );
  assert.equal(unreadable.calls.image, 0);
  assert.equal(await freeUsed(first), false);

  const throwing = fakeDeps(day, {
    judge: async () => {
      throw new Error("timeout");
    },
  });
  const second = uniqueUid();
  await assert.rejects(
    handleGenerateMaterial(second, { prompt: "うさぎ" }, throwing.deps),
    rejectsWith("generation_failed", "unavailable"),
  );
  assert.equal(throwing.calls.image, 0);
  assert.equal(await freeUsed(second), false);
});

test("審査に送った内容が安全フィルターで止められたら、画像を作らずに safety_blocked で断り、回数を戻す", async () => {
  const uid = uniqueUid();
  const { deps, calls } = fakeDeps(uniqueDay(), { judge: async () => "safety_blocked" });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("safety_blocked", "permission-denied"));
  assert.equal(calls.image, 0);
  assert.equal(await freeUsed(uid), false);
});

test("安全フィルターで止まったら safety_blocked で断り、回数を戻す", async () => {
  const uid = uniqueUid();
  const { deps } = fakeDeps(uniqueDay(), { image: async () => ({ kind: "safety_blocked", finishReason: "IMAGE_SAFETY" }) });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps), rejectsWith("safety_blocked", "permission-denied"));
  assert.equal(await freeUsed(uid), false);
});

test("画像が返らない・API が失敗したら generation_failed で断り、回数を戻す", async () => {
  const day = uniqueDay();
  const first = uniqueUid();
  const { deps } = fakeDeps(day, { image: async () => ({ kind: "no_image", finishReason: "STOP" }) });
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
  assert.equal((await db.collection("dailyUsage").doc(jstDateKey(day)).get()).get("free"), 0);
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
  await db.collection("dailyUsage").doc(jstDateKey(day)).set({ free: 0, ticket: 3 });
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
  await db.collection("dailyUsage").doc(jstDateKey(day)).set({ free: 0, ticket: 1 });
  const { deps } = fakeDeps(day, { settings: { totalDailyLimit: 1 } });
  deps.notifyOwner = async () => {
    throw new Error("discord down");
  };
  await assert.rejects(
    handleGenerateMaterial(uniqueUid(), { prompt: "うさぎ" }, deps),
    rejectsWith("total_daily_limit", "resource-exhausted"),
  );
});

test("作れたら、おさえたときと作れたときのログに uid・callId・reservation を書く（問い合わせから回数を戻すため）", async (t) => {
  const uid = uniqueUid();
  const day = uniqueDay();
  const logs = captureInfoLogs(t);
  const { deps } = fakeDeps(day);
  await handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps);

  const reservation = { kind: "free", dateKey: jstDateKey(day) };
  const [reserved, generated] = logs();
  const callId = reserved[1].callId;
  assert.equal(typeof callId, "string");
  assert.deepEqual(reserved, ["回数をおさえた", { uid, callId, reservation }]);
  const [message, payload] = generated;
  assert.equal(message, "generateMaterial");
  assert.equal(payload.result, "ok");
  assert.equal(payload.uid, uid);
  assert.equal(payload.callId, callId);
  assert.deepEqual(payload.reservation, reservation);
});

test("呼び出しごとに callId が変わる（同じ日に同じ回数券で作っても、ログを1回ずつ組にできる）", async (t) => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  const logs = captureInfoLogs(t);
  const { deps } = fakeDeps(day);
  await handleGenerateMaterial(uid, { prompt: "うさぎ" }, deps);
  await handleGenerateMaterial(uid, { prompt: "ねこ" }, deps);

  const reserved = logs().filter(([message]) => message === "回数をおさえた");
  assert.equal(reserved.length, 2);
  assert.deepEqual(reserved[0][1].reservation, reserved[1][1].reservation);
  assert.notEqual(reserved[0][1].callId, reserved[1][1].callId);
});

test("おさえたあとに断って回数を戻したら、戻したことを uid・callId・reservation つきでログに書く", async (t) => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  const logs = captureInfoLogs(t);
  const { deps } = fakeDeps(day, { judge: async () => "real_person" });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "だれか" }, deps), rejectsWith("moderation", "permission-denied"));

  const reservation = { kind: "ticket", dateKey: jstDateKey(day), packId: "p" };
  const entries = logs();
  const callId = entries[0][1].callId;
  assert.deepEqual(entries, [
    ["回数をおさえた", { uid, callId, reservation }],
    ["回数を戻した", { uid, callId, reservation }],
    ["generateMaterial", { result: "moderation", category: "real_person" }],
  ]);
});

test("戻す前に回数券が取り消されていたら、戻さなかったことをログに書く", async (t) => {
  const uid = uniqueUid();
  const day = uniqueDay();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  const logs = captureInfoLogs(t);
  const { deps } = fakeDeps(day, {
    judge: async () => {
      // 審査のあいだに返金で回数券が取り消された
      await db.collection("users").doc(uid).set({ freeUsed: true, packs: {} });
      return "real_person";
    },
  });
  await assert.rejects(handleGenerateMaterial(uid, { prompt: "だれか" }, deps), rejectsWith("moderation", "permission-denied"));

  const reservation = { kind: "ticket", dateKey: jstDateKey(day), packId: "p" };
  const entries = logs();
  const callId = entries[0][1].callId;
  assert.deepEqual(entries, [
    ["回数をおさえた", { uid, callId, reservation }],
    ["回数券が消えていて戻さなかった", { uid, callId, reservation }],
    ["generateMaterial", { result: "moderation", category: "real_person" }],
  ]);
});

async function lastRequestOf(uid: string) {
  return (await db.collection("users").doc(uid).get()).get("lastRequest");
}

async function ticketsOf(uid: string) {
  return (await db.collection("users").doc(uid).get()).get("packs").p.remaining;
}

async function ticketHolder(day: Date): Promise<string> {
  const uid = uniqueUid();
  await db.collection("users").doc(uid).set({ freeUsed: true, packs: { p: { remaining: 5, expiresAt: daysLater(day, 180) } } });
  return uid;
}

function secondsLater(base: Date, seconds: number): Date {
  return new Date(base.getTime() + seconds * 1000);
}

test("頼みの番号つきで作れたら done で覚え、同じ番号でもう一度来たら回数を使わずに作り直す", async (t) => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  const logs = captureInfoLogs(t);
  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(day).deps);
  assert.equal((await lastRequestOf(uid)).state, "done");

  const { deps, calls } = fakeDeps(secondsLater(day, 60));
  const response = await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, deps);
  assert.deepEqual(response, {
    image: "AAAA",
    mimeType: "image/png",
    paidWith: "ticket",
    ticketsRemaining: 4,
    model: DEFAULT_SETTINGS.imageModel,
  });
  assert.equal(calls.judge, 1);
  assert.equal(await ticketsOf(uid), 4);
  assert.equal((await lastRequestOf(uid)).state, "redone");

  const entries = logs();
  const redoLog = entries.find(([message]) => message === "回数を使わずに作り直す");
  assert.ok(redoLog);
  assert.equal(redoLog[1].requestId, "r1");
  assert.equal(redoLog[1].paidWith, "ticket");
  const okLogs = entries.filter(([message, payload]) => message === "generateMaterial" && payload.result === "ok");
  assert.equal(okLogs.length, 2);
  assert.equal(okLogs[0][1].requestId, "r1");
  assert.equal(okLogs[1][1].redo, true);
  assert.notEqual(okLogs[0][1].callId, okLogs[1][1].callId);
});

test("作り直しも1回まで。作り直したあとに同じ番号で来たら、ふつうに回数を使う", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(day).deps);
  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(secondsLater(day, 60)).deps);

  const response = await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(secondsLater(day, 120)).deps);
  assert.equal(response.ticketsRemaining, 3);
  assert.equal(await ticketsOf(uid), 3);
});

test("頼みの番号つきで断って回数を戻したら頼みを消し、同じ番号でもう一度来たらふつうに回数を使う", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "だれか", requestId: "r1" }, fakeDeps(day, { judge: async () => "real_person" }).deps),
    rejectsWith("moderation", "permission-denied"),
  );
  assert.equal(await lastRequestOf(uid), undefined);
  assert.equal(await ticketsOf(uid), 5);

  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(secondsLater(day, 60)).deps);
  assert.equal(await ticketsOf(uid), 4);
});

test("作り直しに失敗したら回数は動かさずに断り、15分以内ならもう一度作り直せる", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(day).deps);

  const failing = fakeDeps(secondsLater(day, 60), { image: async () => ({ kind: "no_image", finishReason: "STOP" }) });
  await assert.rejects(
    handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, failing.deps),
    rejectsWith("generation_failed", "unavailable"),
  );
  assert.equal(await ticketsOf(uid), 4);
  assert.equal((await lastRequestOf(uid)).state, "done");

  const response = await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, fakeDeps(secondsLater(day, 120)).deps);
  assert.equal(response.ticketsRemaining, 4);
  assert.equal(await ticketsOf(uid), 4);
});

test("同じ番号の前の頼みを作っている途中なら、審査も画像も呼ばずに in_progress で断る", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  const retry = fakeDeps(secondsLater(day, 10));
  const first = fakeDeps(day, {
    // 1回目が審査しているあいだに、通信が切れたアプリが同じ番号で押し直した
    judge: async () => {
      await assert.rejects(
        handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, retry.deps),
        rejectsWith("in_progress", "aborted"),
      );
      return "none";
    },
  });
  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "r1" }, first.deps);
  assert.equal(first.calls.judge, 1);
  assert.equal(retry.calls.judge, 0);
  assert.equal(await ticketsOf(uid), 4);
});

test("頼みの番号の形が違うときは、作り直しには使わずにふつうに作る", async () => {
  const day = uniqueDay();
  const uid = await ticketHolder(day);
  const response = await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: "../etc" }, fakeDeps(day).deps);
  assert.equal(response.ticketsRemaining, 4);
  assert.equal(await lastRequestOf(uid), undefined);

  await handleGenerateMaterial(uid, { prompt: "うさぎ", requestId: 123 }, fakeDeps(day).deps);
  assert.equal(await ticketsOf(uid), 3);
});
