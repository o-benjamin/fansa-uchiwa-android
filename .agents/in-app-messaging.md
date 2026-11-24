# アプリ内メッセージ（Firebase In-App Messaging）

更新後のお知らせや新機能の案内は、自前のダイアログにせず Firebase In-App Messaging（FIAM）で出します（#428。決定は会社のリポジトリの 2026-11-21 の決定）。文面・画像・ボタン・出す相手（版・オーディエンス）・期間は Firebase コンソールの「Messaging」のキャンペーンで決め、アプリのコードには持ちません。

## アプリの中の作り

- 表示とボタンの URL を開く動きは FIAM の既定のまま（`firebase-inappmessaging-display`）。アプリは `inappmessage/InAppMessageHost.kt`（ホームにだけ置く）と `InAppMessageViewModel` で、そのあとの始末だけをする
- **ホームが前にいる間だけ出す**。ホームの ON_PAUSE で `setMessagesSuppressed(true)`、ON_RESUME で `false` にする。編集画面・プレビュー・設定などでは出ない。止めている間に来たきっかけ（起動・前に戻るなど）のメッセージは、次のきっかけまで出ない
    - 例外：裏に回している間にプロセスが終わり、編集画面から作り直されたときは、ホームが一度も前に出ていないので止められず、その起動のメッセージが編集画面に出ることがある
- メッセージが表示されたら、次のインタースティシャルを1回出さない（`AdMobRepository.skipNextInterstitialAd()`。更新の案内と同じ）
- 更新の案内のダイアログ（`update/UpdatePrompt.kt`）とは重ならないように作っていない。お知らせは新しい版、更新の案内は古い版に出すので、キャンペーンの相手を新しい版に絞っていれば重ならない

## ボタンの行き先（キャンペーンのボタンの URL）

| 行き先 | URL | 開く人 | 計測の `target` |
|---|---|---|---|
| Play ストアのアプリのページ | `fansauchiwa://store` | アプリ（`AppUpdateRepository.openStorePage()`。Play アプリで開き、なければブラウザ） | `store` |
| Web ページ（お知らせのページなど） | `https://…` | FIAM（Custom Tabs。使えなければブラウザ） | `web` |
| ホーム（閉じるだけ） | `fansauchiwa://home` | — | `home` |
| 今日のお題で作る | `fansauchiwa://daily_prompt` | アプリ（お題のカードの「このお題で作る」と同じ。カードが出ていないときはホームのまま） | `daily_prompt` |
| AI素材を作る | `fansauchiwa://ai_material` | アプリ（お題のカードの「AIで素材を作る」と同じ。カードに入口が出ていないときはホームのまま） | `ai_material` |

- Play ストアに `https://play.google.com/…` を書かない。FIAM が Custom Tabs で Web のストアのページを開き、Play アプリに移らない（計測も `web` になる）。`market://` も書かない（`other` になる）
- 上の表にない `fansauchiwa://…` は、何も開かずにメッセージが閉じる（`other`）。行き先を足したら、その行き先を知らない古い版に出さないよう、キャンペーンの相手を足した版以降に絞る
- `fansauchiwa://` は、ほかのアプリから画面を開かれないよう、**どの Activity にも intent-filter を置かない**。FIAM は開けるアプリのない URL をログ（`Device cannot resolve intent`）に残すだけで開かず、そのあとアプリのクリックのリスナーが開く。この動きは `firebase-inappmessaging-display` 22.0.2 の `launchUriIntent` で確かめた。**FIAM を更新したら**、開けるアプリのない URL で落ちないか（`startActivity` の前に `resolveActivity` で確かめているか）を見る。落ちる形に変わっていたら、`fansauchiwa://` に intent-filter を置く形に作り直す

## キャンペーンの作り方

- 「更新して最初の起動で1回」：トリガーを `on_foreground`、対象のアプリのバージョンを新しい版に絞り、端末ごとの表示の上限を1回にする
- **キャンペーンの公開はオーナーの承認**（外への発信。文面の案は AI エージェントが作ってよい）
- 公開前の確かめ：コンソールの「デバイスでテスト」に、設定画面のいちばん下のインストール ID（#411）を入れる。テスト送信は次にアプリを前に出したとき（ホームで）に出る
- 計測：表示・ボタン・閉じたの数とキャンペーン名は、FIAM が Analytics に送る標準のイベント（`firebase_in_app_message_impression`・`firebase_in_app_message_action`・`firebase_in_app_message_dismiss`、`message_name`）で見る。どの行き先を押したかはアプリが送る `tap_in_app_message`（`target`）で見る（`analytics/InAppMessageAnalyticsParams.kt`）

## データの扱い

- FIAM が扱うのは、表示・タップ・閉じたの記録と Firebase のインストール ID（[Firebase のデータの開示](https://firebase.google.com/docs/android/play-data-disclosure)、2026-11-24 に確認）。送るときは HTTPS で暗号化される。プライバシーポリシーの「本アプリが利用する第三者サービス」に載せている（`.agents/privacy-policy.md`）
- FIAM をやめるときは、コンソールのキャンペーンを止め、`firebase-inappmessaging-display` と `inappmessage/`・`InAppMessaging*`・`InAppMessageTarget`・`tap_in_app_message` を消し、プライバシーポリシーから外す
