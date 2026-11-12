# サーバー（Cloud Functions）

アプリから呼ばれるサーバーの処理。AI 素材の生成（`generateMaterial`、#315）、作った素材の報告（`reportMaterial`、#339）、回数券の購入の確認・返金の取り消し（`verifyPurchase`・`revokeVoidedPurchases`、#348）。TypeScript・Node.js 22・東京リージョン（`asia-northeast1`）。

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

回数券を使う順番（5）・回数を戻す動き（7）・回数券でも断るとき（3 の入口スイッチ、5 の全体の安全上限）を変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す（`.agents/play-billing.md`）。

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

成功したときは `{ image（base64）, mimeType, paidWith: "free" | "ticket", ticketsRemaining, model }` を返す。`model` は画像を作ったモデルで、利用者が報告するときにアプリが `reportMaterial` へ返す。`model` がないとアプリは報告のモデル名を `unknown` として送るので、応答から外さない・名前を変えない。

画像・元画像・文章はサーバーに残さず、ログにも書かない。ログには結果の種類・モデル名・かかった時間・失敗の理由（Gemini のエラーや終わり方）を書く。回数を戻せなかったときだけ、手で戻すために uid と購入 ID も書く。

### Firestore（書けるのはサーバーだけ。`firestore.rules` でアプリからは読み書きできない）

| 場所 | 中身 |
|---|---|
| `users/{uid}` | `freeUsed`（無料の1回を使ったか）、`packs.{購入 ID}.remaining` / `.expiresAt`（回数券の残りと期限。`verifyPurchase` が足し、返金されたら消す） |
| `purchases/{購入 ID}` | 購入の記録（下の「verifyPurchase」）。購入 ID は購入トークンの SHA-256 |
| `dailyUsage/{日本時間の日付}` | `free` / `ticket`（その日に無料・回数券で作った回数。断った・失敗して戻した分は数えない）、`totalLimitAlerted`（全体の上限を知らせたか） |
| `config/aiMaterial` | 設定（下の表） |
| `reports/{ID}`・`reportUsage/{uid}` | 利用者からの報告（下の「reportMaterial」） |

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

- **`回数を戻せなかった`**：断った・失敗したのに回数が減ったままになっている。ログの `uid` と `reservation` を見て、`kind` が `free` なら `users/{uid}.freeUsed` を `false` に、`ticket` なら `users/{uid}.packs.{購入 ID}.remaining` を1増やす。回数券はお金で買った回数なので、見つけたらすぐ戻す
- **`審査のモデルを呼べなかった` / `画像のモデルを呼べなかった` が続く**：ログの `error`（Gemini の `error.status` と `error.message`）を見る。`NOT_FOUND` はモデルの廃止か名前の打ち間違いなので `config/aiMaterial` の `judgeModel` / `imageModel` を差し替える。`PERMISSION_DENIED` / `UNAUTHENTICATED` は API キーの失効なので Secret を入れ直す。`RESOURCE_EXHAUSTED` は割り当ての超過
- **`画像が返らなかった` が続く**：ログの `finishReason` を見る。前置き（`src/gemini.ts` の `STICKER_PREFIX`）かモデルが合っていない
- **`設定を読めなかった` / `回数をおさえられなかった`**：Firestore の障害。続くなら入口スイッチで止める
- 全件が失敗しても Discord には知らせない（知らせるのは全体の上限に当たったときだけ）。入口を開けたら、Cloud Logging の `severity>=ERROR` にログベースのアラートを置いておく

## reportMaterial（作った素材の報告を受ける）

アプリの生成結果の画面の「この素材を報告する」から呼ばれる。Google Play の AI 生成コンテンツのポリシーで、不快な生成物をアプリの中から開発者に報告できることが要るため。アプリは送る前に「頼んだ文章と選んだ理由を、匿名の利用者 ID とあわせて開発者に送ります。絵は送りません」と見せる（`strings.xml` の `ai_material_report_message`）。

- 受け取るもの：`{ prompt（1〜200文字）, reason, model }`。`reason` は `inappropriate`（不快・不適切）/ `real_person`（実在の人に似ている）/ `rights`（ほかの作品やキャラクターに似ている）/ `other`。`model` は `generateMaterial` が返した値
- App Check と匿名ログインを確かめる。入口スイッチが閉じていても受け付ける（閉じる前に作った素材も報告できるように）
- `reports/{自動の ID}` に `uid`・`prompt`・`reason`・`model`・`createdAt`・`expireAt`（180日後）を書き、Discord に理由とモデル名と ID だけを知らせる（文章は書かない）
- 1人1日10件まで（`reportUsage/{uid}` で数える。最後の報告の2日後に消える）。超えたら `details.reason` が `too_many_reports`（resource-exhausted。アプリは「今日はこれ以上報告できません」と出す）。形が違えば `invalid_report`（invalid-argument）、Firestore に書けなければ `report_failed`（unavailable。アプリは押し直せる）
- `reports` と `reportUsage` の `expireAt` での削除（TTL）は `firestore.indexes.json` の `fieldOverrides` に書いてあり、`firebase deploy --only firestore` で入る
- 画像は受け取らない。利用者は報告した絵をもう持っていないこともあり、絵を送らせると個人の写真が混ざるおそれがあるため

### 報告が届いたら

1. Firebase コンソールの Firestore で `reports/{ID}` を開き、文章と理由を読む
2. 実在の人物・グループ名なら `config/aiMaterial.blockedNames` に足す（`tools/write-settings.mjs`）。名前ではなく審査をすり抜けた内容なら、`src/gemini.ts` の審査の前置きを直す Issue を立てる
3. 同じ `uid` から同じ内容が続くときは、いたずらとして扱ってよい
4. 報告が急に増えたら、入口スイッチで止めてから調べる

### 困ったとき（Cloud Logging で探す）

- **`Discord に知らせられなかった`**（`reportMaterial` のログ）：報告は Firestore に残っているが、だれも気づいていない。Secret `DISCORD_WEBHOOK_URL` の Webhook が生きているかを確かめ、Firestore の `reports` を `createdAt` の新しい順に見て、知らせられなかった分を読む
- **`報告を残せなかった` が続く**：Firestore の障害。アプリには「報告を送れませんでした」と出て、利用者は押し直せる

## verifyPurchase（回数券の購入を確かめて回数を足す）

アプリが Play Billing で回数券を買ったら、購入トークンを送る（`{ productId: "ai_material_ticket_5", purchaseToken }`）。サーバーが Play Developer API で購入を確かめ、回数を足してから消費済みにする（`src/verifyPurchase.ts`）。

- 1購入で5回。期限は購入から6か月（日本時間の暦。同じ日がない月は月末）。6か月を超えると資金決済法の対象になるので延ばさない（`TICKET_VALID_MONTHS`）。支払った額によらず同じなので、Play の価格実験で値段を変えてもサーバーは直さない。値段はアプリが Play から取って出す
- 足すのは購入1件につき1回だけ（同時に届いても）。同じ利用者が送り直したときは、足したときの内容を返す（`alreadyGranted: true`）。アプリは応答が届かなかったとき・起動時に未消費の購入が残っていたときに送り直してよい
- 足してから消費する。消費に失敗しても回数は残し、送り直しで消費をやり直す。消費しないまま3日たつと Play が自動で返金し、下の取り消しで回数も消える
- 入口スイッチは見ない（閉じたあとでも、払った人には回数を渡す）
- 期間・期限の決め方・消費しないまま3日たつと返金される動きを変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す（`.agents/play-billing.md`）
- アイテム ID は `ai_material_ticket_5`（消費型）。Play Console のアプリ内アイテムをこの ID で作る
- アイテム ID・1購入の回数・期間は、アプリにも同じ値を書いて購入の案内に出している（`PlayBillingRemoteSource.TICKET_PRODUCT_ID`、`AiMaterialUiState.kt` の `AI_MATERIAL_TICKETS_PER_PURCHASE`・`AI_MATERIAL_TICKET_VALID_MONTHS`）。変えるときはアプリも直して出し直す。出回っている古い版は前の値を出し続ける（アプリ側の決まりは `.agents/play-billing.md`）

成功したときは `{ ticketsAdded, ticketsRemaining（使える回数券の残りの合計）, expiresAt（ISO 8601）, alreadyGranted }` を返す。断ったときは `details.reason` に理由が入る。

| reason | code | 意味 |
|---|---|---|
| `unauthenticated` | unauthenticated | ログインしていない（`details.reason` がなければ App Check で断ったもの） |
| `invalid_purchase` | invalid-argument | アイテム ID が違う・トークンがない |
| `pending` | failed-precondition | 支払い待ち（コンビニ払いなど）。支払いが済んだらアプリが送り直す |
| `not_purchased` | permission-denied | 取り消された・Play が知らない・返金で取り消し済みの購入 |
| `owned_by_other` | permission-denied | ほかの利用者（入れ直して匿名ログインの ID が変わった、など）が受け取り済み |
| `verification_failed` | unavailable | Play か Firestore に聞けなかった。回数は足していないので、やり直せる |

`purchases/{購入 ID}` に `uid`・`orderId`（Play の注文番号 `GPA.…`）・`tickets`・`purchasedAt`・`expiresAt`・`grantedAt`・`consumed` を書く。購入トークンそのものは保存しない。記録は、回数券の期限（`expiresAt`）から30日たったら消してよい（返金の取り消しは過去30日分しか見ないため）。自動で消す仕組みはまだない。

## revokeVoidedPurchases（返金された回数券を取り消す。毎日 4:00）

Voided Purchases API で過去30日分の返金・取り消しを読み、その購入の回数券（`users/{uid}.packs.{購入 ID}`）を消す（`src/voidedPurchases.ts`）。使った分は戻さない。`purchases` に `voidedAt`・`voidedReason`・`voidedSource`・`ticketsRevoked`（取り消したときに残っていた回数）を書く。毎日30日分を見直して取り消し済みは飛ばすので、止まっても30日以内なら次に動いたときに拾える（超えた分は取り消せない）。1件が失敗しても、ほかの件は続ける。取り消し方を変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す（`.agents/play-billing.md`）。

### 困ったとき（回数券。Cloud Logging で探す）

- **問い合わせ（「払ったのに増えない」「入れ直したら消えた」）**：利用者に注文番号（`GPA.…`、Google Play の購入履歴やメールにある）を聞き、`purchases` を `orderId` で探す
  - 見つからない：サーバーで回数を足しておらず、消費もしていない。3日で Play が自動で返金するので、Play Console の「注文管理」で状態を見る。ログを `orderId` で探すと、断った理由（`pending` など）が分かる
  - `voidedAt` がある：返金済み
  - 別の `uid` に付いている（入れ直して匿名ログインの ID が変わった。ログの `owned_by_other` に両方の `uid` がある）：Play Console で返金する。翌日の `revokeVoidedPurchases` が前の ID の残りを消す
- **`Play で購入を確かめられなかった` が続く**：ログの `error` の状態を見る。403（`PERMISSION_DENIED`）は、関数のサービス アカウントが Play Console の「ユーザーと権限」にいないか権限が足りない（下の「本番に出す」の 7）。401 はスコープかアカウントの設定。Play の障害なら待つ（利用者には `verification_failed` で返り、回数は足していない）
- **`消費できなかった`**：回数は足してあり、アプリが送り直せば消費をやり直す。3日続くと Play が自動で返金し、回数券も取り消される。続くなら上と同じく権限を見る
- **`revokeVoidedPurchases` が失敗している**（毎日の件数のログ `revokeVoidedPurchases` が出ない・この関数の ERROR がある）：止まっている間、返金された回数券は使えるまま。403 なら「本番に出す」の 7 の権限。直したら Google Cloud コンソールの Cloud Scheduler でジョブを強制実行し、件数のログが出るのを確かめる。30日を超えて止まった分は取り消せない
- **`返金された回数券を取り消せなかった`**：その1件だけ取り消せなかった（翌日やり直す）。続くなら、ログの `orderId` で `purchases` の記録の中身を見る
- **`返金の一覧をたどりきれなかった`**：1回で20ページ（2万件）を超えた。返金が急に増えていないかを Play Console で見る

## 開発

```sh
cd functions
npm install
npm test     # ビルドして、Firestore エミュレーターの中でテストを回す
```

`npm test` には Firebase CLI 15 以上（`npm i -g firebase-tools`）と Java 21 以上が要る。Gemini・Discord・Play Developer API はテストでは偽物に差し替えるので、API キーやサービス アカウントは要らない。

関数ごとエミュレーターで動かすときは、`functions/.secret.local`（git に入れない）に `GEMINI_API_KEY=...` と `DISCORD_WEBHOOK_URL=...`（空だと Secret Manager を見に行くので、何か値を入れる）を書いて `firebase emulators:start --only functions,firestore --project demo-fansa-uchiwa`。Remote Config にはエミュレーターがないので、ここでは入口が開かず `disabled` で断られる。流れはテストで確かめる。

### コードの書き方

`AGENTS.md` と `.agents/` の規約は Android（Kotlin）向け。`functions/` では次のとおりにする。

- 型は、使う関数と同じファイルに置く（Kotlin の「1ファイルに class / interface は1つ」は当てない）
- テストの名前は「条件と結果」を日本語の文で書く（Kotlin の `対象_条件_結果` は当てない）
- 外の API（Gemini・Discord・Remote Config・Play Developer API）と時刻は、引数（`deps` / `fetchImpl` / `now`）で差し替えられるようにし、テストでは偽物を渡す
- Firestore はエミュレーターの本物を使う。テストごとに利用者の ID と日付を変えて、ほかのテストの回数と混ざらないようにする（`test/helpers.ts`）
- ログに利用者の文章と画像を書かない

## 本番に出す（オーナーの承認が要る）

`firebase deploy` と Secret の登録は Firebase の設定変更なので、オーナーの承認を取ってから行う。初めて出すときは次の順番。

1. Blaze（従量課金）・Firestore（東京）・匿名ログインが有効になっている。App Check は Play Integrity で有効にし、Play Console の「アプリの完全性」で Play Integrity API を同じ Cloud プロジェクトにつなぎ、Firebase のアプリ設定にアプリ署名鍵の SHA-256 を登録する（どちらかが漏れると、全件が App Check で断られる）
2. Cloud Billing に予算アラートを置く（Gemini の請求が想定を超えたら知らせる）。Google Cloud コンソールの「割り当て」で Generative Language API の1日のリクエスト数を絞っておくと、なおよい
3. Secret を登録する：`firebase functions:secrets:set GEMINI_API_KEY`（Google AI Studio の有料枠のキー）、`firebase functions:secrets:set DISCORD_WEBHOOK_URL`
4. `firebase deploy --only functions,firestore`（`firestore` はルールと `firestore.indexes.json` の TTL。`--only` なしの `firebase deploy` は Hosting・関数・Firestore をすべて出す。Hosting だけなら `--only hosting`）
5. `config/aiMaterial` に `blockedNames` を書く（`tools/write-settings.mjs`）
6. サーバーの関数は Remote Config のテンプレートを読む。実行のサービス アカウント（既定は Compute Engine の既定のサービス アカウント）に Remote Config を読む権限（`Firebase Remote Config 閲覧者`）がなければ足す。読めないと入口は閉じたままになる
7. 回数券（`verifyPurchase`・`revokeVoidedPurchases`）：Google Cloud コンソールで「Google Play Android Developer API」を有効にし、Play Console の「ユーザーと権限」に 6 のサービス アカウントのメールアドレスを招待する。アプリの権限は「財務データの表示」と「注文と定期購入の管理」。権限が届くまで最大1日かかることがある。今は全関数が同じサービス アカウントで動くので、購入と関係のない関数もこの権限を持つ（購入専用のアカウントに分けるのは #393）。`revokeVoidedPurchases` は Cloud Scheduler のジョブとして出る（`firebase deploy` が API を有効にする）
8. Play Console でアプリ内アイテム（消費型）を ID `ai_material_ticket_5` で作る。値段はここで決める（コードには書かない）。数量の選択（複数個の購入）は有効にしない（数量の一部だけの返金を、返金の取り消しで拾わないため）

### 入口を初めて開ける前に

`ai_material_enabled` を開けると、利用者の文章と画像が Gemini API に送られる。開ける前に次がそろっていること。

- プライバシーポリシーと Play Console のデータセーフティに、文章と画像を Google の Gemini API に送ること（作るときの文章と画像はサーバーに残さないこと）を書いてある
- 同じく、回数券を買った人の購入の記録（匿名ログインの ID・注文番号・購入時刻）をサーバーに残すことと、その期間（上の「verifyPurchase」）を書いてある（データセーフティの「購入履歴」に当たるかは Play のヘルプで確かめる）
- 生成した画像を報告するボタンがアプリに入っている（#339）。報告では頼んだ文章をサーバーに180日残すので、そのこともプライバシーポリシーに書いてある
- Firestore の TTL が `reports.expireAt` と `reportUsage.expireAt` で有効になっている（上の 4 の deploy で入る。Firebase コンソールの Firestore →「TTL」で確かめる。入っていないと報告が消えずに残る）
- `config/aiMaterial.blockedNames` を書いてある
- 上の「本番に出す」の予算アラートと、ログベースのアラートを置いてある
- 特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）が公開されていて、アプリの購入の案内のリンクから開ける

持ち主が変わるときは、Firebase プロジェクトを移せば Firestore・Secret・関数はそのまま渡る。Gemini の API キーと Discord の Webhook は新しい持ち主のものに入れ替える。アプリを別の Play Console アカウントへ移したら、「本番に出す」の 7 の招待と権限をやり直す（しないと購入の確認が全件 `verification_failed` になり、返金の取り消しも止まる）。Firebase プロジェクトを替えると匿名ログインの ID が変わり、買われた回数券は引き継がれない。プロジェクトごと移すか、最後の購入から6か月たってから替える。
