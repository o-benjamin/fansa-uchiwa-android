#!/usr/bin/env node
// Firestore の config/aiMaterial（AI 素材の設定）に、JSON ファイルの項目を書く。
// 書いた項目だけを変え、ほかの項目（とくに blockedNames）はそのまま残す。
// 丸ごと置き換えるのは --replace を付けたときだけ（書かなかった項目はサーバーの初期値に戻る。src/settings.ts）。
// blockedNames は Firestore にしかない（リポジトリが公開なので置かない）。置き換える前に、今の中身の控えを取る。
//
//   本番:         node tools/write-settings.mjs --project <Firebase のプロジェクト ID> settings.json
//   エミュレーター: FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 node tools/write-settings.mjs --project demo-fansa-uchiwa settings.json
//
// 本番に書くには Application Default Credentials が要る（gcloud auth application-default login）。
// 本番の設定を変えるのは Firebase の設定変更なので、オーナーの承認を取ってから行う。
import { readFileSync } from "node:fs";
import { parseArgs } from "node:util";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

const { values, positionals } = parseArgs({
  options: { project: { type: "string" }, replace: { type: "boolean", default: false } },
  allowPositionals: true,
});
if (!values.project || positionals.length !== 1) {
  console.error("使い方: node tools/write-settings.mjs --project <プロジェクト ID> [--replace] <settings.json>");
  process.exit(1);
}

const settings = JSON.parse(readFileSync(positionals[0], "utf8"));
initializeApp({ projectId: values.project });
await getFirestore()
  .doc("config/aiMaterial")
  .set(settings, { merge: !values.replace });
const mode = values.replace ? "丸ごと置き換えた" : "書いた項目だけ変えた";
console.log(`config/aiMaterial を${mode}（項目: ${Object.keys(settings).join(", ")}）`);
