# ステッカー・フォントの並び順・NEW ラベル・ライセンス台帳

`StickerAsset`（`ui/StickerAssets.kt`）と `FontFamilies`（`edit/FontFamilies.kt`）は、**enum の宣言順がそのまま選択画面の表示順と「1位〜5位」のバッジになる**。手で好みの順に並べないこと（バッジが実データと食い違うため）。

## 並び順

- GA4 の直近28日間の選択回数の多い順に並べる。ステッカーは `select_edit_sticker` の `label`、フォントは `select_edit_text_font` の `font_family`（どちらもカスタムディメンション登録済み）
    - 「全体」タブで文字すべてのフォントをまとめて選んだとき（#308、v2.10.0 から）も、`select_edit_text_font` を1回送っている。`select_edit_all_text` を足して数えると二重になるので、`select_edit_text_font` だけを数える
- 選択回数が同じものは、並べ替え前の相対順を保つ
- `FontFamilies` は `finalFontRankBucket`（GA4 の `final_font_rank_bucket`）でも、バッジと同じ順位（NEW を除いた宣言順）を使う。NEW のフォントは `new` になる（#286）
- 並べ替えても保存済みのうちわは壊れない。Room は `Decoration` を kotlinx.serialization の JSON で保存し、フォントは enum の名前（`KLEE_ONE`）、ステッカーは `Decoration.Sticker.label`（= `StickerAsset.type` の文字列、`heart`）で書かれるため（`ConvertersTest` で確認している）。ステッカーの `type` は保存のキーなので、enum の名前を変えるときも `type` は変えない。`FontFamiliesParceler` は ordinal を使うが、Parcel はプロセス内の一時的なもの

## NEW ラベル（`isNew`）

- 新しく追加したステッカー・フォントには `isNew = true` を付け、enum の**先頭**に置く（見つけてもらうため）。NEW バッジが付き、順位の対象から外れる（`buildRankIndexMap`）
- **NEW は付けてから2リリースで外す**（例：v2.7.0 で付けたら v2.9.0 で外す）。付けたバージョンは `licenses/materials.md` の「追加」列で確かめる。付けたときに、外す作業の Issue も立てておく。外すときは、上の手順で全体を並べ替え直す（外したものが宣言位置のまま順位争いに入り、上位のバッジを奪うため）
- 仕組み（`isNew` の引数、`buildRankIndexMap` の述語、`ItemBadge` の NEW 分岐、`badge_new`）は、次に追加するときのために残しておく

## ライセンス台帳

- ステッカー・フォントを足したり消したりしたら、同じ PR で `licenses/materials.md` を直す（素材の権利（ライセンス・著作権表示・同梱してよいか）を、コードを読まずに後から確かめられるようにするため）
- 今までにない出どころ（アイコン集・ライセンス）の素材を足したら、アプリの「設定 → 素材のライセンス」（`SettingsScreen.kt` のダイアログ）にも載せる（#287）
  - Apache 2.0 なら、`settings_license_message` の括弧に出どころの名前を足す
  - MIT のように著作権表示と許諾文を全文載せる決まりのライセンスなら、出どころごとに `settings_license_<出どころ>_intro`（前置き）と `settings_license_<出どころ>_notice`（原文のまま、`translatable="false"`）を足し、ダイアログに並べる

## ステッカーの drawable の条件

`StickerItemContent.kt` は VectorDrawable の pathData を取り出し、viewport の座標で拡大縮小して、指定の色で塗り・縁取りを描く。そのため、足す drawable は次を満たすこと（#287）。

- viewport は 24×24。縁取りの太さは viewport の座標の単位で決まるので、256×256 などのまま入れると縁取りが細くなる
- 形は塗りの path だけで作る。XML の `strokeColor`・`fillType`・`fillAlpha`・clip-path は描画に使われず、すべての path が塗られる。穴は nonzero の規則で決まる（evenodd を前提にした穴は埋まる）
- 1色。色はアプリで付ける
- 白い縁取りは穴の内側にもかかるので、小さい穴や細い線は埋まる

SVG から作るときは、座標を 24×24 に拡大縮小してから VectorDrawable にする（道具は問わない。#287 の分は、SVG の座標を 24×24 に拡大縮小・移動して小数2桁に丸めた）。足したら、縁取りを付けた状態で形が崩れていないかを見本の画像で確かめる。
