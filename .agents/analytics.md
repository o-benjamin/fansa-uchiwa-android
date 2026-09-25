# 分析（Firebase Analytics / GA4）のコード

分析のためだけのコードは、アプリ本体の設計（設定・DB・画面遷移）に混ぜず、1か所にまとめます（#267）。

## 置き場所

- 分析のためだけのコードは `app/src/main/java/com/fansauchiwa/analytics/` に置いてください。
    - イベント名・パラメータ名・パラメータの値の定義（`AnalyticsEvent.kt` の `AnalyticsActions` など、`*Params`、`AdFormat`）
    - パラメータの計算（`FontSessionAnalyticsBuckets.kt`、`AdPaidEventFactory`）。ViewModel が手元の値（選んだフォント名など）をそのまま `mapOf` に詰めて送るのはかまわない。バケット化・過去の値との比較・画面をまたぐ状態の保持など、値を加工したり分析のために持ったりする処理は `analytics` に置く
    - 分析用データの保持（`FontSessionTracker`）
    - 送信（`AnalyticsRepository` / `AnalyticsDataSource` / `FirebaseAnalyticsRemoteSource`）と DI（`AnalyticsModule`）
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

## 調査用の計測の消し方を書く

- 調査のために一時的に足す計測（消す前提のもの）は、そのクラスの KDoc に「計測をやめるときに消すもの」を書いてください（例：`FontSessionTracker` の KDoc）。
