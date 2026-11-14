# テンプレートの候補（debug ビルドで調整してから取り込む）

新しいテンプレートは、候補を debug ビルドのホームに出し、実機で微調整して保存した結果だけを `TemplateList.kt` に入れる。画面の外で作った見本では中心の小さなずれを直せないので、最後の見た目は実機で決める。

`template_9`〜`template_11` だけは例外で、オーナーが実機で見て調整はいらないと判断したので、保存データを通さず候補の値のまま入れた（#379）。次からは保存した結果だけを入れる。

- 名前だけのテンプレートは種類が足りているので作らず、行動を促す決まり文句を足す
- 段数・文字の大小・並びなどの型は参考にしてよいが、特定の作品や他社アプリのテンプレートをそのまま写さない。実在の名前や写真を入れない

## 取り込み済みの回

- 1回目：`template_9`〜`template_11`（調整なしで入れた。#379）
- 2回目：`template_12`〜`template_30`。60個の候補を debug ビルドで調整・保存した19個だけを取り込み、保存しなかった41個は捨てた（捨てた候補は復元しない。定義は git 履歴の d4df5f4）
- 候補が空のときは、次の回を手順 1 から始める。`candidate_N` は回ごとに使い回すので、コメントの `candidate_N` はその回のものとして読む

## 置き場所

- 候補の一覧：`app/src/debug/java/com/fansauchiwa/data/TemplateCandidateList.kt`
- 候補をアプリに渡す：`app/src/debug/java/com/fansauchiwa/di/TemplateCandidateModule.kt`
- `release` と `beta` には渡す側がないので、`Optional<TemplateCandidates>` が空になり、候補は入らない。`app/src/main` から `templateCandidateList` を参照しない
- ホームでは既存のテンプレートの後ろに並ぶ（`DefaultTemplateRepository`）

## 手順

1. 候補を `TemplateCandidateList.kt` に足す
2. 実機の Play 版を消して debug ビルドを入れる。同じアプリ ID（`com.fansauchiwa`）で署名が違うので、消さないと入らない。その端末のうちわは消える。debug はテスト広告で、計測も送らない
3. 候補を開いて調整し「完成」で保存する。保存しなかった候補は取り込まない
4. 保存データを取り出して `Template(...)` に直し、`TemplateList.kt` の末尾に足す（「保存データの取り出し」の節）
5. 取り込んだ候補とやめた候補を `TemplateCandidateList.kt` から消す
6. 実機の debug ビルドを消して Play 版に戻す。消すと保存も消えるので、必ず 4 のあとにする。`candidate_N` の番号は次の回に使い回すので、前回の保存を残したまま次の回を始めない

## 候補の形

- テンプレートの id は `candidate_N`、飾りの id は `candidate_N_M`。保存データでどの候補から作ったかを見分けるのに使うので変えない
- `isNameInputPlaceholderEnabled = false` だけ。名前入りは入力で文字が置き換わり、保存データからプレースホルダーに戻せない
- 文字の色と全体の縁はホームで選ぶメインカラーで上書きされる（`applyTemplateMainColor`）。調整しても残らないので、組み方・フォント・ステッカーで差をつける
- ぷくぷく（`isPuffyEnabled`・`isPukupuku`・`isOverallBorderPuffyEnabled`）は Android 13 未満では出ない。今のテンプレートには使っていない。使うなら API 29 でも見た目が成り立つかを確かめる

## 保存データの取り出し

```bash
adb shell am force-stop com.fansauchiwa
adb exec-out run-as com.fansauchiwa cat databases/uchiwaData.db > uchiwaData.db
adb exec-out run-as com.fansauchiwa cat databases/uchiwaData.db-wal > uchiwaData.db-wal
adb exec-out run-as com.fansauchiwa cat databases/uchiwaData.db-shm > uchiwaData.db-shm
sqlite3 uchiwaData.db "SELECT decorations, uchiwaColorValue, backgroundColorValue, overallBorderColorValue, overallBorderWidth, isOverallBorderPuffyEnabled FROM fansa_uchiwa_data ORDER BY rowid"
adb shell wm density
```

- Room は WAL なので `-wal` も一緒に取る。本体だけだと最近の保存が抜ける
- 対象は、飾りの id が `candidate_N_M` の行だけ。写真（`Decoration.Image`）が入った行は入れない。同じ候補が2行あれば、要るほうを選ぶ
- 飾りは JSON で、既定値の項目は保存されない。`Decoration.kt` の既定値で埋めて読む。色は `Color.value` を符号付きの Long にした値で、sRGB の色は上位32ビットが ARGB
- `TemplateList.kt` には `Template(id = "template_N", ..., isNameInputPlaceholderEnabled = false)` の形で足す。`template_N` は連番、飾りの id は UUID にする
- テンプレートと候補の位置・文字の縁（`strokeWidth`・`secondBorderWidth`）・全体の縁（`overallBorderWidth`）は、基準の密度 2.625（Pixel_10）の px で持つ。アプリは `TemplateRepository` から取り出すとき（ホームの一覧も編集画面も）に、これらを `密度 / 2.625` 倍する（`TemplateDensity.kt`、#381）。ステッカーの縁はベクター画像の viewport（24×24）の座標で描かれ、ステッカーと一緒に伸びるので変えない
- 保存データはその端末の px なので、取り込むときは上の値を `密度 / 2.625` で割って基準の密度に戻す。密度は `wm density` の値（Override があればそちら）を 160 で割ったもの。取り込んだ `Template` のコメントに残す
- シェイクの書き出し（ログ `TemplateExport`）は `isPuffyEnabled`・`isPukupuku` を出さず、位置と縁の太さも端末の px のまま出す（そのまま貼ると二重に伸びる）ので、取り込みには使わない

会社の作業リポジトリの `scripts/materials/template_lab.py`（`candidates` と `pull`）は、この手順を自動にしたもの。なくても上の手順でできる。`candidates` は `TemplateCandidateList.kt` を丸ごと作り直すので、手で直した分は消える。
