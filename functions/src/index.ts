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
    timeoutSeconds: 120,
    memory: "512MiB",
    // 全体の上限（1日300回）を大きく超える同時実行は要らない。不具合や攻撃で費用が膨らむのを抑える
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
