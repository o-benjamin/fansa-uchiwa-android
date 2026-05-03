# Fansa Uchiwa (Android) - Copilot Instructions

## 1. テクニカルスタック & バージョン

- **Language**: Kotlin 2.0.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Unidirectional Data Flow (UDF)
- **Dependency Management**: Gradle Version Catalog (libs.versions.toml)

## 2. リソース管理の厳格なルール

- **カラー定義**:
    - 色は `ui/theme/Color.kt` ではなく、必ず `app/src/main/java/com/fansauchiwa/ui/theme/Theme.kt`
      に適宜されたMaterialThemeの色を使用してください。
    - Compose内では `colorResource(id = R.color.name)` を使用します。
- **文字列リソース**:
    - UIテキストは `app/src/main/res/values/strings.xml` を使用し、ハードコーディングを避けてください。
- **サイズ（dp）定義**:
    - **8-Point Grid Systemの適用**: UIのサイズや余白は、原則として**8の倍数 (8, 16, 24, 32...)**
      で定義してください（テキスト周りなど細かい調整が必要な場合のみ4の倍数を許容します）。
    - 既存のコードや提案するコードにおいて、対象となるサイズが8の倍数になっていない場合（例: `10.dp` や
      `15.dp` など）は、Copilot自身で最も近い8の倍数（または4の倍数）に修正した上で適用を行ってください。

## 3. アーキテクチャとデータ設計ガイドライン

- **識別子とリソースの分離**:
    - ドメインモデル(例: `Decoration`)において、「オブジェクトの一意なID(Instance
      ID)」と「参照するリソースのID(Resource ID)」は必ず別のプロパティとして管理してください。
    - 例: 画像オブジェクトは `id: String` (UUID) と `imageId: String` (リソース参照) の両方を持つべきです。
- **UI状態管理 (State Hoisting)**:
    - 画面の状態は `UiState` に集約し、`ViewModel` の `StateFlow` で管理してください。
    - **Sealed Interfaceによる状態定義:**
        - 画面のUI状態（UiState）は必ず `sealed interface` を用いて定義すること。
        - 基本構造として `Loading`, `Success`, `Error` の3つの状態（data class / data object）を持たせること。
    - **Flowを用いたリアクティブな状態管理:**
        - ViewModelでの UiState の初期値（デフォルト値）は原則として `Loading` とすること。
        - `ViewModel` 内の `StateFlow` は `MutableStateFlow` + `asStateFlow()` で公開すること。
        - `init` ブロックでまず `observe~()` を呼んでから `fetch~()` を呼ぶこと（購読を先に開始してから取得を開始する）。
        - データ取得は `fetch~()` メソッド、Flow購読は `observe~()` メソッドに分離すること。
        - `observe~()` では Repository の `get~Stream()` を `.onEach { }.launchIn(viewModelScope)`
          で購読し、UiState を更新すること。
        - `fetch~()` では Repository の `fetch~()` を `viewModelScope.launch` で呼び出し、例外発生時は
          `Error` 状態にすること。
    - `ViewModel` 内では `data class` の `copy()` メソッドを使用して不変性を保ちながら状態を更新してください。
- **Repository層のFlowパターン:**
    - Repository の実装クラスは、内部に `MutableSharedFlow(replay = 1)` を保持し、`asSharedFlow()`
      で外部に公開すること。
    - Flowを返すメソッドは `get~Stream(): Flow<T>` という命名にすること。
    - `fetch~()` メソッドは DataSource の `get~Stream().first()` で現在値を1件取得し、内部の
      `MutableSharedFlow` に `emit` すること。返り値は持たせないこと。
    - これにより、`fetch~()` 呼び出し → DataSource から値取得 → `emit` → `get~Stream()`
      購読側に値が流れる、という一方向のデータフローが実現される。
- **DataSource層のFlowパターン:**
    - DataSource（インターフェース・実装ともに）はFlowを返す `get~Stream(): Flow<T>` メソッドのみを定義すること。
    - fetch/observe の分離はRepository以上の責務であり、DataSource層では行わないこと。
- **状態管理とUIインタラクション**:
  -UIイベントはすべてViewModelのメソッドに委譲し、Compose側でビジネスロジックを持たないでください (
  UDF: Unidirectional Data Flow の徹底)。
- **Repositoryパターン**:
    - `ViewModel` からは、`~Repository` というファイル内の `~Repository` インターフェースを呼び出してください。
    - そのインターフェースは、同一ファイル内の `~RepositoryImpl` クラスが実装してください。
    - インターフェースと実装クラスは、`~Repository.kt` という同一ファイルに記述してください。
    - `~Repository.kt` ファイルは必ず `app/src/main/java/com/fansauchiwa/data/repository`
      配下に作成してください。
- **DataSourceパターン (Infra層)**:
    - `~Repository` の実装クラスは、データの永続化や外部APIアクセスなどのインフラ層処理を
      `~DataSource` インターフェースに委譲してください。
    - `~DataSource` インターフェースは、`~DataSource.kt` というファイルに定義してください。
    - そのインターフェースの実装クラスは、`~LocalSource` や `~RemoteSource`
      など、データソースの種類に応じた名前を付け、別ファイルに記述してください（例:
      `MasterpieceLocalSource.kt`）。
    - `~DataSource` および `~LocalSource`/`~RemoteSource` は必ず
      `app/src/main/java/com/fansauchiwa/data/infra` 配下に作成してください。
    - これにより、データアクセス層の切り替えやテスタビリティが向上します。
- **ScreenとViewModelの役割分担**:
    - "~Screen"というファイルにはロジックを持たせず、UIの宣言だけを行なうようにしてください。
    - "~ViewModel"というファイルはUIの情報を持たず、ロジックだけを持足せるようにしてください。
    - ScreenからNavGraphにコールバックを伝えるときも、一度ViewModelを経由するようにしてください。
- **メソッドの行数**:
    - ひとつのメソッドが60行を超える場合には、メソッドの切り出しができないかどうか検討し、可能な場合は再利用可能な単位で切り出してください。
    - そのメソッドに`@Composable`のアノテーションがついている場合、100行を超える場合にコンポーザブルの切り出しを行うようにしてください。
- **メソッドの共通化**:
    - 複数の場所に同じ処理を記述する必要がある際は、メソッドを共通化し、ボイラープレートコードを削減してください。
- **data class**:
    - data classを新規作成する場合、使用するパッケージ配下に新規ファイルを作成し、必ず別ファイルに切り出してください。

## 4. UI実装ガイドライン

- **共通コンポーザブルの作成基準 (Rule of Two)**:
    - 同じ標準コンポーザブル（Button, IconButton, Slider等）を使用する箇所が**2箇所以上**
      になる場合は、必ずその標準コンポーザブルをラップした独自の共通コンポーザブル（例: `FansaButton`
      ）を `ui/component` パッケージに作成してください。
    - これにより、デザイン、Haptic Feedback、Analytics等の横断的関心を一括管理します。
- **Haptic Feedback (触覚フィードバック) の実装ルール**:
    - 触覚フィードバックはUIの副作用であるため、ViewModelには含めずUI層で完結させてください。
    - 実装は原則として、上記の**共通コンポーネント**（`FansaButton`等）または**共通のカスタムModifier
      **（`hapticClickable`等）を使用してください。
    - 個別の `onClick` 等の中で `LocalHapticFeedback` を直接呼び出すことは、一貫性保持のため避けてください。
    - Compose標準の `LocalHapticFeedback` を直接呼び出すことは**禁止**します。必ず
      `rememberFansaHapticManager()` を使用してマネージャーを取得し、アプリ独自のEnumである
      `FansaHapticType` を引数に渡して実行してください。
- **デフォルト引数**:
    - デフォルト引数は、基本的に設定しないでください。
    - デフォルト引数があることで、呼び出し側でのコード記述量が大きく削減される場合は、特例としてデフォルト引数を設定してもよいこととします。
- **Modifier引数**:
    - 引数にModifierを指定する場合、記述する順番は必ずオプショナル引数の中で1番目にしてください。
    - ComposeのAPIガイドラインに基づく指示。
- **プレビュー**:
    - 作成したコンポーザブルは、基本的にPreviewを作成してください。
    - 状態によって見た目が変わるコンポーザブルは、状態の数だけPreviewを作成してください。
    - Previewは、ファイルの最下部にまとめて配置してください。
    - Previewは `FansauchiwaTheme` で囲み、テーマの影響を受けるコンポーザブル（色、形状など）が正しく表示されるようにしてください。
- **セマンティクスキー (Semantics)**:
    - UIテストで値を検証する必要があるセマンティクスプロパティキーは、必ず `edit/SemanticsKeys.kt`
      に集約してください。
    - `SemanticsPropertyKey` の定義と `SemanticsPropertyReceiver` の拡張プロパティを同一ファイルに記述してください。
    - これにより、セマンティクスキーの管理と再利用性が向上し、テスト実装との連携が容易になります。

## 5. テスト実装ガイドライン

- **作成条件とテストの種類**:
    - **ユニットテスト (Local Unit Tests)**:
      計算やデータ処理などのビジネスロジックを含むメソッドを作成した場合は、必ずローカルユニットテストを作成してください。
    - **UI動作テスト (Instrumented UI Tests)**: ボタン押下や画面遷移など、動作を伴う新規UI機能を追加した場合は、必ず
      `composeTestRule` を使用したインストゥルメンテーションテストを作成して動作を保証してください。
- **テストケース**:
    - 正常系（成功）、異常系（失敗）、および境界値（エッジケース：ゼロ、負の値、空文字など）を全て網羅するように設計してください。
- **命名規則**:
    - `テスト対象_条件_期待される結果` のパターンを使用し、テストの意図を明確にしてください。（例:
      `emailValidator_CorrectEmailSimple_ReturnsTrue`）
    - バッククォート（\`）を使った自然言語の命名は使用しないでください。Kotlinのlintで "Remove
      redundant backticks" の警告が発生するためです。
- **場所**:
    - テストの種類に応じて、以下の適切なディレクトリ配下にScreen・機能ごとのパッケージで作成してください。対応するパッケージが存在しない場合は新規作成してください。
        - **ユニットテスト**: `app/src/test/java/com/fansauchiwa/...`
        - **UI動作テスト**: `app/src/androidTest/java/com/fansauchiwa/...`

## 5. プロンプトへの回答スタイル

- コード提案は最新の Kotlin 記法（Trailing Lambdas等）を遵守してください。
- 実装が複雑になる場合（特に `Decoration` の座標計算など）は、ロジックの正当性を証明するための単体テストコードも併せて提案してください。
- 実装の最後にビルドして確認しようとしないでください。コンパイルエラーがないかどうかのみ確認し、ビルドが必要なコンパイルエラーは無視してください。
- 他ファイルのオブジェクトを使用するとき、com.fansauchiwa~から始まるパスを直接記述しないでください。そのような場合はimportをしてください。