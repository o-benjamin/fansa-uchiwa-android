# サーバー（Cloud Functions）

アプリから呼ばれるサーバーの処理。AI 素材の生成（`generateMaterial`、#315）、作った素材の報告（`reportMaterial`、#339）、回数券の購入の確認・返金の取り消し（`verifyPurchase`・`revokeVoidedPurchases`、#348）。TypeScript・Node.js 22・東京リージョン（`asia-northeast1`）。

## generateMaterial（AI 素材を1枚作る）

アプリが文章（と任意で元画像1枚）を送ると、Gemini で正方形のステッカー画像を作って返す。アプリは返った画像の背景を ML Kit で切り抜いてから、うちわに置く。

順番（`src/generateMaterial.ts`）：

1. App Check（`enforceAppCheck`）と Firebase Auth（匿名ログイン）を確かめる
2. 文章（1〜200文字）と元画像（JPEG / PNG / WebP、4MB まで）を確かめる
3. 入口スイッチ（Remote Config の `ai_material_enabled`）が閉じていれば断る
4. 断る名前の一覧（実在の人物・グループ名）に当たれば断る。全角・半角、大文字・小文字、空白と記号、カタカナ・ひらがなの違いは無視して当てる（`src/moderation.ts`）
5. 1回分おさえる（`src/quota.ts`）。無料の1回 → なければ回数券。無料の非常ブレーキと全体の安全上限もここで見る。同じ頼みの番号（`requestId`）の前の頼みで回数を使っていれば、回数を使わずに作り直すか、作っている途中なら断る（この文書の「作り直し」）
6. 文章と元画像を軽いモデルで審査する（実在の人物・既存の作品やブランドのキャラクターやロゴ・性的・暴力・ヘイト）。4 の一覧をすり抜けた名前や、実在の人物の写真の元画像はここで断る。既存の作品やブランドのキャラクター・ロゴの絵を作って渡すと他人の権利に触れるので、Gemini の画像のモデルが自分で断るかには頼らず、頼む文章と元画像の段階で断る。作った絵はまだ審査していない（作った絵の守りは #423）
7. 画像を作る。6・7 で断った・失敗したときは、5 でおさえた回数を戻す

回数券を使う順番（5）・回数を戻す動き（7）・作り直しの時間と回数（この文書の「作り直し」）・回数券でも断るとき（3 の入口スイッチ、5 の全体の安全上限）を変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す（`.agents/play-billing.md`）。

断ったときは `HttpsError` の `details.reason` に理由が入る。アプリはこの値で文言を選び、GA4 にも送る。

| reason | code | 意味 |
|---|---|---|
| `unauthenticated` | unauthenticated | ログインしていない。`details.reason` のない unauthenticated は、App Check で断ったもの（`onCall` が先に断る） |
| `invalid_prompt` / `invalid_image` | invalid-argument | 文章・元画像の形が違う |
| `disabled` | failed-precondition | 入口スイッチが閉じている |
| `blocked_name` / `moderation` | permission-denied | 名前の一覧・審査で断った（`moderation` は `details.category` に分類：`real_person` / `existing_work`（既存の作品やブランドのキャラクターやロゴ） / `sexual` / `violence` / `hate`） |
| `safety_blocked` | permission-denied | Gemini の安全フィルターで止まった（審査に送った内容が止められたときも） |
| `no_credits` | resource-exhausted | 無料を使い終え、回数券もない（購入画面へ） |
| `free_daily_limit` | resource-exhausted | 無料の非常ブレーキに当たった（無料は明日また試せる。回数券は使える） |
| `total_daily_limit` | resource-exhausted | 全体の安全上限に当たった（今日は止める） |
| `generation_failed` | unavailable | 作れなかった（Gemini の失敗・審査の答えを読めなかった・Firestore の一時的な障害）。回数は戻してあるので、やり直せる |
| `in_progress` | aborted | 同じ頼みの番号の前の頼みを、まだ作っている（この文書の「作り直し」）。この呼び出しでは回数を使っていない |

アプリは文章・元画像と一緒に頼みの番号 `requestId`（UUID）を送る。古いアプリは送らず、そのときは作り直しなしで今までどおり動く。成功したときは `{ image（base64）, mimeType, paidWith: "free" | "ticket", ticketsRemaining, model }` を返す。作り直したときの `paidWith` は前の頼みで使ったもので、`ticketsRemaining` は今の残り。`model` は画像を作ったモデルで、利用者が報告するときにアプリが `reportMaterial` へ返す。`model` がないとアプリは報告のモデル名を `unknown` として送るので、応答から外さない・名前を変えない。

画像・元画像・文章はサーバーに残さず、ログにも書かない。ログには結果の種類・モデル名・かかった時間・失敗の理由（Gemini のエラーや終わり方）を書く。回数を動かすとき（`回数をおさえた`・`回数をおさえられなかった`・`回数を戻した`・`回数券が消えていて戻さなかった`・`回数を戻せなかった`・作れたときの `generateMaterial`）は、問い合わせから回数を手で戻せるよう、uid・`callId`（1回の呼び出しごとの ID）・`reservation`（`kind` が `free` か `ticket`、回数券なら購入 ID の `packId`、おさえた日の `dateKey`）も書く（`回数をおさえられなかった` は、おさえる途中で失敗したので uid と `callId` だけで、`reservation` はない）。アプリが頼みの番号を送ったときは `requestId` も書く。作り直すときは `回数を使わずに作り直す`（`paidWith` つき）を書き、作れたときの `generateMaterial` に `redo: true` と `paidWith` を書く（`reservation` はない）。作り直しに失敗したときは `作り直せなかった`（`reason` つき）を書く。ログの保存は Cloud Logging の既定の30日で、それより前の問い合わせはログで確かめられない。保存期間を変えたら、プライバシーポリシー（`docs/privacy-policy.html`）の「データの保存期間と削除」も直す。

### Firestore（書けるのはサーバーだけ。`firestore.rules` でアプリからは読み書きできない）

| 場所 | 中身 |
|---|---|
| `users/{uid}` | `freeUsed`（無料の1回を使ったか）、`packs.{購入 ID}.remaining` / `.expiresAt`（回数券の残りと期限。`verifyPurchase` が足し、返金されたら消す）、`lastRequest`（回数を使った最後の頼み。この文書の「作り直し」） |
| `purchases/{購入 ID}` | 購入の記録（下の「verifyPurchase」）。購入 ID は購入トークンの SHA-256 |
| `dailyUsage/{日本時間の日付}` | `free` / `ticket`（その日に無料・回数券で作った回数。断った・失敗して戻した分は数えない）、`totalLimitAlerted`（全体の上限を知らせたか） |
| `config/aiMaterial` | 設定（下の表） |
| `reports/{ID}`・`reportUsage/{uid}` | 利用者からの報告（下の「reportMaterial」） |

無料は匿名ログインの ID ごとに1回だが、アプリを入れ直すと ID が変わってもう1回使える。そのため、無料の総数は全体の1日の上限（無料の非常ブレーキ）で抑える。

### 作り直し（#387）

回数を使って作ったあと、応答が端末に届く前に通信が切れたり、待っている間にアプリが止められたりすると、回数だけ減って素材が届かない。アプリは素材を受け取って置くか捨てるまで同じ頼みの番号で頼み直すので、サーバーは同じ番号が来たら次のとおり扱う（`src/quota.ts` の `reserveCreditForRequest`）。

- 前の頼みが作れていて（`lastRequest.state` が `done`）、回数を使ってから15分以内（`REDO_WINDOW_MS`。起点は回数を使った時刻 `reservedAt` で、アプリがエラーを出した時刻ではない）なら、**回数も全体の回数（`dailyUsage`）も使わずに作り直す**。作り直して渡すのは1回の頼みにつき1回まで（作れたら `redone`）。作り直しに失敗したら `done` に戻し、15分以内ならもう一度作り直せる。ただし作り直しを試せるのは失敗・止まったものも含めて2回まで（`MAX_REDO_ATTEMPTS`、`lastRequest.redoCount` で数える）なので、1回分で絵を作る（審査と画像のモデルを呼ぶ）のは多くて3回（Gemini の費用は多くて3倍）。作り直しは無料の非常ブレーキと全体の安全上限で止めず、`dailyUsage` にも数えない（回数を使った頼みの分なので）
- 前の頼みをまだ作っている（`generating`・`redoing`）なら、回数を使わずに `in_progress` で断る。ただし、そのまま3分（`STALE_CALL_MS`。関数の `timeoutSeconds` の120秒より長い）たったものは止まった呼び出しとみなし、`done` と同じく作り直せる（止まった `generating` は回数を使ったまま戻しておらず、止まった `redoing` はその作り直しが届いたか分からないため）
- 作り直し済み・作り直しを2回試した・15分を過ぎた・前の頼みは断って回数を戻した（`lastRequest` ごと消す）・番号が違う、のどれかなら、ふつうに回数を使う

作り直す絵は前と同じではなく、同じ頼みで新しく作ったもの（サーバーに絵は残さない。残すとプライバシーポリシーとデータセーフティの書き直しが要る）。作り直しの文章と元画像が前と同じかは確かめない（前の頼みは受け取れていないので、変えても1回分で1枚しか渡らない）。正規のアプリしか呼べない（App Check）ので、受け取ったのに受け取れなかったふりをするにはアプリを改造して App Check を通す必要があり、通されても損は作り直し2回分の費用。`lastRequest` の状態を進める書き込み（`頼みの状態を書けなかった`）が失敗しても応答は変えない。`generating`・`redoing` のまま残っても、3分たてば上のとおり扱われる。15分（とその起点）と1回までを変えたら、アプリのエラー文（`strings.xml` の `ai_material_error_unconfirmed`）と特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す。3分（`STALE_CALL_MS`）を変えたら、`in_progress` の文（`ai_material_error_in_progress`）も直す。

出す順番：エラー文と表記のページは回数を使わない作り直しを約束しているので、作り直しを入れた `generateMaterial` を先に本番に出し、そのあとでこのエラー文のアプリをクローズドテスト・製品版に出し、表記のページを Hosting に出す。前の `generateMaterial` は `requestId` を読まないので、逆の順番だと、文のとおりに押した人がもう1回分の回数を使う。

### 設定（`config/aiMaterial`。サーバーを出し直さずに変えられる。反映は最大1分）

| 項目 | 初期値 | 意味 |
|---|---|---|
| `freeDailyLimit` | 30 | 無料の非常ブレーキ：全体で無料で作れる1日の回数 |
| `totalDailyLimit` | 300 | 全体の安全上限：無料と回数券を合わせた1日の回数。当たったら止めて Discord に知らせる。作り直し（この文書の「作り直し」）は数えず、当たっていても止めない。断った・失敗した試行は数えないので、Gemini の請求の歯止めは別に置く（この文書の「本番に出す」の予算アラート） |
| `imageModel` | `gemini-3.1-flash-lite-image` | 画像のモデル。文字の質が足りなければ `gemini-3.1-flash-image`（約2倍の値段） |
| `judgeModel` | `gemini-3.1-flash-lite` | 審査のモデル。変えたら「入口を初めて開ける前に」の審査の確認をやり直す |
| `blockedNames` | なし | 断る実在の人物・グループ名。リポジトリが公開なので、一覧はここに置かず Firestore にだけ置く。既存の作品のキャラクター名・作品名は入れない（文のどこかに入っていれば当てるので、ふつうの言葉と同じ名前だと誤って断る。キャラクターは審査のモデルが見る） |

ドキュメントがない・型が違う項目は初期値を使う。設定を一度も読めていないときは、名前の一覧なしで動かさないよう `generation_failed` で断る。

書くときは `tools/write-settings.mjs`（使い方はファイルの先頭）。JSON に書いた項目だけを変え、ほかの項目は残す。丸ごと置き換える `--replace` は、`blockedNames` まで消えるので控えを取ってから使う。1項目だけなら Firebase コンソールの Firestore で直接直してもよい。

### 止める

- **すぐ止める**：Remote Config の `ai_material_enabled` を、初期値も条件の値もすべて `false` にして公開する。サーバーは1分以内に断り始める（アプリの入口が消えるのは、公開から最大約1時間たったあとにホームが前に出たとき）。だれに入口を出すか（条件）はアプリが決める。サーバーは端末の条件を評価できないので、どれか1つでも `true` なら開いているとみなす
- **無料だけ止める**：`config/aiMaterial.freeDailyLimit` を 0 にする。無料で使った頼みの作り直しは止めない（回数を使ってから15分の間は続く）
- **長く止める・提供をやめる**：入口を閉じた時点で期限が来ていなかった回数券に残りの回数があれば、その購入を Play Console で全額返金する（一部を使っていても全額。返金は注文ごとで、使った回数で割らない）。期限は延ばさない（6か月を超えると資金決済法の対象になるため。この文書の「verifyPurchase」）。お金が動くので、オーナーが判断し、承認を取ってから行う。どれくらい止めたら「長く」とみなすかは決まっていない（#439）。この扱いを変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）の「使える期間と条件」と「返品・キャンセル」も直す（`.agents/play-billing.md`）。行う人に要る権限は、Remote Config の公開と変更履歴（`Firebase Remote Config 管理者`）、Play Console のアプリ内アイテムの編集と「注文と定期購入の管理」、Firestore を読む（`Cloud Datastore 閲覧者`）、ログを見る（`ログ閲覧者`）
  1. 「すぐ止める」で入口を閉じ、回数券が使われないようにする。すでに閉じているなら、閉じた日時を Remote Config の変更履歴で確かめる。Play Console でアプリ内アイテム `ai_material_ticket_5` を無効にし、新しく買えないようにする（長く止めたあとに再開するときは有効に戻す）。閉じてから5分は待つ（サーバーが断り始めるまで最大1分）。時間切れのあとも処理が続いて、あとから回数が戻ることがある（この文書の generateMaterial の「困ったとき（Cloud Logging で探す）」の「`回数をおさえた` だけ」）。その分は 5 のくり返しで拾う
  2. Firestore コンソールで `purchases` を、`expiresAt` が 1 の閉じた日時より後のもので絞り込む
  3. 残ったそれぞれについて、`voidedAt` があれば返金済みなので飛ばす。なければ `uid` で `users/{uid}` を開き、`packs.{購入 ID}.remaining`（購入 ID は `purchases` のドキュメント ID）が1以上の購入を返金の対象にする。0 は使い切りで、`packs.{購入 ID}` がないのは返金で取り消し済み
  4. 対象の購入ごとに、`purchases` の `orderId`（`GPA.…`）を Play Console の「注文管理」で探して全額返金する。注文管理で返金済みになっていれば飛ばす。ただし、注文管理で返金済みなのに 3 で `voidedAt` がなかったなら、返金した回数券の取り消しが済んでいない。「困ったとき（回数券。Cloud Logging で探す）」の `返金された回数券を取り消せなかった` を見る。返金した `orderId` は手元に控える（5 で同じ購入を拾ったときに飛ばすため）。`orderId` が `null` の購入は Play に注文がなく、代金を受け取っていないので返金しない。次の 4:00 に動く `revokeVoidedPurchases` が残りの回数を消すので、この関数は止めない
  5. 1週間あけて 2〜4 をもう一度行い、前の回より後に足された購入（`grantedAt`）がなくなったら終わる。`verifyPurchase` は入口スイッチを見ないので、入口が端末から消えるまで（最大約1時間）に AI 素材の画面を開いた人の購入には、閉じたあとにも回数が足されることがある。消えたあとに支払いが済んだ購入はアプリが送らないので届かず、3日で Play が自動で返金する（`purchases` には載らない）。前の回に控えた購入は、`purchases` に `voidedAt` が付いたか（付くのは返金の次の 4:00）を確かめ、付いていなければ 4 と同じく `返金された回数券を取り消せなかった` を見る

### 困ったとき（Cloud Logging で探す）

- **`回数を戻せなかった`**：断った・失敗したのに回数が減ったままになっている。ログの `uid` と `reservation` を見て、`kind` が `free` なら `users/{uid}.freeUsed` を `false` に、`ticket` なら `users/{uid}.packs.{購入 ID}.remaining` を1増やす。回数券はお金で買った回数なので、見つけたらすぐ戻す
  - 回数券は、先に `packs.{購入 ID}` があるか確かめる。ないときは返金で取り消し済みで、お金が戻っているので戻さない（`refundCredit` と同じ）。ないのに `remaining` を足すと `expiresAt` のない回数券ができ、数えられないので何も起きない
  - `expiresAt` を過ぎているときは、足しても使えない。同じ利用者の期限内のほかの回数券があればそちらに1足し、なければ期限切れで戻せないと伝える（期限切れの回数券の残りは、どのみち使えなくなっている）
- **「作れたか確かめられませんでした」と出た人から連絡が来た**：利用者 ID のない連絡には、先に返信で、設定画面のいちばん下の「利用者 ID」（タップでコピーできる。同じ画面の「インストール ID」ではない）と、そのあとアプリを入れ直したりデータを消したりしたかを聞く。エラー文（`strings.xml` の `ai_material_error_unconfirmed`）と特定商取引法に基づく表記でも頼んでいるが、添えずに送る人がいる。「利用者 ID」の行が出ていないと返ってきたら、入れ直したかデータを消したあとで、画面からは前の ID が分からない。回数券を買っていれば注文番号（`GPA.…`）を聞き、`purchases` を `orderId` で探して、出た `uid` で手順 1 から探す。無料の1回だけなら探せない
  1. 連絡の利用者 ID で、Cloud Logging を `jsonPayload.uid="<利用者 ID>"` で、利用者が書いた日時（連絡が来た時刻ではない）の前後を探す。利用者 ID はアプリの設定画面の「利用者 ID」（匿名ログインの ID）で、同じ画面の「インストール ID」（#411）とは別物。インストール ID で探してもログは出ない
  2. 戻すかを決める前提：ログで減ったままと分かる組（`回数を戻せなかった`・`回数をおさえた` だけ）と、おさえたかどうか分からない組（`回数をおさえられなかった` だけ）は、何度目でも戻す。`result: "ok"` の組は、受け取れたかをサーバーでは確かめられないので、連絡を信じて戻す。ただし同じ利用者 ID への手での戻しが30日に3回目になるときは、`result: "ok"` の組は戻さずに断ってよい（受け取ったあとに連絡すれば何度でも戻ってしまうため。reportMaterial の「同じ `uid` から同じ内容が続くときは、いたずらとして扱ってよい」と同じ考え）。手での戻しはログにも Firestore にも残らないので、戻したら返信に利用者 ID・戻した日時・`callId` を書いておき、同じ利用者 ID への過去30日の返信で数える
  3. ログを `callId` で組にする（`reservation` は同じ日に同じ回数券で作ると同じ値になるので、組にならない）。組ごとに、最初の行（`回数をおさえた` か `回数を使わずに作り直す`）と、そのあとに何が出たかで見分ける
     - `回数を戻した`：断った・失敗した分で、回数は戻してある（減っていない）
     - `回数券が消えていて戻さなかった`：返金で取り消し済みの回数券。お金が戻っているので戻さない
     - `回数を戻せなかった`：減ったまま。この「困ったとき」の「`回数を戻せなかった`」の手順で戻す
     - `generateMaterial` の `result: "ok"`：作れて回数が減った。ふつうに受け取れたときも同じ並びで、受け取れなかったか（通信が切れた・応答の読み取りや保存に失敗した・待っている間にアプリが止められた）はサーバーでは分からない。利用者が書いた日時に合う組だけを、連絡のあった回数まで、この「困ったとき」の「`回数を戻せなかった`」の手順で戻す。同じ `requestId` のあとの組に `回数を使わずに作り直す` と `redo: true` の `result: "ok"` があれば、作り直しで1枚渡しているので、手順 2 の「30日に3回目」のときと同じく戻さずに断ってよい（作り直しも受け取れなかったと書いてあれば戻す）
     - `回数を使わずに作り直す` で始まる組：回数を動かしていないので、戻すものはない。`作り直せなかった` が続けば作り直しに失敗していて、前の組の回数は減ったまま（前の組を `result: "ok"` の組と同じく扱う）
     - `回数をおさえた` だけ：作っている途中で関数が止まった（`src/index.ts` の `timeoutSeconds`、いまは120秒の時間切れなど）。時間切れで応答を返したあとも処理が続き、あとから `回数を戻した`・`回数を戻せなかった`・`result: "ok"` などが出ることがあるので、その日の終わりまで見てから決める。それでも出なければ減ったままなので、この「困ったとき」の「`回数を戻せなかった`」の手順で戻す
  4. `回数をおさえられなかった`（ERROR）だけの `callId` があるとき：Firestore の障害で、おさえたかどうか分からない（まれに、書き込めたのに失敗として返る）。見分けられないので1回戻す。ログに `reservation` はないので、戻す先は `users/{uid}` を見て決める：期限内の回数券（`remaining` が0のものも含む）があれば期限のいちばん近いものの `remaining` を1増やし、なければ `freeUsed` を `false` にする（どちらを使ったかは分からないので、利用者に損のない回数券の側に足す。回数券がなければ、おさえたのは無料の1回）
  5. 何も出ないときは、結論の前に、インストール ID と取り違えていないか・アプリを入れ直していないか（ID が変わる。回数券なら、この文書の「困ったとき（回数券。Cloud Logging で探す）」の注文番号で探す手順へ）・日時の幅が足りているかを確かめる。それでも出なければ、サーバーに届いていないので回数は減っていない。30日より前の連絡は、ログが消えていて見分けられない
- **「前に頼んだ分をまだ作っています」が続くと連絡が来た**：Firestore で `users/{uid}.lastRequest` の `state` と `updatedAt` を見る。`generating`・`redoing` のままでも、`updatedAt` から3分（`STALE_CALL_MS`）たてば `in_progress` では断らなくなる（そのあと押したときに作り直すか、ふつうに回数を使うかは、この文書の「作り直し」の条件どおり）。同じ画面のまま3分ほど待ってから押してもらう。`in_progress` で断った分は回数を使っていない。最初に押した分が届かずに減ったままかは、この「困ったとき」の「「作れたか確かめられませんでした」と出た人から連絡が来た」の手順で見る
- **`審査のモデルを呼べなかった` / `画像のモデルを呼べなかった` が続く**：ログの `error`（Gemini の `error.status` と `error.message`）を見る。`NOT_FOUND` はモデルの廃止か名前の打ち間違いなので `config/aiMaterial` の `judgeModel` / `imageModel` を差し替える。`PERMISSION_DENIED` / `UNAUTHENTICATED` は API キーの失効なので Secret を入れ直す。`RESOURCE_EXHAUSTED` は割り当ての超過
- **`画像が返らなかった` が続く**：ログの `finishReason` を見る。前置き（`src/gemini.ts` の `STICKER_PREFIX`）かモデルが合っていない
- **`設定を読めなかった` / `回数をおさえられなかった`**：Firestore の障害。続くなら入口スイッチで止める
- **審査で断られたという問い合わせが続く**：`generateMaterial` のログ（`result: "moderation"`）を `category` ごとに見る。分類は Cloud Logging でしか見られない（アプリは `details.category` を読まず、GA4 に送るのは `moderation` まで）
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

`purchases/{購入 ID}` に `uid`・`orderId`（Play の注文番号 `GPA.…`）・`tickets`・`purchasedAt`・`expiresAt`・`grantedAt`・`consumed` を書く。購入トークンそのものは保存しない。記録は、回数券の期限（`expiresAt`）から30日たったら消してよい（返金の取り消しは過去30日分しか見ないため）。ただし、入口を閉じているあいだ（generateMaterial の「止める」の「長く止める・提供をやめる」の返金が済むまで）は消さない（閉じた時点で期限が来ていなかった購入を返金の対象として探すため）。自動で消す仕組みはまだない。

## revokeVoidedPurchases（返金された回数券を取り消す。毎日 4:00）

Voided Purchases API で過去30日分の返金・取り消しを読み、その購入の回数券（`users/{uid}.packs.{購入 ID}`）を消す（`src/voidedPurchases.ts`）。使った分は戻さない。`purchases` に `voidedAt`・`voidedReason`・`voidedSource`・`ticketsRevoked`（取り消したときに残っていた回数）を書く。毎日30日分を見直して取り消し済みは飛ばすので、止まっても30日以内なら次に動いたときに拾える（超えた分は取り消せない）。1件が失敗しても、ほかの件は続ける。取り消し方を変えたら、特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）も直す（`.agents/play-billing.md`）。

### 困ったとき（回数券。Cloud Logging で探す）

- **問い合わせ（「払ったのに増えない」「入れ直したら消えた」）**：利用者に注文番号（`GPA.…`、Google Play の購入履歴やメールにある）を聞き、`purchases` を `orderId` で探す
  - 見つからない：サーバーで回数を足しておらず、消費もしていない。3日で Play が自動で返金するので、Play Console の「注文管理」で状態を見る。ログを `orderId` で探すと、断った理由（`pending` など）が分かる
  - `voidedAt` がある：返金済み
  - 別の `uid` に付いている（入れ直して匿名ログインの ID が変わった。ログの `owned_by_other` に両方の `uid` がある）：Play Console で返金する。翌日の `revokeVoidedPurchases` が前の ID の残りを消す
- **`Play で購入を確かめられなかった` が続く**：ログの `error`（`Play Developer API が <状態>…`）を見る。403（`PERMISSION_DENIED`）は、購入専用のサービス アカウント（`purchase-functions@…`）が Play Console の「ユーザーと権限」にいないか権限が足りない（下の「本番に出す」の 8）。401 はスコープかアカウントの設定。Play の障害なら待つ（利用者には `verification_failed` で返り、回数は足していない）
- **`購入の記録を読めなかった`・`回数を足せなかった`・`回数を読めなかった`**：Firestore を読み書きできなかった（利用者には `verification_failed`）。ログの `error` が `7 PERMISSION_DENIED` なら、購入専用のサービス アカウントに `Cloud Datastore ユーザー` のロールがない（「本番に出す」の 4）
- **`消費できなかった`**：回数は足してあり、アプリが送り直せば消費をやり直す。3日続くと Play が自動で返金し、回数券も取り消される。Play の消費と Firestore の書き込みのどちらの失敗もこのログになるので、続くならログの `error` を見る。`Play Developer API が 403` なら「本番に出す」の 8 の権限、`7 PERMISSION_DENIED` なら 4 のロール
- **`revokeVoidedPurchases` が失敗している**（毎日の件数のログ `revokeVoidedPurchases` が出ない・この関数の ERROR がある）：止まっている間、返金された回数券は使えるまま。返金の一覧を Play から読めないと関数ごと失敗する。その ERROR のメッセージが `Play Developer API が 403…` なら「本番に出す」の 8 の権限。直したら Google Cloud コンソールの Cloud Scheduler でジョブを強制実行し、件数のログが出るのを確かめる。30日を超えて止まった分は取り消せない
- **`返金された回数券を取り消せなかった`**：その1件だけ取り消せなかった（翌日やり直す。ほかの件と件数のログは続く）。件数のログ（`revokeVoidedPurchases`）の `failed` が毎日 `voided` と同じで、`error` が `7 PERMISSION_DENIED` なら、購入専用のサービス アカウントに `Cloud Datastore ユーザー` のロールがない（「本番に出す」の 4。30日を超えた返金は取り消せなくなるので急ぐ）。そうでなければ、ログの `orderId` で `purchases` の記録の中身を見る
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
4. 回数券の関数（`verifyPurchase`・`revokeVoidedPurchases`）を動かす購入専用のサービス アカウントを作る（#393）。Play Console で返金ができる権限（8）は、このアカウントだけに付ける。このアカウントがないと、5 の deploy でこの2つの関数を作れない。ほかの関数は既定のサービス アカウントのまま
   - Google Cloud コンソールの「IAM と管理」→「サービス アカウント」で、ID `purchase-functions` で作る（`src/index.ts` の `purchaseServiceAccount` の `@` の前と同じ。変えるときは両方を直す）。説明欄には「verifyPurchase・revokeVoidedPurchases 用。Play Console で返金の権限を持つ」と書く。鍵（JSON キー）は作らない（関数は鍵なしでこのアカウントとして動く）
   - 「IAM」で、このアカウントにロール `Cloud Datastore ユーザー`（`roles/datastore.user`。Firestore）と `ログ書き込み`（`roles/logging.logWriter`）を付ける
   - デプロイする人がプロジェクトのオーナーでなければ、その人に、このアカウントに対する `サービス アカウント ユーザー`（`roles/iam.serviceAccountUser`）を付ける
5. `firebase deploy --only functions,firestore`（`firestore` はルールと `firestore.indexes.json` の TTL。`--only` なしの `firebase deploy` は Hosting・関数・Firestore をすべて出す。Hosting だけなら `--only hosting`）
   - 出したあと、Google Cloud コンソールの Cloud Run で `verifypurchase` と `revokevoidedpurchases` のサービス アカウントが `purchase-functions@…` になっていて、ほかの関数は Compute Engine の既定のサービス アカウントのままかを確かめる（Cloud Run のサービス名は関数名の小文字）。Cloud Scheduler の `revokeVoidedPurchases` のジョブも、サービス アカウントが `purchase-functions@…` になっているかを見る
6. `config/aiMaterial` に `blockedNames` を書く（`tools/write-settings.mjs`）
7. `generateMaterial` は Remote Config のテンプレートを読む。動かすサービス アカウント（Compute Engine の既定のサービス アカウント。購入専用のアカウントには要らない）に Remote Config を読む権限（`Firebase Remote Config 閲覧者`、`roles/firebaseremoteconfig.viewer`）がなければ足す。読めないと入口は閉じたままになる
8. 回数券（`verifyPurchase`・`revokeVoidedPurchases`）：Google Cloud コンソールで「Google Play Android Developer API」を有効にし、Play Console の「ユーザーと権限」に 4 の購入専用のサービス アカウント（`purchase-functions@<プロジェクト ID>.iam.gserviceaccount.com`）を招待する。アプリの権限は「財務データの表示」と「注文と定期購入の管理」。権限が届くまで最大1日かかることがある。Compute Engine の既定のサービス アカウント（`<プロジェクト番号>-compute@developer.gserviceaccount.com`）は招待しない（招待すると、購入と関係のない関数も返金ができるようになる）。アカウントを分ける前（#393 より前）の手順で既定のサービス アカウントを招待していたら、「ユーザーと権限」から外す。`revokeVoidedPurchases` は Cloud Scheduler のジョブとして出る（`firebase deploy` が API を有効にする）
9. Play Console でアプリ内アイテム（消費型）を ID `ai_material_ticket_5` で作る。値段はここで決める（コードには書かない）。数量の選択（複数個の購入）は有効にしない（数量の一部だけの返金を、返金の取り消しで拾わないため）

### 入口を初めて開ける前に

`ai_material_enabled` を開けると、利用者の文章と画像が Gemini API に送られる。開ける前に次がそろっていること。

- プライバシーポリシーと Play Console のデータセーフティに、文章と画像を Google の Gemini API に送ること（作るときの文章と画像はサーバーに残さないこと）を書いてある
- 同じく、回数券を買った人の購入の記録（匿名ログインの ID・注文番号・購入時刻）をサーバーに残すことと、その期間（上の「verifyPurchase」）を書いてある（データセーフティの「購入履歴」に当たるかは Play のヘルプで確かめる）
- プライバシーポリシーの「データの保存期間と削除」に、ログに利用者 ID や注文番号が残り、30日で消えることが書いてある（回数を動かすログに uid・`callId`・購入 ID を書くため。release/v2.12.0 で入れた文で、#398 の公開ページの確認で見る）
- プライバシーポリシーの公開ページの「データの保存期間と削除」が、設定画面の「利用者 ID」を添えて削除を頼む形になっていて、「お問い合わせ」に問い合わせのメールの扱いが書いてある（#416）。特定商取引法に基づく表記も利用者 ID を添えた連絡を頼み、問い合わせでメールアドレスと利用者 ID が結びつくので、この2つの文は表記のページより先か同時に公開しておく。表記のページのリンク先（`web/public/privacy-policy.html`）は同じ `firebase deploy --only hosting` で出る。Play Console のポリシーの URL がまだ Pages なら、Pages（`master`）にも出しておく（`release/vX.Y.Z` → `master` より先に出すときは、`.agents/privacy-policy.md` の「公開のしかた」の `master` 向けの PR）
- 削除の依頼（利用者 ID・注文番号か受け取った日時・180日以内の報告の日と文章のどれか）から記録を探して消す手順が、この文書にある（#399）。ページが、お知らせいただいたものから利用者 ID を探して削除すると約束しているため
- 生成した画像を報告するボタンがアプリに入っている（#339）。報告では頼んだ文章をサーバーに180日残すので、そのこともプライバシーポリシーに書いてある
- Firestore の TTL が `reports.expireAt` と `reportUsage.expireAt` で有効になっている（上の 5 の deploy で入る。Firebase コンソールの Firestore →「TTL」で確かめる。入っていないと報告が消えずに残る）
- `config/aiMaterial.blockedNames` を書いてある
- 審査のモデルが次の答えを返すことを、オーナーの端末だけ入口を開けて確かめてある（開け方は #411 の、設定画面のインストール ID で Remote Config の条件を作るやり方）。どの分類で断ったかは `generateMaterial` のログの `category` で見る（アプリの文言は分類で変わらない）。審査は回数をおさえたあとに走るので、使える回数（無料の1回か回数券）が要る。断られれば回数は戻るが、作れれば1回分減る。回数がなければ `users/{uid}.freeUsed` を `false` に戻す
  - 実在の人物名 → `real_person`
  - 作品のキャラクター名・作品名・名前を出さずに特徴だけを並べた頼み → `existing_work`
  - 一般的なキャラクター（「うさぎのキャラ」「王子様」など）・ふつうの素材（ハート・推しの色など） → `none`
  - 作品のキャラクターや作品名の頼みが `none` で通ったら、入口を広げず `src/moderation.ts` の `JUDGE_INSTRUCTION` か `judgeModel` を見直す。一般的なキャラクターが `existing_work` で断られたら、`JUDGE_INSTRUCTION` の none の例を足す
- 上の「本番に出す」の予算アラートと、ログベースのアラートを置いてある
- 特定商取引法に基づく表記のページ（`web/public/tokushoho.html`）が公開されていて、アプリの購入の案内と「作れたか確かめられませんでした」の文言の下のリンクから開ける
- 「作れたか確かめられませんでした」と出た人から連絡を受けたときに、回数を戻す利用者を探せる（#387 の③：アプリの設定画面に「利用者 ID」が、インストール ID と区別できる見出しで出ていて、特定商取引法に基づく表記の連絡先とエラー文（`strings.xml` の `ai_material_error_unconfirmed`）に「利用者 ID」を添えるよう書いてある。ログの探し方は、この文書の generateMaterial の「困ったとき（Cloud Logging で探す）」）。これがないと、連絡を受けても `users/{uid}` を特定できない

持ち主が変わるときは、Firebase プロジェクトを移せば Firestore・Secret・関数はそのまま渡る。Gemini の API キーと Discord の Webhook は新しい持ち主のものに入れ替える。アプリを別の Play Console アカウントへ移したら、「本番に出す」の 8 の招待と権限をやり直す（しないと購入の確認が全件 `verification_failed` になり、返金の取り消しも止まる）。Firebase プロジェクトを替えると匿名ログインの ID が変わり、買われた回数券は引き継がれない。プロジェクトごと移すか、最後の購入から6か月たってから替える。替えたら、新しいプロジェクトで「本番に出す」の 4 と 8 をやり直す（アドレスはプロジェクト ID から決まるので、コードは直さない）。
