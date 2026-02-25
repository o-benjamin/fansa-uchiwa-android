# Fansa Uchiwa (Android) - Copilot Instructions

## 1. テクニカルスタック & バージョン

- **Language**: Kotlin 2.0.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Unidirectional Data Flow (UDF)
- **Dependency Management**: Gradle Version Catalog (libs.versions.toml)

## 2. リソース管理の厳格なルール

- **カラー定義**:
    - 色は `ui/theme/Color.kt` ではなく、必ず `app/src/main/res/values/colors.xml` を使用してください。
    - Compose内では `colorResource(id = R.color.name)` を使用します。
- **文字列リソース**:
    - UIテキストは `app/src/main/res/values/strings.xml` を使用し、ハードコーディングを避けてください。

## 3. アーキテクチャとデータ設計ガイドライン

- **識別子とリソースの分離**:
    - ドメインモデル(例: `Decoration`)において、「オブジェクトの一意なID(Instance
      ID)」と「参照するリソースのID(Resource ID)」は必ず別のプロパティとして管理してください。
    - 例: 画像オブジェクトは `id: String` (UUID) と `imageId: String` (リソース参照) の両方を持つべきです。
- **UI状態管理 (State Hoisting)**:
    - 画面の状態は `UiState` データクラスに集約し、`ViewModel` の `StateFlow` で管理してください。
    - `ViewModel` 内では `data class` の `copy()` メソッドを使用して不変性を保ちながら状態を更新してください。
- **副作用の排除**:
    - Composable関数内でビジネスロジックを実行せず、ラムダ式を通じて `ViewModel` の関数を呼び出してください。
- **Repositoryパターン**:
    - `ViewModel` からは、`~Repository` というファイル内の `~Repository` インターフェースを呼び出してください。
    - そのインターフェースは、同一ファイル内の `~RepositoryImpl` クラスが実装してください。
    - インターフェースと実装クラスは、`~Repository.kt` という同一ファイルに記述してください。
- **DataSourceパターン (Infra層)**:
    - `~Repository` の実装クラスは、データの永続化や外部APIアクセスなどのインフラ層処理を
      `~DataSource` インターフェースに委譲してください。
    - `~DataSource` インターフェースは、`~DataSource.kt` というファイルに定義してください。
    - そのインターフェースの実装クラスは、`~LocalSource` や `~RemoteSource`
      など、データソースの種類に応じた名前を付け、別ファイルに記述してください（例:
      `MasterpieceLocalSource.kt`）。
    - これにより、データアクセス層の切り替えやテスタビリティが向上します。

## 4. UI実装ガイドライン

- **パフォーマンス**:
    - `EditScreen` のような編集画面では、再描画範囲を最小限にするため `derivedStateOf` や `remember`
      を積極的に活用してください。
    - 重い計算処理は `LaunchedEffect` または `ViewModel` (`viewModelScope`) に委譲してください。
- **デフォルト引数**:
    - デフォルト引数は、基本的に設定しないでください。
    - デフォルト引数があることで、呼び出し側でのコード記述量が大きく削減される場合は、特例としてデフォルト引数を設定してもよいこととします。
- **Modifier引数**:
    - 引数にModifierを指定する場合、記述する順番は必ずオプショナル引数の中で1番目にしてください。
    - ComposeのAPIガイドラインに基づく指示。
- **プレビュー**:
    - 作成したコンポーザブルは、基本的にPreviewを作成してください。
    - 状態によって見た目が変わるコンポーザブルは、状態の数だけPreviewを作成してください。

## 5. プロンプトへの回答スタイル

- コード提案は最新の Kotlin 記法（Trailing Lambdas等）を遵守してください。
- 実装が複雑になる場合（特に `Decoration` の座標計算など）は、ロジックの正当性を証明するための単体テストコードも併せて提案してください。
- 実装の最後にビルドして確認しようとしないでください。コンパイルエラーがないかどうかのみ確認し、ビルドが必要なコンパイルエラーは無視してください。
- 他ファイルのオブジェクトを使用するとき、com.fansauchiwa~から始まるパスを直接記述しないでください。そのような場合はimportをしてください。