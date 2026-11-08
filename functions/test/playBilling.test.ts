import assert from "node:assert/strict";
import { test } from "node:test";
import { createPlayBillingClient, PlayHttpError } from "../src/playBilling";

function fakeFetch(bodies: object[], status = 200) {
  const calls: { url: string; init: RequestInit }[] = [];
  const fetchImpl = (async (url: string, init: RequestInit) => {
    calls.push({ url, init });
    return new Response(JSON.stringify(bodies[calls.length - 1] ?? {}), { status });
  }) as unknown as typeof fetch;
  return { fetchImpl, calls };
}

const getToken = async () => "access-token-for-test";

test("購入はパッケージ名・アイテム・トークンの URL を GET し、アクセストークンはヘッダーで送る", async () => {
  const { fetchImpl, calls } = fakeFetch([{ purchaseState: 0, orderId: "GPA.1" }]);
  const play = createPlayBillingClient("com.fansauchiwa", getToken, fetchImpl);

  const purchase = await play.getProductPurchase("ai_material_ticket_5", "token/with+symbols");

  assert.deepEqual(purchase, { purchaseState: 0, orderId: "GPA.1" });
  assert.equal(
    calls[0].url,
    "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.fansauchiwa/purchases/products/" +
      "ai_material_ticket_5/tokens/token%2Fwith%2Bsymbols",
  );
  assert.equal(calls[0].init.method, "GET");
  assert.equal((calls[0].init.headers as Record<string, string>).authorization, "Bearer access-token-for-test");
});

test("消費は :consume に POST する", async () => {
  const { fetchImpl, calls } = fakeFetch([{}]);
  const play = createPlayBillingClient("com.fansauchiwa", getToken, fetchImpl);

  await play.consumeProductPurchase("ai_material_ticket_5", "token");

  assert.match(calls[0].url, /\/products\/ai_material_ticket_5\/tokens\/token:consume$/);
  assert.equal(calls[0].init.method, "POST");
});

test("返金の一覧は次のページのトークンを返し、2ページ目はそのトークンで頼む", async () => {
  const { fetchImpl, calls } = fakeFetch([
    { voidedPurchases: [{ purchaseToken: "a" }], tokenPagination: { nextPageToken: "next/1" } },
    {},
  ]);
  const play = createPlayBillingClient("com.fansauchiwa", getToken, fetchImpl);

  const first = await play.listVoidedPurchases();
  const second = await play.listVoidedPurchases(first.nextPageToken);

  assert.deepEqual(first, { voidedPurchases: [{ purchaseToken: "a" }], nextPageToken: "next/1" });
  assert.deepEqual(second, { voidedPurchases: [], nextPageToken: undefined });
  assert.match(calls[0].url, /\/purchases\/voidedpurchases$/);
  assert.match(calls[1].url, /\/purchases\/voidedpurchases\?token=next%2F1$/);
});

test("エラーの答えは、状態と API の理由を持った PlayHttpError にする", async () => {
  const { fetchImpl } = fakeFetch([{ error: { status: "PERMISSION_DENIED", message: "no access" } }], 403);
  const play = createPlayBillingClient("com.fansauchiwa", getToken, fetchImpl);

  await assert.rejects(play.getProductPurchase("ai_material_ticket_5", "token"), (error: unknown) => {
    assert.ok(error instanceof PlayHttpError);
    assert.equal(error.status, 403);
    assert.equal(error.apiStatus, "PERMISSION_DENIED");
    assert.equal(error.apiMessage, "no access");
    return true;
  });
});
