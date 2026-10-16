import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getRemoteConfig } from "firebase-admin/remote-config";
import { defineSecret } from "firebase-functions/params";
import { onCall } from "firebase-functions/v2/https";
import { postToDiscord } from "./discord";
import { createEntrySwitch } from "./entrySwitch";
import { createGeminiClient } from "./gemini";
import { handleGenerateMaterial } from "./generateMaterial";
import { createSettingsLoader } from "./settings";

initializeApp();

/** Google AI Studio の Gemini API キー（有料枠。学習に使われない） */
const geminiApiKey = defineSecret("GEMINI_API_KEY");
/** 全体の安全上限に当たったときに知らせる Discord の Webhook の URL */
const discordWebhookUrl = defineSecret("DISCORD_WEBHOOK_URL");

const db = getFirestore();
const isEntryOpen = createEntrySwitch(() => getRemoteConfig().getTemplate());
const loadSettings = createSettingsLoader(db);

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
