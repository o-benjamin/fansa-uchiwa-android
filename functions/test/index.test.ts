import assert from "node:assert/strict";
import { test } from "node:test";
import { Expression } from "firebase-functions/params";
import { RESET_VALUE } from "firebase-functions/v2/options";
import { generateMaterial, reportMaterial, revokeVoidedPurchases, verifyPurchase } from "../src/index";

// 購入専用のサービス アカウントは Play Console で返金ができる（#393）。デプロイの設定なので、
// 関数が書き出す設定（firebase deploy が読むもの）で、そのアカウントで動く関数を確かめる

test("回数券の2つの関数は、購入専用のサービス アカウントで動き、メールアドレスはデプロイのときに最後まで埋まる", () => {
  for (const fn of [verifyPurchase, revokeVoidedPurchases]) {
    const serviceAccount = fn.__endpoint.serviceAccountEmail;
    assert.ok(serviceAccount instanceof Expression);
    assert.equal(serviceAccount.toCEL(), "purchase-functions@{{ params.PROJECT_ID }}.iam.gserviceaccount.com");
  }
});

test("ほかの関数は既定のサービス アカウントのまま（RESET_VALUE はデプロイで既定に戻す印）で、購入の権限を持たない", () => {
  assert.equal(generateMaterial.__endpoint.serviceAccountEmail, RESET_VALUE);
  assert.equal(reportMaterial.__endpoint.serviceAccountEmail, RESET_VALUE);
});
