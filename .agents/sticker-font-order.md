# ステッカー・フォントの並び順と NEW ラベル

`StickerAsset`（`ui/StickerAssets.kt`）と `FontFamilies`（`edit/FontFamilies.kt`）は、**enum の宣言順がそのまま選択画面の表示順と「1位〜5位」のバッジになる**。手で好みの順に並べないこと（バッジが実データと食い違うため）。

## 並び順

- GA4 の直近28日間の選択回数の多い順に並べる。ステッカーは `select_edit_sticker` の `label`、フォントは `select_edit_text_font` の `font_family`（どちらもカスタムディメンション登録済み）
- 選択回数が同じものは、並べ替え前の相対順を保つ
- `FontFamilies` は `finalFontRankBucket`（GA4 の `final_font_rank_bucket`）でも宣言順を順位として使う
- 並べ替えても保存済みのうちわは壊れない。Room は `Decoration` を kotlinx.serialization の JSON で保存し、enum は名前で書かれるため（`ConvertersTest` で確認している）。`FontFamiliesParceler` は ordinal を使うが、Parcel はプロセス内の一時的なもの

## NEW ラベル（`isNew`）

- 新しく追加したステッカー・フォントには `isNew = true` を付ける。NEW バッジが付き、順位の対象から外れる（`buildRankIndexMap`）
- **NEW は付けてから2リリースで外す**（例：v2.7.0 で付けたら v2.9.0 で外す）。外すときは、上の手順で全体を並べ替え直す（外したものが宣言位置のまま順位争いに入り、上位のバッジを奪うため）
- 仕組み（`isNew` の引数、`buildRankIndexMap` の述語、`ItemBadge` の NEW 分岐、`badge_new`）は、次に追加するときのために残しておく
