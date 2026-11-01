# テンプレートの候補（開発版で調整してから取り込む）

新しいテンプレートは、候補を開発版のホームに出し、実機で微調整して保存した結果だけを `TemplateList.kt` に入れる。

## 置き場所

- 候補の一覧：`app/src/debug/java/com/fansauchiwa/data/TemplateCandidateList.kt`
- 候補をアプリに渡す：`app/src/debug/java/com/fansauchiwa/di/TemplateCandidateModule.kt`
- `release` と `beta` には渡す側がないので、`Optional<TemplateCandidates>` が空になり、候補は入らない。release に候補を入れないため、`app/src/main` から `templateCandidateList` を参照しない
- ホームでは既存のテンプレートの後ろに並ぶ（`DefaultTemplateRepository`）

## 候補の形

- テンプレートの id は `candidate_N`、飾りの id は `candidate_N_M`。保存データから候補を見分けるのにこの形を使うので変えない
- `isNameInputPlaceholderEnabled = false` だけ。名前入りは入力で文字が置き換わり、保存データからプレースホルダーに戻せない

## 保存データの読み方

- 保存先は Room の `uchiwaData.db` の `fansa_uchiwa_data`。WAL なので `-wal` も一緒に取る。本体だけだと最近の保存が抜ける
- 飾りは JSON で、既定値の項目は保存されない（`Decoration.kt` の既定値で埋めて読む）。色は `Color.value` を符号付きの Long にした値で、sRGB の色は上位32ビットが ARGB
- 取り込むときは `TemplateList.kt` の末尾に足し、飾りの id は UUID に、テンプレートの id は `template_N` の連番にする
- 位置は端末の px のまま保存され、文字の大きさは密度で変わる。基準の密度は 2.625（Pixel_10）。違う端末で調整したら、ほかの機種でずれないかを確かめる

候補の書き出しと保存データの変換は、会社の作業リポジトリの `scripts/materials/template_lab.py` でやっている（`candidates` と `pull`）。手元にないときは、上の形のとおりに手で書いてよい。
