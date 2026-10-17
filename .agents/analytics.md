# 分析（Firebase Analytics / GA4）のコード

分析のためだけのコードは、アプリ本体の設計（設定・DB・画面遷移）に混ぜず、1か所にまとめます（#267）。

## 置き場所

- 分析のためだけのコードは `app/src/main/java/com/fansauchiwa/analytics/` に置いてください。
    - イベント名・パラメータ名・パラメータの値の定義（`AnalyticsEvent.kt` の `AnalyticsActions` など、`*Params`、`AdFormat`）
    - パラメータの計算（`FontSessionAnalyticsBuckets.kt`、`AdPaidEventFactory`）。ViewModel が手元の値（選んだフォント名など）をそのまま `mapOf` に詰めて送るのはかまわない。バケット化・過去の値との比較・画面をまたぐ状態の保持など、値を加工したり分析のために持ったりする処理は `analytics` に置く
    - 分析用データの保持（`FontSessionTracker`）
    - 送信（`AnalyticsRepository` / `AnalyticsDataSource` / `FirebaseAnalyticsRemoteSource`）と DI（`AnalyticsModule`）
- 調査用の画面部品（フェイクドアの入口やダイアログなど）は、表示する機能のパッケージ（例：ホームに出すなら `home/` の下）に置き、計測の定義・計算だけを `analytics` に置いてください（#270 の PR #277 のレビューで決めた形）。
- `.agents/architecture.md` の「Repository は `data/repository`、DataSource は `data/infra`」は、分析については適用しません（このパッケージにまとめる）。
- テストは `app/src/test/java/com/fansauchiwa/analytics/` に置いてください。

## ViewModel は呼ぶだけ

- ViewModel は画面ごとのまま（分析用の ViewModel を別に作ったり、1つにまとめたりしない）。
- ViewModel は `analytics` のクラスを呼ぶだけにして、パラメータの計算や分析用の状態を ViewModel に持たせないでください。
    - 例：フォント計測（#242）は `EditViewModel` が `FontSessionTracker.onFontSwitched()` などを呼び、`UchiwaPreviewViewModel` が `FontSessionTracker.exportParams()` の結果をそのまま送る。

## 分析のために本体を増やさない

分析用の値を設定や DB に保存すると、アプリの機能と関係のないデータが端末に残り続け、計測をやめても消しにくい（既存の端末に値が残る）。計測はアプリが持っているデータとメモリだけで行い、本体の設計を分析に合わせて変えないためです。

- 分析のためだけに、設定（DataStore）・DB（Room のテーブルや列）・ナビゲーション引数・UiState の項目を増やさないでください。
    - 画面をまたいで値を渡す必要があるときは、`analytics` の中の `@Singleton` のクラスにメモリで持たせる（`FontSessionTracker` と同じ形）。プロセスが再生成されて値が失われたときは、そのパラメータを送らない。
    - 過去の値と比べる必要があるときは、アプリがもともと持っているデータ（保存済みのうちわなど）から求める。求められないなら、その計測はやめる案を出す。
- GA4 に送るイベント名・パラメータの値を変えるときは、既存のレポートやカスタムディメンションが壊れないかを PR に書いてください。

## 分析の処理が失敗しても本体の操作は止めない

- 分析のための処理（パラメータの計算・データの読み込み・送信）が例外を出しても、保存や共有などの本体の操作を止めたり、アプリを落としたりしないでください。失敗した部分のパラメータは付けずに送り、例外は `CrashReportingRepository.recordException()` で記録してください（例：`FontSessionTracker.exportParams()`）。

## 新しいイベントが届いたかを公開前に確かめる

イベントやパラメータが届いていないと公開後に分かっても、直して出し直すまでの測定期間は取り戻せません。AI エージェントはアプリを動かさない（`AGENTS.md`）ので、操作はオーナーにクローズドテスト版（Play のクローズドテストで配る release ビルド。Analytics を送る）で頼み、届いたかは PR を作った側が GA4 で確かめます（#295）。

- 新しいイベントやパラメータを足す・変える PR では、画面の動きが変わらなくても「オーナーに動作確認してほしいこと」を書いてください（例：PR #294）。
    - 「操作」には、クローズドテスト版でそのイベントを起こす操作を書く。
    - 「期待する結果」には、画面で分かること（今までどおり保存できる、など）と、届くはずのイベント名・件数・パラメータの値を書き、後者には「PR を作った側が GA4 で確かめる」と添える。オーナーには GA4 を見る作業を頼まない。
- パラメータの値をレポートで見るには、GA4 のカスタムディメンションへの登録が要ります。登録した日より前のデータは見られないので、登録が要るかどうかを PR に書いてください（例：PR #294 の「GA4 への影響」）。
    - GA4 の「管理 > データの表示 > カスタム定義」に同じパラメータ名があれば要らない。イベントのスコープの登録はパラメータ名で効くので、ほかのイベントで登録済みの名前でもよい。
    - 要るなら、クローズドテストでの操作より前に登録する。登録は GA4 の設定の変更で、プロパティの編集者以上の権限が要るので、AI エージェントはせずオーナーに頼む。
- 届いたかは、PR を作った側（AI エージェントを含む）が GA4 で確かめ、結果を PR にコメントしてください（マージ後でもよい）。
    - 見る先：Firebase プロジェクトにリンクした GA4 プロパティ。ID は Firebase コンソールの「プロジェクトの設定 > 統合 > Google Analytics」で確かめる（ID の値はリポジトリに書かない）。GA4 Data API で見るときは、呼ぶアカウントにそのプロパティの閲覧者以上の権限と `analytics.readonly` のスコープが要り、Google Cloud のプロジェクトで Google Analytics Data API を有効にしておく。画面で見るなら GA4 の「レポート > リアルタイム」と「探索」。
    - 版で絞る：既存のイベントを変える PR では、公開中の版から届く同じイベントも数えてしまう。ディメンション `appVersion` を加え、クローズドテスト版の `versionName`（`app/build.gradle.kts`）に絞る。件数は指標 `eventCount` で見る。
    - イベント名と件数：操作から30分以内なら `runRealtimeReport`（ディメンション `eventName`・`appVersion`）で見られる。届くまで時間がかかることがあるので、見えなくてもすぐに届いていないとはせず、`runReport`（ディメンション `date`・`eventName`・`appVersion`）で見直す。
    - パラメータの値：`runReport` のディメンション `customEvent:<パラメータ名>` で見る。リアルタイムでは見られず、集計に最大48時間ほどかかるので、当日に見えなくても届いていない根拠にしない。
    - コメントに書くのは、その版から届いたかと、期待した件数・値と合ったかだけにする（版で絞らない全体の件数は、利用の規模が読めるので書かない）。
- 確かめる時期と、届いていなかったとき
    - イベント名と件数は、そのリリースを製品版の審査に出す前に確かめる。その版から届いていなければ製品版に出さず、Issue を立てて直す。
    - パラメータの値が審査に出す前に見えなければ、出したあとに見る。操作から48時間を過ぎても見えなければ届いていないとして、Issue を立てて直す。
- debug ビルドは Analytics と Crashlytics を送らない（`app/src/debug/AndroidManifest.xml`）。debug で DebugView に何も出なくても故障ではない。手元でアプリを動かして確かめるときは beta ビルドを使い、`adb shell setprop debug.firebase.analytics.app com.fansauchiwa` で端末のデバッグモードを有効にすると DebugView に出る。

## 調査用の計測の消し方を書く

- 調査のために一時的に足す計測（消す前提のもの）は、そのクラスの KDoc に「計測をやめるときに消すもの」を書いてください（例：`FontSessionTracker` の KDoc）。
