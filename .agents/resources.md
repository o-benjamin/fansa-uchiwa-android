# リソース管理（色・文字列・dp）

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
      `15.dp` など）は、エージェント自身で最も近い8の倍数（または4の倍数）に修正した上で適用を行ってください。
