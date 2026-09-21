# アーキテクチャとデータ設計（UiState / ViewModel / Repository / DataSource）

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
- **非致命の例外の記録**: Crashlytics へ `recordException` を送るときは、`AnalyticsRepository` と同様に `CrashReportingRepository`（`CrashReportingDataSource` / `FirebaseCrashlyticsRemoteSource`）を経由してください（`Firebase.crashlytics` を直接呼ばない）。
