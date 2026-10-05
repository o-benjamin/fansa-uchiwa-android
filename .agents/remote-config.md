# Remote Config（アプリを出し直さずに変える値）

Firebase Remote Config の値を読む処理は `data/infra/FirebaseRemoteConfigRemoteSource.kt` に集める（`RemoteConfigDataSource` 経由。#311）。
新しいパラメータを足すときは、この表に1行足し、アプリ側の初期値は「何も出さない・何も変えない」側にする（コンソールに値がなくても、いままでと同じ動きにするため）。

| パラメータ | 型 | 使い道 | 初期値 |
|---|---|---|---|
| `affiliate_materials` | String（JSON） | 保存完了のダイアログに出す、うちわの材料の Amazon アソシエイトのリンク | 空文字（リンクの欄を出さない） |

取得は、うちわのプレビュー画面を開いたとき。最小取得間隔は1時間なので、**コンソールで公開してから端末に届くまで最大約1時間**かかる。取得に失敗したら前回の値を使う（なければ空）。

## `affiliate_materials` の書き方

```json
{
  "items": [
    { "id": "jumbo_uchiwa", "label": "ジャンボうちわ", "url": "https://www.amazon.co.jp/dp/XXXXXXXXXX?tag=xxxx-22" },
    { "id": "mirror_sheet", "label": "ミラーシート", "url": "https://amzn.to/xxxxxxx" }
  ]
}
```

- `id`：計測に使う英小文字の名前。GA4 の `tap_preview_affiliate` の `affiliate_item` に出る（レポートで見るには、GA4 でカスタムディメンション `affiliate_item` の登録が要る）
- `label`：画面に出す材料の名前（ボタンは「〇〇をAmazonで見る」になる）
- `url`：開く先。**`https` で、ホストが `amazon.co.jp` / `www.amazon.co.jp` / `amzn.to` / `amzn.asia` のものだけ**。設定の誤りで任意の URL を開かないための制限（`AffiliateLinksParser`）
- 出すのは先頭から**3件まで**。`id` が重複した項目は先の1件だけ残る。`id`・`label`・許可した `url` のどれかが欠けた項目は捨てられる
- 商品の画像と価格は出さない（Amazon が提供する仕組みを通さないと使えない。アソシエイトの規約）
- 「PR」の表示と「Amazonのアソシエイトとして、…」の文は、リンクと必ず一緒に出す（ステマ規制とアソシエイトの規約）。文は `strings.xml` にあり、Remote Config では変えられない

## 止める・確かめる・うまく出ないとき

- **止める**：値を空文字にして公開する（アプリの出し直しは要らない。反映まで最大約1時間）
- **確かめる**：beta ビルドを入れてうちわを保存する。DebugView に `view_preview_affiliate`（保存完了のダイアログにリンクを出したとき）と `tap_preview_affiliate` が出る
- **リンクが出ない**
    - 値が `{"items": [...]}` の形でない、または項目が1件もあるのに1件も使えない → Crashlytics の非致命の例外に `affiliate_materials` で始まるメッセージが出る（1プロセスに1回）
    - 取得そのものの失敗（オフライン・回数制限・プロジェクトの設定の誤り）は想定内として握る。ログのタグ `RemoteConfig` で見る
    - 公開から1時間以内なら、まだ届いていない可能性がある
- 売却のときは、Firebase プロジェクトを移管すれば Remote Config の値もそのまま渡る。Amazon アソシエイトのアカウントは別で、移管できないので買い手が自分のアカウントのリンクに差し替える（`tag=` の入った URL を作り直す）
