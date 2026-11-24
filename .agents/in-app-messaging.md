# アプリ内メッセージ（Firebase In-App Messaging）

更新後のお知らせや新機能の案内は、自前のダイアログにせず Firebase In-App Messaging（FIAM）で出します（#428 の「ねらい」）。出したあとに文面を直したり、新機能の案内を版をまたいで続けたりするのに、アプリを出し直さずに済むためです。文面・画像・ボタン・出す相手（版・オーディエンス）・期間は Firebase コンソールの「Messaging」のキャンペーンで決め、アプリのコードには持ちません。

## アプリの中の作り

- 表示とボタンの URL を開く動きは FIAM の既定のまま（`firebase-inappmessaging-display`）。アプリは `inappmessage/InAppMessageHost.kt`（`FansaUchiwaNavGraph` に1つだけ置く）と、Activity に1つの `InAppMessageViewModel` で、そのあとの始末だけをする。お題・AI素材の行き先は、ホームが `pendingTarget` を受け取って開く
- **ホームが今の画面のときだけ出す**。ナビゲーションの今の画面がホームでなくなったら `setMessagesSuppressed(true)`、ホームに戻ったら `false` にする。編集画面・プレビュー・設定などでは出ない。止めている間に来たきっかけ（起動・前に戻るなど）のメッセージは、次のきっかけまで出ない
    - アプリを裏に回しただけでは止めない。FIAM は前に戻ったときに出していたメッセージを描き直すが、止めていると描かずにそのメッセージを持ったままになり、以後のメッセージも出なくなる（22.0.2 の `showActiveFiam`）
    - 例外：裏に回している間にプロセスが終わり、編集画面から作り直されたときは、最初の描画で止めるまでの間にメッセージの準備が終わると、編集画面に出ることがある
- **表示とボタンのリスナーは Activity が前にいる間だけ付ける**。FIAM は Activity が一時停止するたびにアプリのリスナーをすべて外す（22.0.2 の `onActivityPaused` → `removeAllListeners`）ので、付けたままにすると、一度裏に回したあとはボタンの行き先が効かない。**FIAM を更新したら**、この動きが変わっていないかを見る
- 形式はモーダル・カード・画像だけにし、**バナーは使わない**。バナーは下の画面を触れるので、出したまま編集画面に移れ、そこで裏に回すと、この節の「アプリを裏に回しただけでは止めない」と同じく、メッセージが描き直されないまま残る
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
- 形式はモーダル・カード・画像のどれか。**バナーは使わない**（上の「アプリの中の作り」）
- `on_foreground` のメッセージは、キャンペーンをまたいで端末ごとに1日1回までしか出ない（22.0.2 の `RateLimitModule`。テスト送信は対象外）。同じ日に `on_foreground` のキャンペーンを2つ動かすと片方が出ない
- **キャンペーンの公開はオーナーの承認**（外への発信。文面の案は AI エージェントが作ってよい）
- 公開前の確かめ：コンソールの「デバイスでテスト」に、設定画面のいちばん下のインストール ID（#411）を入れる。テスト送信は次にアプリを前に出したとき（ホームで）に出る。Analytics を送るのはクローズドテスト版（release）と beta で、debug は送らない
    - **テスト送信では、FIAM の標準のイベントもコンソールの結果も記録されない**（22.0.2 の `MetricsLoggerClient` はテストのキャンペーンでは Analytics への記録を飛ばし、アプリのリスナーだけを呼ぶ）。`tap_in_app_message` は届く。標準のイベントは最初のキャンペーンを公開してから確かめる（#449）
    - 確かめ終わったら、下書きのキャンペーンを消す
- 出ないとき：ログのタグ `FIAM.Headless` を見る。起動時に「Starting InAppMessaging runtime with Installation ID」と出るので、テストに入れた ID と合っているかを確かめる
- 計測：表示・ボタン・閉じたの数とキャンペーン名は、FIAM が Analytics に送る標準のイベント（`firebase_in_app_message_impression`・`firebase_in_app_message_action`・`firebase_in_app_message_dismiss`、`message_name`）で見る。どの行き先を押したかはアプリが送る `tap_in_app_message`（`target`）で見る（`analytics/InAppMessageAnalyticsParams.kt`）

## データの扱い

- FIAM が扱うのは、表示・タップ・閉じたの記録と Firebase のインストール ID（[Firebase のデータの開示](https://firebase.google.com/docs/android/play-data-disclosure)、2026-11-24 に確認）。送るときは HTTPS で暗号化される。プライバシーポリシーの「本アプリが利用する第三者サービス」に載せている（`.agents/privacy-policy.md`）
- 持ち主が変わるとき：キャンペーンは Firebase プロジェクトにあり、プロジェクトごと移せばそのまま渡る。別のプロジェクトに移すなら、動いているキャンペーンを作り直す（アプリ側は `google-services.json` の差し替えだけ）
- FIAM をやめるときは、コンソールのキャンペーンを止め、`git grep -n -i -e 'InAppMessag' -e 'in_app_message' -- app gradle` に当たるものを消し（`inappmessage/`・`InAppMessaging*`・`InAppMessageTarget`・`InAppMessageAnalyticsParams` はファイルごと、`HomeScreen`・`HomeViewModel`・`FansaUchiwaNavGraph`・`DailyPromptAnalyticsParams`・`AnalyticsEvent`・DI の2つは当たった箇所だけ）、プライバシーポリシーから外す。`firebase-inappmessaging-display` を外すと、一緒に入っていた Glide・gRPC・RxJava 2 なども消える（アプリの画像の読み込みは Coil で、Glide は FIAM のためだけに入っている）
