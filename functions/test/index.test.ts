import assert from "node:assert/strict";
import { test } from "node:test";
import { Expression } from "firebase-functions/params";
import { RESET_VALUE } from "firebase-functions/v2/options";
import * as exported from "../src/index";

// 関数の __endpoint は、firebase deploy が読む設定（サービス アカウントもここに入る）。
// 購入専用のアカウントは Play Console で返金ができる（#393）ので、どの関数がそのアカウントで動くかをここで確かめる

const PURCHASE_FUNCTION_NAMES = ["verifyPurchase", "revokeVoidedPurchases"] as const;

test("回数券の2つの関数は、購入専用のサービス アカウントのメールアドレス（プロジェクト ID はデプロイのときに入る）で動く", () => {
  for (const name of PURCHASE_FUNCTION_NAMES) {
    const serviceAccount = exported[name].__endpoint.serviceAccountEmail;
    assert.ok(serviceAccount instanceof Expression, name);
    assert.equal(serviceAccount.toCEL(), "purchase-functions@{{ params.PROJECT_ID }}.iam.gserviceaccount.com", name);
  }
});

test("回数券でない関数は、すべて既定のサービス アカウントで動く", () => {
  // RESET_VALUE は、デプロイで既定のサービス アカウントに戻す印。setGlobalOptions などで serviceAccount を
  // まとめて付けたり、新しい関数に purchaseServiceAccount を付けたりすると、その関数にも返金ができる権限が付くので、
  // ここで落とす。別のサービス アカウントで動かす関数を足すときは、このテストも直す
  const purchaseFunctionNames = new Set<string>(PURCHASE_FUNCTION_NAMES);
  for (const [name, fn] of Object.entries(exported)) {
    if (purchaseFunctionNames.has(name)) continue;
    assert.equal(fn.__endpoint.serviceAccountEmail, RESET_VALUE, name);
  }
});
