# UI実装（Compose コンポーザブル・Haptic・Preview・Semantics）

- **共通コンポーザブルの作成基準 (Rule of Two)**:
    - 同じ標準コンポーザブル（Button, IconButton, Slider等）を使用する箇所が**2箇所以上**
      になる場合は、必ずその標準コンポーザブルをラップした独自の共通コンポーザブル（例: `FansaButton`
      ）を `ui/composable` パッケージに作成してください。
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
