import assert from "node:assert/strict";
import { test } from "node:test";
import { createGeminiClient, GeminiHttpError, parseImageResponse, STICKER_PREFIX } from "../src/gemini";

function fakeFetch(body: object, status = 200) {
  const calls: { url: string; init: RequestInit }[] = [];
  const fetchImpl = (async (url: string, init: RequestInit) => {
    calls.push({ url, init });
    return new Response(JSON.stringify(body), { status });
  }) as unknown as typeof fetch;
  return { fetchImpl, calls };
}

test("画像の答えから画像を取り出す", () => {
  const result = parseImageResponse({
    candidates: [{ content: { parts: [{ text: "説明" }, { inlineData: { mimeType: "image/png", data: "AAAA" } }] } }],
  });
  assert.deepEqual(result, { kind: "image", mimeType: "image/png", data: "AAAA" });
});

test("頼んだ文章が止められた・安全フィルターで止まったときは safety_blocked", () => {
  assert.deepEqual(parseImageResponse({ promptFeedback: { blockReason: "PROHIBITED_CONTENT" } }), {
    kind: "safety_blocked",
    finishReason: "PROHIBITED_CONTENT",
  });
  assert.deepEqual(parseImageResponse({ candidates: [{ finishReason: "IMAGE_SAFETY" }] }), {
    kind: "safety_blocked",
    finishReason: "IMAGE_SAFETY",
  });
});

test("画像がなく、安全フィルターでもなければ no_image（ログに残すため終わり方も返す）", () => {
  assert.deepEqual(parseImageResponse({ candidates: [{ finishReason: "STOP", content: { parts: [{ text: "x" }] } }] }), {
    kind: "no_image",
    finishReason: "STOP",
  });
  assert.deepEqual(parseImageResponse({}), { kind: "no_image", finishReason: "候補なし" });
});

test("画像は正方形で、ステッカー用の前置きを付け、API キーはヘッダーで送る", async () => {
  const { fetchImpl, calls } = fakeFetch({
    candidates: [{ content: { parts: [{ inlineData: { mimeType: "image/png", data: "AAAA" } }] } }],
  });
  const client = createGeminiClient("key-for-test", fetchImpl);
  const result = await client.generateImage("gemini-3.1-flash-lite-image", "王冠のうさぎ");

  assert.equal(result.kind, "image");
  assert.equal(calls.length, 1);
  assert.match(calls[0].url, /models\/gemini-3\.1-flash-lite-image:generateContent$/);
  assert.equal((calls[0].init.headers as Record<string, string>)["x-goog-api-key"], "key-for-test");
  assert.doesNotMatch(calls[0].url, /key-for-test/);
  const body = JSON.parse(calls[0].init.body as string);
  assert.deepEqual(body.generationConfig, { responseModalities: ["IMAGE"], imageConfig: { aspectRatio: "1:1" } });
  assert.equal(body.contents[0].parts.length, 1);
  assert.ok(body.contents[0].parts[0].text.startsWith(STICKER_PREFIX));
  assert.ok(body.contents[0].parts[0].text.endsWith("内容：王冠のうさぎ"));
});

test("元画像があれば、文章の前に画像を付ける", async () => {
  const { fetchImpl, calls } = fakeFetch({ candidates: [] });
  const client = createGeminiClient("key", fetchImpl);
  await client.generateImage("model", "うさぎ", { mimeType: "image/jpeg", data: "BBBB" });

  const parts = JSON.parse(calls[0].init.body as string).contents[0].parts;
  assert.deepEqual(parts[0], { inlineData: { mimeType: "image/jpeg", data: "BBBB" } });
  assert.ok(parts[1].text.includes("添付の画像をもとに"));
});

test("API がエラーを返したら、API の理由を持った GeminiHttpError", async () => {
  const { fetchImpl } = fakeFetch({ error: { status: "RESOURCE_EXHAUSTED", message: "quota" } }, 429);
  const client = createGeminiClient("key", fetchImpl);
  await assert.rejects(client.generateImage("model", "うさぎ"), (error) => {
    assert.ok(error instanceof GeminiHttpError);
    assert.equal(error.status, 429);
    assert.equal(error.apiStatus, "RESOURCE_EXHAUSTED");
    assert.equal(error.apiMessage, "quota");
    return true;
  });
});

test("審査は JSON の答えを頼み、分類を返す", async () => {
  const { fetchImpl, calls } = fakeFetch({
    candidates: [{ content: { parts: [{ text: '{"category":"real_person"}' }] } }],
  });
  const client = createGeminiClient("key", fetchImpl);
  assert.equal(await client.judge("gemini-3.1-flash-lite", "だれかの顔"), "real_person");

  const body = JSON.parse(calls[0].init.body as string);
  assert.equal(body.generationConfig.responseMimeType, "application/json");
  assert.equal(body.contents[0].parts[0].text, "だれかの顔");
  assert.ok(body.systemInstruction.parts[0].text.length > 0);
});

test("審査に元画像も渡す", async () => {
  const { fetchImpl, calls } = fakeFetch({ candidates: [{ content: { parts: [{ text: '{"category":"none"}' }] } }] });
  const client = createGeminiClient("key", fetchImpl);
  assert.equal(await client.judge("model", "うさぎ", { mimeType: "image/png", data: "CCCC" }), "none");

  const parts = JSON.parse(calls[0].init.body as string).contents[0].parts;
  assert.deepEqual(parts, [{ inlineData: { mimeType: "image/png", data: "CCCC" } }, { text: "うさぎ" }]);
});

test("審査に送った内容が安全フィルターで止められたら safety_blocked、答えを読めなければ undefined", async () => {
  const blocked = createGeminiClient("key", fakeFetch({ promptFeedback: { blockReason: "SAFETY" } }).fetchImpl);
  assert.equal(await blocked.judge("model", "x"), "safety_blocked");
  const unreadable = createGeminiClient("key", fakeFetch({ candidates: [{ finishReason: "MAX_TOKENS" }] }).fetchImpl);
  assert.equal(await unreadable.judge("model", "x"), undefined);
});
