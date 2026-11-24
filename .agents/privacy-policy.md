# プライバシーポリシー（`web/public/privacy-policy.html`・`docs/privacy-policy.html`）

アプリから外へ送るもの・サーバーに残すものを変えたら、このページとデータセーフティの申告を一緒に直し、この文書の「公開のしかた」で2か所とも公開します。ページの記述が実際の動きとずれると、利用者に事実と違う説明をすることになります。

## 公開のしかた

- 同じページを2か所に置いています。中身は同じにします（片方を直したら、もう片方へ `cp` で写します）。`firebase.json` の hosting の `predeploy` が2つを `diff` で比べ、違えば Hosting に出すのを止めます。Windows では WSL から出します（フックは Node の `shell: true` で動くので、Git Bash や PowerShell から出しても cmd.exe で動きます。2つが違うときは、案内の代わりに `'{'` が見つからないというエラーで止まります。直前の `Files … differ` が、2つが違うという意味です）。案内の文は単引用符が外れて渡るので、ASCII の記号（`(` `)` `'` `;` `&` `*` など）を入れません
    - Firebase Hosting：`web/public/privacy-policy.html`（https://fansauchiwa-578d22ff.web.app/privacy-policy.html）。v2.13.0 からのアプリ・公式サイト・特定商取引法に基づく表記のページが開きます。ブランチにマージしただけでは出ません。出し方（承認・どのブランチから出すか・出す前の確かめ）は `.agents/play-billing.md` の「特定商取引法に基づく表記」の、`firebase deploy --only hosting` で出す箇条と同じです（`web/public` がまるごと置き換わるため）
    - GitHub Pages：`master` の `docs/privacy-policy.html`（https://o-benjamin.github.io/fansa-uchiwa-android/privacy-policy.html）。v2.12.x までのアプリが開きます。`release/vX.Y.Z` に入っただけでは公開されません。`release/vX.Y.Z` → `master` は公開の24時間後です
- `release/vX.Y.Z` でこのページを直したら、その版の製品版を出す前に、リリースを進める人がオーナーの承認をもらって Hosting に出します。送るもの・残すものが変わった新しい版のアプリが開くのは Hosting なので、Pages（`master`）より先に出します
- 機能の公開より先にページを出したいときは、2つのファイルだけを変える `master` 向けの PR を作り、マージのあと Hosting にも出します（例：#283・#341。Pages だけだったころ）
- Pages は、v2.13.0 からの版が行き渡り、リポジトリを非公開にするときに止めます。判断のしかた・止め方・消すものは #451 にあります
- まだ提供していない機能の説明がページに載っているときは、その項の頭と「本アプリが利用する第三者サービス」の囲みに「今後のアップデートで提供する予定」の文を置きます（消す時期は下の「直すきっかけ」）。今は AI素材の機能（#315）がこの状態です（v2.12.0 に AI素材を入れなかったため。#398）。囲みの文は目的に「AI素材」とある行を指しているので、文が残っている間は、ほかの行の目的に「AI素材」と書きません。ページの文を消すときに、この箇条の「今は」から「書きません」までと、下の「直すきっかけ」の AI素材の例（括弧の中）も消します
- URL を使っている場所：
    - Hosting の URL：`app/src/main/java/com/fansauchiwa/home/HomeScreen.kt`（メニューの「プライバシーポリシー」）。`web/public/index.html` と `web/public/tokushoho.html` は相対リンク（`privacy-policy.html`）です。URL は Firebase のプロジェクト ID（`.firebaserc`）から決まり、出回っている版は前の URL を開き続けるので、持ち主が変わるときはプロジェクトを替えずにプロジェクトごと移します（`.agents/play-billing.md` の「持ち主が変わるとき」）。それでも替えるときは、`git grep -n 'fansauchiwa-578d22ff.web.app'` で出るアプリのリンク（ポリシーのほか、公式サイト・使い方・特定商取引法に基づく表記）をすべて新しいプロジェクトのドメインに直し、前のプロジェクトの Hosting は消さずに残します（消すと、出回っている版のリンクが開かなくなります）
    - Pages の URL：v2.12.x までのアプリが開きます。Pages の URL は転送されないので、Pages を止めるまでリポジトリの名前と持ち主を変えません
    - Play Console の「アプリのコンテンツ」：Hosting に出したら、Play Console の権限がある人が Hosting の URL に変えます。ストアの掲載ページから開くポリシーにも、同じ版を出すためです（#451）
- 改訂日（ページの末尾）は、PR では直した日にしておきます。先に公開されたほう（ふつうは Hosting）の日とずれていたら、Hosting に出した人が、出したブランチ（`release/vX.Y.Z` から出したときはそのブランチ）で2つとも直し、同じブランチから Hosting に出し直します。あとから Pages に出たときは直しません（今回の AI素材の改訂の分は、#398 の「入口（`ai_material_enabled`）を開ける前に」の項で確かめます）

## 直すきっかけ

次のどれかをしたら、ページの該当する節を直します。

- 外部サービス・SDK を足す・替える・外す（「本アプリが利用する第三者サービス」の囲みと、「第三者への提供」のリンク）
- Analytics のイベント・パラメータで、新しい種類の情報を送る（「情報の収集と利用」の例。例：#362 の共有先のアプリ）
- AI素材のサーバー（`functions/`）で、保存する項目・保存期間（TTL）・ログに書くもの・知らせる先（Discord）を変える。Gemini 以外のモデルに替える、API キーを替える（「AI素材の機能で送信・保存する情報」「データの保存期間と削除」）。利用者 ID（`uid`）を持つ記録を足したら、`functions/README.md` の「削除の依頼が来たら」の手順「消す」にも足す
- バックアップの決まり（`app/src/main/res/xml/backup_rules.xml`・`data_extraction_rules.xml`）を変える（「端末のバックアップ」「オプトアウト権」）
- 削除の依頼で添えてもらう利用者 ID の見出し・場所を変える・外す（`strings.xml` の `settings_user_id`・`settings_installation_id`、`settings/SettingsScreen.kt` の `AppInfoSection`）。直すのは「データの保存期間と削除」の頼み方と「オプトアウト権」の最後の1文。特定商取引法に基づく表記も同じ見出しで案内しているので、`.agents/play-billing.md` の「次を変えたら、ページも直してください」も見る。ほかに案内している先（AI 素材のエラー文・`functions/README.md`）は `.agents/firebase-auth-app-check.md` の「利用者の ID を画面に出す」
- 問い合わせのメールの使い道・保存のしかた・受け取る受信箱を変える（「お問い合わせ」の2段落目）
- Remote Config で配る値の種類を足す（「本アプリが利用する第三者サービス」の Firebase Remote Config の行。`.agents/remote-config.md` の「初めて公開する前に」も同じ確認）
- まだ提供していない機能を提供し始める（AI素材なら Remote Config のキー `ai_material_enabled` で入口を開ける。キーの定義と開ける前の確認は、`.agents/remote-config.md` と `functions/README.md` の「入口を初めて開ける前に」）。その前に「今後のアップデートで提供する予定」の文（`git grep 今後のアップデート` で出る）を消し、消した版が2か所とも（Pages は `master` から、Hosting は `firebase deploy` で）公開されてから開けます。`release/vX.Y.Z` で消しても `master` に入るのは公開の24時間後なので、それより先に開けるときは「公開のしかた」の `master` 向けの PR で消します。機能を出さないと決めたときは、その機能の記述をページからすべて消します。AI素材なら `git grep -n -e AI素材 -e '利用者 ID' -e 回数券 web/public/privacy-policy.html` で洗い出し（端末のバックアップの項は `allowBackup` のために要るので、AI素材の利用者 ID の部分だけ消す）、「第三者への提供」のリンクのうち AI素材の分（Gemini・Play 請求サービス・Firebase の Authentication 以降・Discord。grep には出ない）と、このファイルの AI素材の箇条と「節ごとの根拠」も消します

あわせて Play Console のデータセーフティの申告も見直します。申告は Play Console の権限がある人がします。

## 節ごとの根拠（2026-11 時点）

この表は、AI素材のコードが `release/vX.Y.Z` に入る前に `feature/348-ai-material-billing-server`（#391）の c2d987c で確かめました（設定画面の「利用者 ID」と問い合わせのメールの2行は、#416 で `release/v2.13.0` の 2003880 で、回数の行の `lastRequest` は #387 で `feature/387-redo-same-request` の 2afa585 で、Google Play の行は #428 で `feature/428-update-prompt` の 737f7ce で確かめました）。AI素材のコードは `release/v2.13.0` に入ったので、`master` に入る前に表を見直し（#398）、この段落は消します。

| ページの記述 | 根拠 |
|---|---|
| AI素材で送った文章と画像をサーバーに残さない、ログに書かない | `functions/src/generateMaterial.ts` のログ、`functions/README.md` |
| ログに利用者 ID や注文番号が残ることがある、ログは30日で消える | `generateMaterial.ts`・`verifyPurchase.ts`・`voidedPurchases.ts` のログ。30日は Cloud Logging の `_Default` バケットの既定の保存期間（変えたらページも直す） |
| 購入の記録（注文番号・商品・日時・期限、トークンは残さない。注文番号はない場合がある） | `functions/src/verifyPurchase.ts` の「購入の記録」 |
| 返金で回数券を取り消す | `functions/src/voidedPurchases.ts` |
| 報告の内容は180日、1日ごとの報告の数は2日で消える。Discord には理由とモデル名だけ | `functions/src/reportMaterial.ts`（`REPORT_RETENTION_DAYS`・`REPORT_USAGE_RETENTION_DAYS`、TTL、`notifyOwner`） |
| 回数（無料の1回、回数券の残りと期限）と、最後に回数を使った頼み（番号・日時・作れたか・作り直した回数） | `functions/src/quota.ts`（`lastRequest` は #387 の作り直し。`functions/README.md` の「作り直し」） |
| App Check は素材を作る・回数券を買う・報告するとき | `functions/src/index.ts` の `enforceAppCheck: true` |
| 利用者 ID は AI素材の機能を初めて使うときに作る。作るとき IP アドレスが Firebase に記録される | `app/src/main/java/com/fansauchiwa/data/repository/AuthRepository.kt`（使うときに `signInAnonymously`）、[Firebase のデータの開示](https://firebase.google.com/docs/android/play-data-disclosure)（2026-10-16 に確認） |
| Gemini は有料の契約で、送った内容は学習に使われない | `functions/src/index.ts` の `GEMINI_API_KEY` のコメント、[Gemini API 追加利用規約](https://ai.google.dev/gemini-api/terms)の有料サービスの項。無料枠のキーにすると、この記述と食い違う |
| Google Play のアプリ内レビュー・アプリ内アップデートを、レビューの受け付けと新しい版の確認・更新に使う | `gradle/libs.versions.toml` の `review-ktx`（#243）・`app-update-ktx`（#428）。扱うデータは Google の [In-App Updates](https://developer.android.com/guide/playcore/in-app-updates) と [In-App Review](https://developer.android.com/guide/playcore/in-app-review) の「Data safety」の節（2026-11-24 に確認）。更新は端末の情報・アプリの版・入っているモジュールの一覧を、新しい版の有無と大きさを調べるために使い、第三者に渡さない。レビューは入力した評価と文章を Play ストアのレビューに使う |
| 顔の確認と背景の切り抜きは端末内 | 切り抜きは `app/src/main/java/com/fansauchiwa/data/infra/ImageProcessingLocalSource.kt`、顔の確認も同じファイル（#338 で追加）。どちらも `gradle/libs.versions.toml` の ML Kit（Play 開発者サービスの端末内モデル） |
| うちわのデータ・挿入した画像・利用者 ID がバックアップに入る | `app/src/main/AndroidManifest.xml` の `allowBackup="true"`、`backup_rules.xml`・`data_extraction_rules.xml` に除外がない |
| 設定画面の「利用者 ID」を添えれば削除を頼める。画面に出ていなければ、その端末のアプリにはいま利用者 ID がない（まだ使っていなければ記録もない。入れ直した・データを消したなら前の ID の記録が残る）。報告の日と文章で頼めるのは180日以内 | `app/src/main/java/com/fansauchiwa/settings/SettingsScreen.kt` の `AppInfoSection`（`userId` が null なら行を出さない）、`app/src/main/java/com/fansauchiwa/data/repository/AppInfoRepository.kt`（`getCurrentUserId()`。サーバーを呼ぶ前の `AuthRepository.prepareForServerCall()` まで作らない）、`functions/src/reportMaterial.ts` の `REPORT_RETENTION_DAYS`。利用者 ID で記録を探して消す手順は `functions/README.md` の「削除の依頼が来たら」 |
| 問い合わせのメールの利用者 ID・メールアドレスは、回答・回数の確認と戻し・返金・削除にだけ使う。期限を決めずに保存し、頼まれたら消す。記録の削除を頼まれたら、終えたと知らせたあとにそのやりとりも消す | 運用（`fansauchiwa.app@gmail.com` の受信箱。自動で消す・転送する設定は置かない）。`functions/README.md` の generateMaterial の「困ったとき（Cloud Logging で探す）」は、手での戻しを同じ利用者 ID への過去30日の返信で数えるので、返信を含むやりとりは、頼まれたとき以外は30日より早く消さない（頼まれて消すと数えられなくなるが、1回の戻しは1枚分なので受け入れる）。消すやりとりの範囲は `functions/README.md` の「削除の依頼が来たら」の手順「終えたことを返信で知らせ、そのあとでメールを消す」にある。保存の期限を決めたら（#444）、ページとこの行とその手順を直す |
