import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getRemoteConfig } from "firebase-admin/remote-config";
import { defineSecret, expr, projectID } from "firebase-functions/params";
import { onCall } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { GoogleAuth } from "google-auth-library";
import { postToDiscord } from "./discord";
import { createEntrySwitch } from "./entrySwitch";
import { createGeminiClient } from "./gemini";
import { handleGenerateMaterial } from "./generateMaterial";
import { createPlayBillingClient } from "./playBilling";
import { handleReportMaterial } from "./reportMaterial";
import { createSettingsLoader } from "./settings";
import { handleVerifyPurchase } from "./verifyPurchase";
import { handleRevokeVoidedPurchases } from "./voidedPurchases";

initializeApp();

/** Google AI Studio の Gemini API キー（有料枠。学習に使われない） */
const geminiApiKey = defineSecret("GEMINI_API_KEY");
/** 全体の安全上限に当たったときに知らせる Discord の Webhook の URL */
const discordWebhookUrl = defineSecret("DISCORD_WEBHOOK_URL");

/** アプリのパッケージ名（app/build.gradle.kts の applicationId）。Play Developer API で購入を確かめるときに使う */
const ANDROID_PACKAGE_NAME = "com.fansauchiwa";

const db = getFirestore();
const isEntryOpen = createEntrySwitch(() => getRemoteConfig().getTemplate());
const loadSettings = createSettingsLoader(db);

/**
 * 回数券の関数（verifyPurchase・revokeVoidedPurchases）だけを動かすサービス アカウント（#393）。
 * Play Console で返金ができる権限を持たせるので、ほかの関数（既定のサービス アカウント）とは分ける。
 * 作り方と権限は functions/README.md の「本番に出す」。「名前@」の省略形は Cloud Scheduler のジョブには
 * 展開されずに渡るので、メールアドレスを最後まで書く（プロジェクト ID はデプロイのときに入る）
 */
const purchaseServiceAccount = expr`purchase-functions@${projectID}.iam.gserviceaccount.com`;

// Play Developer API は、呼ぶ関数を動かすサービス アカウントで呼ぶ。呼ぶのは回数券の関数だけなので
// purchaseServiceAccount になる。Play Console の「ユーザーと権限」にはこのアカウントだけを足す
const playAuth = new GoogleAuth({ scopes: ["https://www.googleapis.com/auth/androidpublisher"] });
const play = createPlayBillingClient(ANDROID_PACKAGE_NAME, async () => {
  const token = await playAuth.getAccessToken();
  if (!token) throw new Error("Play Developer API のアクセストークンを取れなかった");
  return token;
});

/** AI 素材を1枚作る（#315）。中身と順番は generateMaterial.ts、運用は functions/README.md。 */
export const generateMaterial = onCall(
  {
    region: "asia-northeast1",
    enforceAppCheck: true,
    secrets: [geminiApiKey, discordWebhookUrl],
    // gemini.ts の待ち時間（審査15秒＋画像60秒）より長くする。短いと、時間切れで打ち切られて回数を戻せなくなる
    timeoutSeconds: 120,
    memory: "512MiB",
    // インスタンスが増え続けないように抑える（1インスタンスが複数の呼び出しを同時に受けるので、同時実行の上限ではない）。
    // 作る回数と費用の上限は、回数の上限（quota.ts の全体の1日の上限）で守る
    maxInstances: 5,
  },
  (request) =>
    handleGenerateMaterial(request.auth?.uid, request.data, {
      db,
      isEntryOpen,
      loadSettings,
      gemini: createGeminiClient(geminiApiKey.value()),
      notifyOwner: (message) => postToDiscord(discordWebhookUrl.value(), message),
      now: () => new Date(),
    }),
);

/** 作った素材を利用者が報告する（#339）。中身は reportMaterial.ts、運用は functions/README.md。 */
export const reportMaterial = onCall(
  {
    region: "asia-northeast1",
    enforceAppCheck: true,
    secrets: [discordWebhookUrl],
    // generateMaterial と同じく、インスタンスが増え続けないように抑える（同時実行の上限ではない）。
    // Firestore に1件書くだけで1回が短いので2で足り、メモリも既定（256MiB）のまま。報告の数は1人1日の上限（MAX_REPORTS_PER_DAY）で抑える
    maxInstances: 2,
  },
  (request) =>
    handleReportMaterial(request.auth?.uid, request.data, {
      db,
      notifyOwner: (message) => postToDiscord(discordWebhookUrl.value(), message),
      now: () => new Date(),
    }),
);

/** 回数券の購入を確かめて回数を足す（#348）。中身は verifyPurchase.ts、運用は functions/README.md。 */
export const verifyPurchase = onCall(
  // maxInstances はインスタンスが増え続けないように抑える（generateMaterial と同じ）
  { region: "asia-northeast1", enforceAppCheck: true, maxInstances: 5, serviceAccount: purchaseServiceAccount },
  (request) => handleVerifyPurchase(request.auth?.uid, request.data, { db, play, now: () => new Date() }),
);

/** 返金された購入の回数券を毎日取り消す（#348）。中身は voidedPurchases.ts。 */
export const revokeVoidedPurchases = onSchedule(
  {
    schedule: "every day 04:00",
    timeZone: "Asia/Tokyo",
    region: "asia-northeast1",
    serviceAccount: purchaseServiceAccount,
  },
  async () => {
    await handleRevokeVoidedPurchases({ db, play, now: () => new Date() });
  },
);
