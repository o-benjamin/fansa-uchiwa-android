# サーバー（Cloud Functions）

アプリから呼ばれるサーバーの処理。今は AI 素材の生成（`generateMaterial`、#315）だけ。TypeScript・Node.js 22・東京リージョン（`asia-northeast1`）。

## generateMaterial（AI 素材を1枚作る）

アプリが文章（と任意で元画像1枚）を送ると、Gemini で正方形のステッカー画像を作って返す。アプリは返った画像の背景を ML Kit で切り抜いてから、うちわに置く。

順番（`src/generateMaterial.ts`）：

1. App Check（`enforceAppCheck`）と Firebase Auth（匿名ログイン）を確かめる
2. 文章（1〜200文字）と元画像（JPEG / PNG / WebP、4MB まで）を確かめる
3. 入口スイッチ（Remote Config の `ai_material_enabled`）が閉じていれば断る
4. 断る名前の一覧（実在の人物・グループ名）に当たれば断る
5. 1回分おさえる（`src/quota.ts`）。無料の1回 → なければ回数券。無料の非常ブレーキと全体の安全上限もここで見る
6. 文章を軽いモデルで審査する（実在の人物・性的・暴力・ヘイト）
7. 画像を作る。6・7 で断った・失敗したときは、5 でおさえた回数を戻す

断ったときは `HttpsError` の `details.reason` に理由が入る。アプリはこの値で文言を選ぶ。

| reason | code | 意味 |
|---|---|---|
| `unauthenticated` | unauthenticated | ログインしていない |
| `invalid_prompt` / `invalid_image` | invalid-argument | 文章・元画像の形が違う |
| `disabled` | failed-precondition | 入口スイッチが閉じている |
| `blocked_name` / `moderation` | permission-denied | 名前の一覧・審査で断った（`moderation` は `details.category` に分類） |
| `safety_blocked` | permission-denied | Gemini の安全フィルターで止まった |
| `no_credits` | resource-exhausted | 無料を使い終え、回数券もない（購入画面へ） |
| `free_daily_limit` | resource-exhausted | 無料の非常ブレーキに当たった（無料は明日また試せる。回数券は使える） |
| `total_daily_limit` | resource-exhausted | 全体の安全上限に当たった（今日は止める） |
| `generation_failed` | unavailable | 画像を作れなかった（回数は戻してある） |

成功したときは `{ image（base64）, mimeType, paidWith: "free" | "ticket", ticketsRemaining }` を返す。

画像・元画像・文章はサーバーに残さない。ログには結果の種類と回数だけを書き、文章は書かない。

### Firestore（書けるのはサーバーだけ。`firestore.rules` でアプリからは読み書きできない）

| 場所 | 中身 |
|---|---|
| `users/{uid}` | `freeUsed`（無料の1回を使ったか）、`packs.{注文番号}.remaining` / `.expiresAt`（回数券の残りと期限。購入の確認 #348 が足す） |
| `spend/{日本時間の日付}` | `free` / `paid`（その日に作った回数）、`totalLimitAlerted`（全体の上限を知らせたか） |
| `config/aiMaterial` | 設定（下の表） |

### 設定（`config/aiMaterial`。サーバーを出し直さずに変えられる。反映は最大1分）

| 項目 | 初期値 | 意味 |
|---|---|---|
| `freeDailyLimit` | 30 | 無料の非常ブレーキ：全体で無料で作れる1日の回数 |
| `totalDailyLimit` | 300 | 全体の安全上限：無料と回数券を合わせた1日の回数。当たったら止めて Discord に知らせる |
| `imageModel` | `gemini-3.1-flash-lite-image` | 画像のモデル。文字の質が足りなければ `gemini-3.1-flash-image`（約2倍の値段） |
| `judgeModel` | `gemini-3.1-flash-lite` | 文章の審査のモデル |
| `blockedNames` | なし | 断る実在の人物・グループ名。リポジトリが公開なので、一覧はここに置かず Firestore にだけ置く |

ドキュメントがない・型が違う項目は初期値を使う。まとめて書くときは `tools/write-settings.mjs`（使い方はファイルの先頭）。

### 止める

- **すぐ止める**：Remote Config の `ai_material_enabled` を、初期値も条件の値もすべて `false` にして公開する。サーバーは1分以内に断り始める（アプリの入口が消えるのは最大約1時間後）
- **無料だけ止める**：`config/aiMaterial.freeDailyLimit` を 0 にする

## 開発

```sh
cd functions
npm install
npm test     # ビルドして、Firestore エミュレーターの中でテストを回す
```

`npm test` には Firebase CLI（`npm i -g firebase-tools`）と Java 11 以上が要る。Gemini と Discord はテストでは偽物に差し替えるので、API キーは要らない。

関数ごとエミュレーターで動かすときは、`functions/.secret.local`（git に入れない）に `GEMINI_API_KEY=...` と `DISCORD_WEBHOOK_URL=...`（空だと Secret Manager を見に行くので、何か値を入れる）を書いて `firebase emulators:start --only functions,firestore --project demo-fansa-uchiwa`。

## 本番に出す（オーナーの承認が要る）

`firebase deploy` と Secret の登録は Firebase の設定変更なので、オーナーの承認を取ってから行う。初めて出すときは次の順番。

1. Blaze（従量課金）・Firestore（東京）・匿名ログイン・App Check（Play Integrity）が有効になっている
2. Secret を登録する：`firebase functions:secrets:set GEMINI_API_KEY`（Google AI Studio の有料枠のキー）、`firebase functions:secrets:set DISCORD_WEBHOOK_URL`
3. `firebase deploy --only functions,firestore:rules`
4. `config/aiMaterial` に `blockedNames` を書く（`tools/write-settings.mjs`）
5. サーバーの関数は Remote Config のテンプレートを読む。実行のサービス アカウント（既定は Compute Engine の既定のサービス アカウント）に Remote Config を読む権限（`Firebase Remote Config 閲覧者`）がなければ足す。読めないと入口は閉じたままになる

持ち主が変わるときは、Firebase プロジェクトを移せば Firestore・Secret・関数はそのまま渡る。Gemini の API キーと Discord の Webhook は新しい持ち主のものに入れ替える。
