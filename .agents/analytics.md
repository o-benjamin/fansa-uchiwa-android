# 分析（Firebase Analytics / GA4）のコード

分析のためだけのコードは、アプリ本体の設計（設定・DB・画面遷移）に混ぜず、1か所にまとめます（#267）。

## 置き場所

- 分析のためだけのコードは `app/src/main/java/com/fansauchiwa/analytics/` に置いてください。
    - イベント名・パラメータ名・パラメータの値の定義（`AnalyticsEvent.kt` の `AnalyticsActions` など、`*Params`、`AdFormat`）
    - パラメータの計算（`FontSessionAnalyticsBuckets.kt`、`AdPaidEventFactory`）
    - 分析用データの保持（`FontSessionTracker`）
    - 送信（`AnalyticsRepository` / `AnalyticsDataSource` / `FirebaseAnalyticsRemoteSource`）と DI（`AnalyticsModule`）
- `.agents/architecture.md` の「Repository は `data/repository`、DataSource は `data/infra`」は、分析については適用しません（このパッケージにまとめる）。
- テストは `app/src/test/java/com/fansauchiwa/analytics/` に置いてください。

## ViewModel は呼ぶだけ

- ViewModel は画面ごとのまま（分析用の ViewModel を別に作ったり、1つにまとめたりしない）。
- ViewModel は `analytics` のクラスを呼ぶだけにして、パラメータの計算や分析用の状態を ViewModel に持たせないでください。
    - 例：フォント計測（#242）は `EditViewModel` が `FontSessionTracker.onFontSwitched()` などを呼び、`UchiwaPreviewViewModel` が `FontSessionTracker.exportParams()` の結果をそのまま送る。

## 分析のために本体を増やさない

- 分析のためだけに、設定（DataStore）・DB（Room のテーブルや列）・ナビゲーション引数・UiState の項目を増やさないでください。
    - 画面をまたいで値を渡す必要があるときは、`analytics` の中の `@Singleton` のクラスにメモリで持たせる（`FontSessionTracker` と同じ形）。プロセスが再生成されて値が失われたときは、そのパラメータを送らない。
    - 過去の値と比べる必要があるときは、アプリがもともと持っているデータ（保存済みのうちわなど）から求める。求められないなら、その計測はやめる案を出す。
- GA4 に送るイベント名・パラメータの値を変えるときは、既存のレポートやカスタムディメンションが壊れないかを PR に書いてください。

## 調査用の計測の消し方を書く

- 調査のために一時的に足す計測（消す前提のもの）は、そのクラスの KDoc に「計測をやめるときに消すもの」を書いてください（例：`FontSessionTracker` の KDoc）。
