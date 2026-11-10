# プライバシーポリシー（`docs/privacy-policy.html`）

アプリから外へ送るもの・サーバーに残すものを変えたら、このページとデータセーフティの申告を一緒に直します。ページの記述が実際の動きとずれると、利用者に事実と違う説明をすることになります。

## 公開のしかた

- GitHub Pages が `master` の `docs/` から出します（https://o-benjamin.github.io/fansa-uchiwa-android/privacy-policy.html）。`release/vX.Y.Z` に入っただけでは公開されません。`release/vX.Y.Z` → `master` は公開の24時間後です
- 機能の公開より先にページを出したいときは、`docs/privacy-policy.html` だけを変える `master` 向けの PR を作ります（例：#283・#341）
- まだ提供していない機能の説明がページに載っているときは、その項の頭と「本アプリが利用する第三者サービス」の囲みに「今後のアップデートで提供する予定」の文を置きます。その機能を提供し始める前（AI素材なら入口 `ai_material_enabled` を開ける前）に、`master` で公開されるように消します。今は AI素材の機能（#315）がこの状態です（v2.12.0 に AI素材を入れなかったため。#398）
- URL を使っている場所：`app/src/main/java/com/fansauchiwa/home/HomeScreen.kt`（メニューの「プライバシーポリシー」）、`web/public/index.html`、Play Console の「アプリのコンテンツ」。リポジトリを別のアカウントに移すと Pages の URL は転送されないので、この3か所を直します
- 改訂日（ページの末尾）は、PR では直した日にしておきます。`master` に入って公開された日とずれていたら、公開ページを確かめる人が `master` 向けの PR で直します（今回の AI素材の改訂の分は、#398 の「入口（`ai_material_enabled`）を開ける前に」の項で確かめます）

## 直すきっかけ

次のどれかをしたら、ページの該当する節を直します。

- 外部サービス・SDK を足す・替える・外す（「本アプリが利用する第三者サービス」の囲みと、「第三者への提供」のリンク）
- Analytics のイベント・パラメータで、新しい種類の情報を送る（「情報の収集と利用」の例。例：#362 の共有先のアプリ）
- AI素材のサーバー（`functions/`）で、保存する項目・保存期間（TTL）・ログに書くもの・知らせる先（Discord）を変える。Gemini 以外のモデルに替える、API キーを替える（「AI素材の機能で送信・保存する情報」「データの保存期間と削除」）
- バックアップの決まり（`app/src/main/res/xml/backup_rules.xml`・`data_extraction_rules.xml`）を変える（「端末のバックアップ」「オプトアウト権」）
- Remote Config で配る値の種類を足す（「本アプリが利用する第三者サービス」の Firebase Remote Config の行。`.agents/remote-config.md` の「初めて公開する前に」も同じ確認）

あわせて Play Console のデータセーフティの申告も見直します。申告は Play Console の権限がある人がします。

## 節ごとの根拠（2026-11 時点）

`functions/`・`AuthRepository.kt`・顔検出（#338）はまだ `release/v2.12.0` に入っていないので、この表は `feature/348-ai-material-billing-server`（#391）の c2d987c で確かめました。入ったときに表を見直し（#398）、この段落は消します。

| ページの記述 | 根拠 |
|---|---|
| AI素材で送った文章と画像をサーバーに残さない、ログに書かない | `functions/src/generateMaterial.ts` のログ、`functions/README.md` |
| ログに利用者IDや注文番号が残ることがある、ログは30日で消える | `generateMaterial.ts`・`verifyPurchase.ts`・`voidedPurchases.ts` のログ。30日は Cloud Logging の `_Default` バケットの既定の保存期間（変えたらページも直す） |
| 購入の記録（注文番号・商品・日時・期限、トークンは残さない。注文番号はない場合がある） | `functions/src/verifyPurchase.ts` の「購入の記録」 |
| 返金で回数券を取り消す | `functions/src/voidedPurchases.ts` |
| 報告の内容は180日、1日ごとの報告の数は2日で消える。Discord には理由とモデル名だけ | `functions/src/reportMaterial.ts`（`REPORT_RETENTION_DAYS`・`REPORT_USAGE_RETENTION_DAYS`、TTL、`notifyOwner`） |
| 回数（無料の1回、回数券の残りと期限） | `functions/src/quota.ts` |
| App Check は素材を作る・回数券を買う・報告するとき | `functions/src/index.ts` の `enforceAppCheck: true` |
| 利用者IDは AI素材の機能を初めて使うときに作る。作るとき IP アドレスが Firebase に記録される | `app/src/main/java/com/fansauchiwa/data/repository/AuthRepository.kt`（使うときに `signInAnonymously`）、[Firebase のデータの開示](https://firebase.google.com/docs/android/play-data-disclosure)（2026-10-16 に確認） |
| Gemini は有料の契約で、送った内容は学習に使われない | `functions/src/index.ts` の `GEMINI_API_KEY` のコメント、[Gemini API 追加利用規約](https://ai.google.dev/gemini-api/terms)の有料サービスの項。無料枠のキーにすると、この記述と食い違う |
| 顔の確認と背景の切り抜きは端末内 | 切り抜きは `app/src/main/java/com/fansauchiwa/data/infra/ImageProcessingLocalSource.kt`、顔の確認も同じファイル（#338 で追加）。どちらも `gradle/libs.versions.toml` の ML Kit（Play 開発者サービスの端末内モデル） |
| うちわのデータ・挿入した画像・利用者IDがバックアップに入る | `app/src/main/AndroidManifest.xml` の `allowBackup="true"`、`backup_rules.xml`・`data_extraction_rules.xml` に除外がない |
