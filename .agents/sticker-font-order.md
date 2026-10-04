# ステッカー・フォントの並び順・NEW ラベル・ライセンス台帳

`StickerAsset`（`ui/StickerAssets.kt`）と `FontFamilies`（`edit/FontFamilies.kt`）は、**enum の宣言順がそのまま選択画面の表示順と「1位〜5位」のバッジになる**。手で好みの順に並べないこと（バッジが実データと食い違うため）。

## 並び順

- GA4 の直近28日間の選択回数の多い順に並べる。ステッカーは `select_edit_sticker` の `label`、フォントは `select_edit_text_font` の `font_family`（どちらもカスタムディメンション登録済み）
- 選択回数が同じものは、並べ替え前の相対順を保つ
- `FontFamilies` は `finalFontRankBucket`（GA4 の `final_font_rank_bucket`）でも、バッジと同じ順位（NEW を除いた宣言順）を使う。NEW のフォントは `new` になる（#286）
- 並べ替えても保存済みのうちわは壊れない。Room は `Decoration` を kotlinx.serialization の JSON で保存し、enum は名前で書かれるため（`ConvertersTest` で確認している）。`FontFamiliesParceler` は ordinal を使うが、Parcel はプロセス内の一時的なもの

## NEW ラベル（`isNew`）

- 新しく追加したステッカー・フォントには `isNew = true` を付け、enum の**先頭**に置く（見つけてもらうため）。NEW バッジが付き、順位の対象から外れる（`buildRankIndexMap`）
- **NEW は付けてから2リリースで外す**（例：v2.7.0 で付けたら v2.9.0 で外す）。付けたバージョンは `licenses/materials.md` の「追加」列で確かめる。付けたときに、外す作業の Issue も立てておく。外すときは、上の手順で全体を並べ替え直す（外したものが宣言位置のまま順位争いに入り、上位のバッジを奪うため）
- 仕組み（`isNew` の引数、`buildRankIndexMap` の述語、`ItemBadge` の NEW 分岐、`badge_new`）は、次に追加するときのために残しておく

## ライセンス台帳

- ステッカー・フォントを足したり消したりしたら、同じ PR で `licenses/materials.md` を直す（素材の権利（ライセンス・著作権表示・同梱してよいか）を、コードを読まずに後から確かめられるようにするため）
- 今までにない出どころ（アイコン集・ライセンス）の素材を足したら、アプリの「設定 → 素材のライセンス」（`settings_license_message`・`settings_license_phosphor_notice`）にも載せる。MIT は著作権表示と許諾文を全文載せる決まりのため（#287）
