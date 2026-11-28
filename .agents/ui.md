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
- **振動の決まり（どの瞬間にどの種類を鳴らすか。#454）**:
    - 新しい画面・ボタンもこの表に合わせてください。表にない瞬間に鳴らすときは、この表に行を足してください。
    - 成功の合図（`CONFIRM`）は「本当に成功した瞬間」に、失敗の合図（`REJECT`）は「失敗した瞬間」に取っておきます。画面を進めるだけのボタンでは鳴らしません。
    - Android 10（API 29）では、`CONFIRM` は `VIRTUAL_KEY` に、`REJECT` は `LONG_PRESS` に置き換わります（androidx.core の `HapticFeedbackConstantsCompat`）。Android 10 で「保存できたときと完成ボタンの振動が同じ」なのは不具合ではありません。

      | 瞬間 | 種類 | 今の場所 |
      |---|---|---|
      | 保存できた（保存完了のダイアログが出た） | `CONFIRM` を1回 | `preview/SaveResultHapticEffect.kt` |
      | 保存に失敗した（Snackbar が出た） | `REJECT` を1回 | `preview/SaveResultHapticEffect.kt` |
      | 画面を進めるだけのボタン（編集の「完成」）・軽いボタン（元に戻す・やり直す） | `VIRTUAL_KEY` | `edit/EditScreen.kt`・`edit/imagepreview/ImagePreviewScreen.kt` |
      | ドラッグで装飾の順番が1つ入れ替わった | `VIRTUAL_KEY` | `edit/EditScreen.kt`（`onMoveDecoration`） |
      | スナップ（回転・中央のガイド線）・一覧の選択が変わった | `SEGMENT_TICK` | `edit/EditScreen.kt`・`timeline/EventTimelineScreen.kt` |
      | スライダーを動かしている間 | `SEGMENT_FREQUENT_TICK` | `edit/EditCommonComponents.kt` |
      | 長押し | `LONG_PRESS` | `ui/modifier/FansaCombinedClickable.kt` |

    - 決まりの外に残っている使い方（#454 では変えていない。直すかどうかは #456）。新しいボタンではまねせず、上の表に合わせてください。
        - AI素材の画面の「生成する」「購入する」「決定」：押した瞬間に `CONFIRM`（`edit/aimaterial/AiMaterialScreen.kt`）
        - ホームの「削除」：押した瞬間に `CONFIRM`（`home/HomeScreen.kt`。押すとすぐ消える）
- **振動を鳴らさない場面**:
    - 設定の「触覚フィードバック」をオフにした人には鳴らしません。`rememberFansaHapticManager()` を通せば守られます。
    - 同じ結果で続けて鳴らしません。処理の結果（成功・失敗）で鳴らすときは、鳴らしたことを `rememberSaveable` に残し、ダークモードの切り替えなどで画面が作り直されても鳴らし直さないでください（例：`preview/SaveResultHapticEffect.kt`）。
    - 全画面の広告（リワード・インタースティシャル）が前面にある間は鳴らしません。押すと広告やリワードの同意ダイアログが出るボタン（プレビューの「画像として保存」・共有）、同意ダイアログのボタン、バナー広告には振動をつけません（誤タップを誘わないため）。
- **リワード広告の表示中に終わる処理の結果**:
    - 報酬獲得時の保存のように広告の表示中に終わる処理の結果のうち、時間で消えるもの（Snackbar）と1回きりのもの（振動・通知の許可・アプリ内レビュー依頼）は、画面が前面（`RESUMED`）に戻ってから出してください。AdMob の広告の画面は透過なので、下の画面は止まらずに結果を受け取り、広告の下で出したものは見えないまま終わります（例：`preview/UchiwaPreviewScreen.kt`）。閉じるまで残るダイアログ（保存完了のダイアログ）は待たなくてかまいません。
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
