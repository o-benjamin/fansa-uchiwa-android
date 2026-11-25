# プッシュ通知（お知らせ）

新しい機能やテンプレートなどのお知らせを、Firebase Cloud Messaging（FCM）で送ります（#429）。送るのは Firebase コンソールの「Messaging」からで、サーバーは置いていません。

## アプリの作り

- 起動のたびに、全員のトピック `all`（`PushMessagingRepository.ANNOUNCEMENT_TOPIC`）を購読します（`FansaUchiwaApplication`）。名前を変えると、変える前の版を入れた人に届かなくなるので変えません
- 通知チャンネルは「お知らせ」（ID `announcement-channel`、`strings.xml` の `announcement_notification_channel_id`）。`AndroidManifest.xml` の `default_notification_channel_id` が同じ ID を指し、FCM が出す通知はこのチャンネルに入ります。ID を変えると、利用者が端末の設定でこのチャンネルを切っていても新しいチャンネルで届いてしまうので変えません
- 通知の許可（Android 13 以上）は、うちわを保存したあとに1回だけ聞きます（`NotificationPermissionRepository`、`UchiwaPreviewViewModel.onSaveSuccessShown`）。許可されなくても、もう聞きません。同じ保存でアプリ内レビュー依頼（#243）は出しません。ほかの通知（#361 のお題の通知など）を足すときも、聞く場面はここ1つにまとめ、別の場面で聞き直しません。イベント予定のリマインドは、リマインドを付けたときにその場で聞きます（こちらとは別）
- アプリの外にいるあいだに届いたお知らせだけ、FCM が通知を出します。アプリを開いているあいだに届いたお知らせは出ません（`FirebaseMessagingService` を置いていないため）。開いているあいだにも出したくなったら、サービスを足して `onMessageReceived` で通知を作ります
- 通知を押すと、アプリはいつもホームで開きます（`MainActivity.handlePushNotificationOpen`）。お題のカードもホームにあるので、お題のお知らせもホームで足ります。別の画面を開きたくなったら、カスタムデータに行き先のキーを足し、ナビゲーションに渡します
- 通知から開いたら、そのあとの最初のインタースティシャルは出しません（`AdMobRepository.skipNextInterstitialAd`）

## 計測

- `open_from_push`：通知を押して開いたとき。`source` にカスタムデータの `campaign` の値を付けます（`PushAnalyticsParams`）。`campaign` を付けずに送ると、パラメータなしで届きます
- コンソールから送ったお知らせは、FCM も `notification_receive`・`notification_open`・`notification_dismiss` を自動で送り、コンソールの「レポート」に送った数・開いた数が出ます。開いた割合はそちらで見て、通知から開いたあとの動き（保存したかなど）は `open_from_push` と並べて GA4 で見ます

## 送り方

1. Firebase コンソールの「Messaging」→「新しいキャンペーン」→「Firebase Notification メッセージ」
2. タイトルと本文を書く。外部への送信なので、文面と送る日時はオーナーの承認をもらってから送ります
3. 「テスト メッセージを送信」で、自分の端末に先に送って見た目を確かめる。端末の登録トークンは、アプリの設定画面のいちばん下の「通知のテスト用 ID」をタップしてコピーできます（アプリを閉じてから送る。開いていると出ません）
4. 「ターゲット」で「トピック」を選び、`all` を入れる。版で絞りたいときは「ユーザー セグメント」でアプリと版を選ぶ（トピックとは一緒に使えません）
5. 「追加オプション」→「カスタムデータ」に、キー `campaign`・値にお知らせごとの名前（例 `2026-12-new-templates`、英数字とハイフン、100文字まで）を入れる
6. 「通知チャンネル」は空のまま（`announcement-channel` に入ります）

送れる相手は、FCM が入った版（v2.13.0 以降）を入れた人だけです。Android 13 以上は、許可した人だけに届きます。

## 持ち主が変わるとき

送るのに要るのは、Firebase プロジェクトの Messaging を使える権限だけです（鍵やサーバーはありません）。プロジェクトを替えると、出回っている版の登録トークンとトピックの購読は前のプロジェクトのものなので、新しいプロジェクトからは届きません。プロジェクトごと移します（`.agents/play-billing.md` の「持ち主が変わるとき」と同じ）。
