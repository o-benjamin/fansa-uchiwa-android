# サーバー（Cloud Functions）

アプリから呼ばれるサーバーの処理。今は AI 素材の生成（`generateMaterial`、#315）だけ。TypeScript・Node.js 22・東京リージョン（`asia-northeast1`）。

## generateMaterial（AI 素材を1枚作る）

アプリが文章（と任意で元画像1枚）を送ると、Gemini で正方形のステッカー画像を作って返す。アプリは返った画像の背景を ML Kit で切り抜いてから、うちわに置く。

順番（`src/generateMaterial.ts`）：

1. App Check（`enforceAppCheck`）と Firebase Auth（匿名ログイン）を確かめる
2. 文章（1〜200文字）と元画像（JPEG / PNG / WebP、4MB まで）を確かめる
3. 入口スイッチ（Remote Config の `ai_material_enabled`）が閉じていれば断る
4. 断る名前の一覧（実在の人物・グループ名）に当たれば断る。全角・半角、大文字・小文字、空白と記号、カタカナ・ひらがなの違いは無視して当てる（`src/moderation.ts`）
5. 1回分おさえる（`src/quota.ts`）。無料の1回 → なければ回数券。無料の非常ブレーキと全体の安全上限もここで見る
6. 文章と元画像を軽いモデルで審査する（実在の人物・性的・暴力・ヘイト）。4 の一覧をすり抜けた名前や、実在の人物の写真の元画像はここで断る
7. 画像を作る。6・7 で断った・失敗したときは、5 でおさえた回数を戻す

断ったときは `HttpsError` の `details.reason` に理由が入る。アプリはこの値で文言を選び、GA4 にも送る。

| reason | code | 意味 |
|---|---|---|
| `unauthenticated` | unauthenticated | ログインしていない。`details.reason` のない unauthenticated は、App Check で断ったもの（`onCall` が先に断る） |
| `invalid_prompt` / `invalid_image` | invalid-argument | 文章・元画像の形が違う |
| `disabled` | failed-precondition | 入口スイッチが閉じている |
| `blocked_name` / `moderation` | permission-denied | 名前の一覧・審査で断った（`moderation` は `details.category` に分類：`real_person` / `sexual` / `violence` / `hate`） |
| `safety_blocked` | permission-denied | Gemini の安全フィルターで止まった（審査に送った内容が止められたときも） |
| `no_credits` | resource-exhausted | 無料を使い終え、回数券もない（購入画面へ） |
| `free_daily_limit` | resource-exhausted | 無料の非常ブレーキに当たった（無料は明日また試せる。回数券は使える） |
| `total_daily_limit` | resource-exhausted | 全体の安全上限に当たった（今日は止める） |
| `generation_failed` | unavailable | 作れなかった（Gemini の失敗・審査の答えを読めなかった・Firestore の一時的な障害）。回数は戻してあるので、やり直せる |

成功したときは `{ image（base64）, mimeType, paidWith: "free" | "ticket", ticketsRemaining }` を返す。

画像・元画像・文章はサーバーに残さず、ログにも書かない。ログには結果の種類・モデル名・かかった時間・失敗の理由（Gemini のエラーや終わり方）を書く。回数を戻せなかったときだけ、手で戻すために uid と注文番号も書く。

### Firestore（書けるのはサーバーだけ。`firestore.rules` でアプリからは読み書きできない）

| 場所 | 中身 |
|---|---|
| `users/{uid}` | `freeUsed`（無料の1回を使ったか）、`packs.{注文番号}.remaining` / `.expiresAt`（回数券の残りと期限。購入の確認 #348 が足す） |
| `dailyUsage/{日本時間の日付}` | `free` / `ticket`（その日に無料・回数券で作った回数。断った・失敗して戻した分は数えない）、`totalLimitAlerted`（全体の上限を知らせたか） |
| `config/aiMaterial` | 設定（下の表） |

無料は匿名ログインの ID ごとに1回だが、アプリを入れ直すと ID が変わってもう1回使える。そのため、無料の総数は全体の1日の上限（無料の非常ブレーキ）で抑える。

### 設定（`config/aiMaterial`。サーバーを出し直さずに変えられる。反映は最大1分）

| 項目 | 初期値 | 意味 |
|---|---|---|
| `freeDailyLimit` | 30 | 無料の非常ブレーキ：全体で無料で作れる1日の回数 |
| `totalDailyLimit` | 300 | 全体の安全上限：無料と回数券を合わせた1日の回数。当たったら止めて Discord に知らせる。断った・失敗した試行は数えないので、Gemini の請求の歯止めは別に置く（下の「本番に出す」の予算アラート） |
| `imageModel` | `gemini-3.1-flash-lite-image` | 画像のモデル。文字の質が足りなければ `gemini-3.1-flash-image`（約2倍の値段） |
| `judgeModel` | `gemini-3.1-flash-lite` | 審査のモデル |
| `blockedNames` | なし | 断る実在の人物・グループ名。リポジトリが公開なので、一覧はここに置かず Firestore にだけ置く |

ドキュメントがない・型が違う項目は初期値を使う。設定を一度も読めていないときは、名前の一覧なしで動かさないよう `generation_failed` で断る。

書くときは `tools/write-settings.mjs`（使い方はファイルの先頭）。JSON に書いた項目だけを変え、ほかの項目は残す。丸ごと置き換える `--replace` は、`blockedNames` まで消えるので控えを取ってから使う。1項目だけなら Firebase コンソールの Firestore で直接直してもよい。

### 止める

- **すぐ止める**：Remote Config の `ai_material_enabled` を、初期値も条件の値もすべて `false` にして公開する。サーバーは1分以内に断り始める（アプリの入口が消えるのは最大約1時間後）。だれに入口を出すか（条件）はアプリが決める。サーバーは端末の条件を評価できないので、どれか1つでも `true` なら開いているとみなす
- **無料だけ止める**：`config/aiMaterial.freeDailyLimit` を 0 にする

### 困ったとき（Cloud Logging で探す）

- **`回数を戻せなかった`**：断った・失敗したのに回数が減ったままになっている。ログの `uid` と `reservation` を見て、`kind` が `free` なら `users/{uid}.freeUsed` を `false` に、`ticket` なら `users/{uid}.packs.{packId}.remaining` を1増やす。回数券はお金で買った回数なので、見つけたらすぐ戻す
- **`審査のモデルを呼べなかった` / `画像のモデルを呼べなかった` が続く**：ログの `error`（Gemini の `error.status` と `error.message`）を見る。`NOT_FOUND` はモデルの廃止か名前の打ち間違いなので `config/aiMaterial` の `judgeModel` / `imageModel` を差し替える。`PERMISSION_DENIED` / `UNAUTHENTICATED` は API キーの失効なので Secret を入れ直す。`RESOURCE_EXHAUSTED` は割り当ての超過
- **`画像が返らなかった` が続く**：ログの `finishReason` を見る。前置き（`src/gemini.ts` の `STICKER_PREFIX`）かモデルが合っていない
- **`設定を読めなかった` / `回数をおさえられなかった`**：Firestore の障害。続くなら入口スイッチで止める
- 全件が失敗しても Discord には知らせない（知らせるのは全体の上限に当たったときだけ）。入口を開けたら、Cloud Logging の `severity>=ERROR` にログベースのアラートを置いておく

## 開発

```sh
cd functions
npm install
npm test     # ビルドして、Firestore エミュレーターの中でテストを回す
```

`npm test` には Firebase CLI 15 以上（`npm i -g firebase-tools`）と Java 21 以上が要る。Gemini と Discord はテストでは偽物に差し替えるので、API キーは要らない。

関数ごとエミュレーターで動かすときは、`functions/.secret.local`（git に入れない）に `GEMINI_API_KEY=...` と `DISCORD_WEBHOOK_URL=...`（空だと Secret Manager を見に行くので、何か値を入れる）を書いて `firebase emulators:start --only functions,firestore --project demo-fansa-uchiwa`。Remote Config にはエミュレーターがないので、ここでは入口が開かず `disabled` で断られる。流れはテストで確かめる。

### コードの書き方

`AGENTS.md` と `.agents/` の規約は Android（Kotlin）向け。`functions/` では次のとおりにする。

- 型は、使う関数と同じファイルに置く（Kotlin の「1ファイルに class / interface は1つ」は当てない）
- テストの名前は「条件と結果」を日本語の文で書く（Kotlin の `対象_条件_結果` は当てない）
- 外の API（Gemini・Discord・Remote Config）と時刻は、引数（`deps` / `fetchImpl` / `now`）で差し替えられるようにし、テストでは偽物を渡す
- Firestore はエミュレーターの本物を使う。テストごとに利用者の ID と日付を変えて、ほかのテストの回数と混ざらないようにする（`test/helpers.ts`）
- ログに利用者の文章と画像を書かない

## 本番に出す（オーナーの承認が要る）

`firebase deploy` と Secret の登録は Firebase の設定変更なので、オーナーの承認を取ってから行う。初めて出すときは次の順番。

1. Blaze（従量課金）・Firestore（東京）・匿名ログインが有効になっている。App Check は Play Integrity で有効にし、Play Console の「アプリの完全性」で Play Integrity API を同じ Cloud プロジェクトにつなぎ、Firebase のアプリ設定にアプリ署名鍵の SHA-256 を登録する（どちらかが漏れると、全件が App Check で断られる）
2. Cloud Billing に予算アラートを置く（Gemini の請求が想定を超えたら知らせる）。Google Cloud コンソールの「割り当て」で Generative Language API の1日のリクエスト数を絞っておくと、なおよい
3. Secret を登録する：`firebase functions:secrets:set GEMINI_API_KEY`（Google AI Studio の有料枠のキー）、`firebase functions:secrets:set DISCORD_WEBHOOK_URL`
4. `firebase deploy --only functions,firestore:rules`（`--only` なしの `firebase deploy` は Hosting・関数・ルールをすべて出す。Hosting だけなら `--only hosting`）
5. `config/aiMaterial` に `blockedNames` を書く（`tools/write-settings.mjs`）
6. サーバーの関数は Remote Config のテンプレートを読む。実行のサービス アカウント（既定は Compute Engine の既定のサービス アカウント）に Remote Config を読む権限（`Firebase Remote Config 閲覧者`）がなければ足す。読めないと入口は閉じたままになる

### 入口を初めて開ける前に

`ai_material_enabled` を開けると、利用者の文章と画像が Gemini API に送られる。開ける前に次がそろっていること。

- プライバシーポリシーと Play Console のデータセーフティに、文章と画像を Google の Gemini API に送ること（サーバーには残さないこと）を書いてある
- 生成した画像を報告するボタンがアプリに入っている（#339）
- `config/aiMaterial.blockedNames` を書いてある
- 上の「本番に出す」の予算アラートと、ログベースのアラートを置いてある

持ち主が変わるときは、Firebase プロジェクトを移せば Firestore・Secret・関数はそのまま渡る。Gemini の API キーと Discord の Webhook は新しい持ち主のものに入れ替える。
