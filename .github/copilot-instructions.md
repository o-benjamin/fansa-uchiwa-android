# Fansa Uchiwa (Android) - Copilot Instructions

## 1. テクニカルスタック & バージョン
- **Language**: Kotlin 2.0.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Target SDK**: 36 / **Min SDK**: 28
- **Dependency Management**: Gradle Version Catalog (libs.versions.toml)

## 2. リソース管理の厳格なルール (重要)
- **カラー定義の参照先**:
    - **色は `ui/theme/Color.kt` ではなく、必ず `app/src/main/res/values/colors.xml` に定義されているリソースを使用してください。**
    - Compose内で色を指定する場合は、`colorResource(id = R.color.your_color_name)` を使用して取得してください。
    - 新しい色が必要な場合は、まず `colors.xml` への追加を提案してください。
- **文字列リソース**:
    - 全てのUIテキストは `app/src/main/res/values/strings.xml` を使用してください。
    - Compose内では `stringResource(id = R.string.name)` を使用してください。

## 3. UI実装ガイドライン
- **Composable関数**:
    - 全てのUIコンポーネントは Material 3 をベースにします。
    - `Modifier` は関数の第一引数（デフォルト値 `Modifier`）として受け取る設計にしてください。
- **プレビュー**:
    - 新たなComposableが作成する際は、プレビューが可能なステートレスなComposableを作成してください。
- **パフォーマンス**:
    - 編集画面（Edit Screen）などのリアルタイム描画が発生する箇所では、`derivedStateOf` を活用し、不要な再コンポーズを抑制してください。

## 4. プロジェクト固有のコンテキスト
- このアプリは「ファンサうちわ」を制作・編集・表示するためのアプリです。

## 5. プロンプトへの回答スタイル
- コード提案は最新の Kotlin 記法（Trailing Lambdas等）を遵守してください。
- カラーリソースを提案する際は、`R.color.xxx` が存在するか、あるいは追加すべきかを明確にしてください。
- 実装が複雑になる場合は、`ExampleUnitTest.kt` に準じた単体テストコードも併せて提案してください。
- **ビルド・検証の省略**: 提案の最後に「実際にビルドして動作を確認します」と宣言しないでください。また、エラーが解決されたと判断できる場合は、実際にビルドコマンドを実行する必要はありません。