# Fansa Uchiwa (Android) - AI Agent Instructions

Jetpack Compose (Material 3) + MVVM / UDF の Android アプリ。Kotlin・ライブラリのバージョンは `gradle/libs.versions.toml` を参照。

## 常に守るルール

- ローカルでフルビルドせず、push して Android CI（ユニットテストと Lint）の結果で確かめてください。Draft PR を早めに作り、CI が緑になるまでレビューを依頼しないでください。手順は `.agents/ci.md`。
- コードは最新の Kotlin 記法（Trailing Lambdas 等）で書いてください。
- 他ファイルのオブジェクトを `com.fansauchiwa~` から始まる完全修飾名で書かず、import してください。
- class / interface / object / enum class / data class は原則1ファイルに1つだけ定義してください。

## 作業内容に応じて参照するドキュメント

以下は必要なときだけ読んでください（該当する作業を始める前に必ず読むこと）。

| 作業内容 | ドキュメント |
|---|---|
| 色・文字列・dp サイズを扱う | `.agents/resources.md` |
| ViewModel / UiState / Repository / DataSource を作成・変更する | `.agents/architecture.md` |
| Kotlin コードを新規作成・変更する（メソッド分割・共通化・ファイル構成） | `.agents/coding-style.md` |
| コンポーザブル・Preview・Haptic・Semantics を作成・変更する | `.agents/ui.md` |
| テストを作成・変更する、ロジックを追加する | `.agents/testing.md` |
| リリースビルド設定・ProGuard ルール・`@Keep` を扱う | `.agents/r8.md` |
| コミットメッセージを作成する | `.agents/commit-message.md` |
| push・PR を作る、CI が失敗した | `.agents/ci.md` |

## ルールの追加方法

- コードに関する方針が新たに決まったら、該当する `.agents/*.md` に追記してください（該当がなければ新規作成し、上の表に登録する）。
- このファイルと `CLAUDE.md` は全セッションで読み込まれるため、ほぼすべての作業に関係する最小限の内容だけを記載してください。
- `.agents/*.md` を `@` で import しないでください（常時読み込まれてしまうため）。
