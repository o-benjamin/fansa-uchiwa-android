# 素材のライセンス台帳

アプリで使っている素材（フォント・ステッカー）の出どころとライセンスの一覧。素材を足したり消したりしたら、同じ PR でこの表を直す（`.agents/sticker-font-order.md`）。

## フォント

Google Fonts の表はフォント名の ABC 順に並べている（enum の並び替えのたびに直さなくてよいように）。

### Google Fonts

アプリには含めず、端末で Google Play 開発者サービスからダウンロードして使う（`GoogleFont`、`ui/theme/Typography.kt`）。ライセンスは [google/fonts](https://github.com/google/fonts) のリポジトリで確認した（2026-10-04）。「追加」が — のものは、台帳を作る前（v2.8.x 以前）から入っている。

| enum | フォント | ライセンス | 著作権表示 | 追加 |
|---|---|---|---|---|
| `AOBOSHI_ONE` | Aoboshi One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/aoboshione/OFL.txt) | Copyright 2020 The Aoboshi Font Project Authors (https://github.com/matsuba723/Aoboshi), all rights reserved. | v2.9.0（#286） |
| `CHERRY_BOMB_ONE` | Cherry Bomb One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/cherrybombone/OFL.txt) | Copyright 2019 The Cherry Bomb Project Authors (https://github.com/satsuyako/CherryBomb) | v2.9.0（#286） |
| `CHOKOKUTAI` | Chokokutai | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/chokokutai/OFL.txt) | Copyright 2020 The Chokokutai Project Authors (https://github.com/go108go/Chokokutai) | v2.9.0（#286） |
| `DARUMADROP_ONE` | Darumadrop One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/darumadropone/OFL.txt) | Copyright 2020 The Darumadrop One Project Authors (https://github.com/ManiackersDesign/darumadrop) | v2.9.0（#286） |
| `DELA_GOTHIC_ONE` | Dela Gothic One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/delagothicone/OFL.txt) | Copyright 2020 The Dela Gothic Project Authors (https://github.com/syakuzen/DelaGothic) | — |
| `DOT_GOTHIC_16` | DotGothic16 | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/dotgothic16/OFL.txt) | Copyright 2020 The DotGothic16 Project Authors (https://github.com/fontworks-fonts/DotGothic16) | — |
| `HACHI_MARU_POP` | Hachi Maru Pop | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/hachimarupop/OFL.txt) | Copyright 2020 The Hachi Maru Pop Project Authors (https://github.com/noriokanisawa/HachiMaruPop) | — |
| `KAISEI_DECOL` | Kaisei Decol | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/kaiseidecol/OFL.txt) | Copyright 2020 The Kaisei Project Authors (https://github.com/Font-Kai/Kaisei) | v2.9.0（#286） |
| `KAISEI_TOKUMIN` | Kaisei Tokumin | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/kaiseitokumin/OFL.txt) | Copyright 2020 The Kaisei Project Authors (https://github.com/Font-Kai/Kaisei) | v2.9.0（#286） |
| `KIWI_MARU` | Kiwi Maru | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/kiwimaru/OFL.txt) | Copyright 2020 The Kiwi Maru Project Authors (https://github.com/Kiwi-KawagotoKajiru/Kiwi-Maru) | — |
| `KLEE_ONE` | Klee One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/kleeone/OFL.txt) | Copyright 2020 The Klee Project Authors (https://github.com/fontworks-fonts/Klee) | — |
| `KOSUGI` | Kosugi | [Apache 2.0](https://github.com/google/fonts/blob/main/apache/kosugi/LICENSE.txt) | Copyright 2010 The Kosugi Project Authors (https://github.com/googlefonts/kosugi) | — |
| `KOSUGI_MARU` | Kosugi Maru | [Apache 2.0](https://github.com/google/fonts/blob/main/apache/kosugimaru/LICENSE.txt) | Copyright 2010 The Kosugi Maru Project Authors (https://github.com/googlefonts/kosugi-maru) | — |
| `M_PLUS_1` | M PLUS 1 | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mplus1/OFL.txt) | Copyright 2021 The M+ FONTS Project Authors (https://github.com/coz-m/MPLUS_FONTS) | — |
| `M_PLUS_1_CODE` | M PLUS 1 Code | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mplus1code/OFL.txt) | Copyright 2021 The M+ FONTS Project Authors (https://github.com/coz-m/MPLUS_FONTS) | — |
| `M_PLUS_1P` | M PLUS 1p | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mplus1p/OFL.txt) | Copyright 2016 The M+ Project Authors. | — |
| `M_PLUS_2` | M PLUS 2 | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mplus2/OFL.txt) | Copyright 2021 The M+ FONTS Project Authors (https://github.com/coz-m/MPLUS_FONTS) | — |
| `M_PLUS_ROUNDED_1C` | M PLUS Rounded 1c | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mplusrounded1c/METADATA.pb)（OFL.txt はなく METADATA.pb に記載） | Copyright 2016 The Rounded M+ Project Authors. | — |
| `MOCHIY_POP_ONE` | Mochiy Pop One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mochiypopone/OFL.txt) | Copyright 2020 The Mochiypop Project Authors (https://github.com/fontdasu/Mochiypop) | — |
| `MOCHIY_POP_P_ONE` | Mochiy Pop P One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/mochiypoppone/OFL.txt) | Copyright 2020 The Mochiypop Project Authors (https://github.com/fontdasu/Mochiypop) | — |
| `MONOMANIAC_ONE` | Monomaniac One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/monomaniacone/OFL.txt) | Copyright 2020 The Monomaniac Project Authors (https://github.com/ManiackersDesign/monomaniac), all rights reserved. | v2.9.0（#286） |
| `NOTO_SANS_JP` | Noto Sans JP | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/notosansjp/OFL.txt) | Copyright 2014-2021 Adobe (http://www.adobe.com/), with Reserved Font Name 'Source' | — |
| `NOTO_SERIF_JP` | Noto Serif JP | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/notoserifjp/OFL.txt) | Copyright 2012 Google Inc. All Rights Reserved. | — |
| `PALETTE_MOSAIC` | Palette Mosaic | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/palettemosaic/OFL.txt) | Copyright 2020 The Palette Mosaic Project Authors (https://github.com/shibuyafont/Palette-mosaic-font-mono), all rights reserved. | v2.9.0（#286） |
| `POTTA_ONE` | Potta One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/pottaone/OFL.txt) | Copyright 2020 The Potta Project Authors (https://github.com/go108go/Potta) | — |
| `RAMPART_ONE` | Rampart One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/rampartone/OFL.txt) | Copyright 2020 The Rampart Project Authors (https://github.com/fontworks-fonts/Rampart) | — |
| `REGGAE_ONE` | Reggae One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/reggaeone/OFL.txt) | Copyright 2020 The Reggae Project Authors (https://github.com/fontworks-fonts/Reggae) | — |
| `ROCKNROLL_ONE` | RocknRoll One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/rocknrollone/OFL.txt) | Copyright 2020 The RocknRoll Project Authors (https://github.com/fontworks-fonts/RocknRoll) | — |
| `SAWARABI_GOTHIC` | Sawarabi Gothic | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/sawarabigothic/OFL.txt) | Copyright 2016 The Sawarabi Gothic Project Authors. | — |
| `SAWARABI_MINCHO` | Sawarabi Mincho | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/sawarabimincho/OFL.txt) | Copyright 2024 The Sawarabi Mincho Project Authors (https://github.com/googlefonts/sawarabi-mincho/) | — |
| `SHIPPORI_ANTIQUE` | Shippori Antique | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/shipporiantique/OFL.txt) | Copyright 2020 The Shippori Antique Project Authors (https://github.com/fontdasu/ShipporiAntique) | — |
| `SHIPPORI_ANTIQUE_B1` | Shippori Antique B1 | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/shipporiantiqueb1/OFL.txt) | Copyright 2020 The Shippori Antique Project Authors (https://github.com/fontdasu/ShipporiAntique) | — |
| `SHIPPORI_MINCHO` | Shippori Mincho | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/shipporimincho/OFL.txt) | Copyright 2021 The Shippori Mincho Project Authors (https://github.com/fontdasu/ShipporiMincho) | — |
| `SHIPPORI_MINCHO_B1` | Shippori Mincho B1 | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/shipporiminchob1/OFL.txt) | Copyright 2021 The Shippori Mincho Project Authors (https://github.com/fontdasu/ShipporiMincho) | — |
| `STICK` | Stick | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/stick/OFL.txt) | Copyright 2020 The Stick Project Authors (https://github.com/fontworks-fonts/Stick) | — |
| `TRAIN_ONE` | Train One | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/trainone/OFL.txt) | Copyright 2020 The Train Project Authors (https://github.com/fontworks-fonts/Train) | — |
| `WDXL_LUBRIFONT_JP_N` | WDXL Lubrifont JP N | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/wdxllubrifontjpn/OFL.txt) | Copyright 2025 The WDXL Lubrifont Project Authors (https://github.com/NightFurySL2001/WD-XL-font) | v2.9.0（#286） |
| `YOMOGI` | Yomogi | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/yomogi/OFL.txt) | Copyright 2020 The Yomogi Project Authors (https://github.com/satsuyako/YomogiFont), all rights reserved. | — |
| `YUSEI_MAGIC` | Yusei Magic | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/yuseimagic/OFL.txt) | Copyright 2020 The Yusei Magic Project Authors (https://github.com/tanukifont/YuseiMagic) | — |
| `ZEN_ANTIQUE` | Zen Antique | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenantique/OFL.txt) | Copyright 2021 The Zen Antique Project Authors (https://github.com/googlefonts/zen-antique) | — |
| `ZEN_ANTIQUE_SOFT` | Zen Antique Soft | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenantiquesoft/OFL.txt) | Copyright 2021 The Zen Antique Project Authors (https://github.com/googlefonts/zen-antique) | — |
| `ZEN_KAKU_GOTHIC_ANTIQUE` | Zen Kaku Gothic Antique | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenkakugothicantique/OFL.txt) | Copyright 2022 The Zen Kaku Gothic Project Authors (https://github.com/googlefonts/zen-kakugothic) | — |
| `ZEN_KAKU_GOTHIC_NEW` | Zen Kaku Gothic New | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenkakugothicnew/OFL.txt) | Copyright 2022 The Zen Kaku Gothic Project Authors (https://github.com/googlefonts/zen-kakugothic) | — |
| `ZEN_KURENAIDO` | Zen Kurenaido | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenkurenaido/OFL.txt) | Copyright 2021 The Zen Kurenaido Project Authors (https://github.com/googlefonts/zen-kurenaido) | — |
| `ZEN_MARU_GOTHIC` | Zen Maru Gothic | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenmarugothic/OFL.txt) | Copyright 2021 The Zen Maru Gothic Project Authors (https://github.com/googlefonts/zen-marugothic) | — |
| `ZEN_OLD_MINCHO` | Zen Old Mincho | [OFL 1.1](https://github.com/google/fonts/blob/main/ofl/zenoldmincho/OFL.txt) | Copyright 2021 The Zen Old Mincho Project Authors (https://github.com/googlefonts/zen-oldmincho) | — |

### アプリに同梱しているフォント

`app/src/main/res/font/` に入っている。規約は未確認（#296）。

| enum | フォント | ファイル | ライセンス |
|---|---|---|---|
| `KEI_FONT` | けいふぉんと | `keifont.ttf` | 未確認（#296） |
| `LIGHT_NOVEL_POP` | ラノベPOP v2 | `lightnovelpopv2.otf` | 未確認（#296） |
| `AKAZUKI_POP` | あかずきんポップ | `akazukinpop.otf` | 未確認（#296） |

## ステッカー

1色の VectorDrawable で、`app/src/main/res/drawable/` に入っている（`StickerAsset`、`ui/StickerAssets.kt`）。表は enum の ABC 順に並べている。「追加」が — のものは、台帳を作る前（v2.8.x 以前）から入っている。

ライセンスの表示は、アプリの「設定 → 素材のライセンス」に載せている（Apache 2.0 は全文へのリンク、MIT は著作権表示と許諾文の全文）。出どころを足したら、この表示も同じ PR で直す。

出どころの確かめ方：

- v2.9.0（#287）で足した分は、アイコン集の版を固定して npm のパッケージから取り込み、パッケージの LICENSE を確かめた（2026-09-27）
  - Phosphor Icons：`@phosphor-icons/core@2.1.1` の `assets/fill/<アイコン名>-fill.svg`（[MIT](https://github.com/phosphor-icons/core/blob/main/LICENSE)）
  - Material Symbols：`@material-symbols/svg-400@0.47.5` の `rounded/<アイコン名>-fill.svg`。このパッケージは [marella/material-symbols](https://github.com/marella/material-symbols) による Google の Material Symbols の再配布で、パッケージも元の [google/material-design-icons](https://github.com/google/material-design-icons/blob/master/LICENSE) も Apache 2.0
  - どちらも座標を 24×24 に拡大縮小し、小数2桁に丸めて VectorDrawable にした。原本の SVG と数値が合わないのはこのため（形は変えていない）
- それより前の分は取り込みの記録がない。ファイル名の形（`baseline_`／`round_`〜`_24` は Android Studio の Vector Asset が Material Icons から作る名前、`〜_24px`／`rounded_〜_24` は Material Symbols から作る名前）とアイコン名から出どころを判断した。`STAR`・`THUMB_UP`・`BRIGHTNESS_2`・`BRIGHTNESS_3`・`HEART` は、Material Icons の SVG と形の数値が一致することも確かめた（2026-10-04）
- 「自作」は、このアプリのために生成 AI（Claude）が SVG で描き、VectorDrawable にしたもの。第三者の素材・作品を元にしていない。元の SVG はリポジトリになく、`app/src/main/res/drawable/` の XML が原本
- SVG Silh は、サイトのフッターに CC0（パブリックドメイン）と明記された無料 SVG 素材集（画像の元は Pixabay）。個別の素材ページは特定できておらず、台帳はサイトの表記に基づく（2026-10-10）
- Material Icons と Material Symbols の LICENSE（Apache 2.0）には著作権者の行がなく、NOTICE ファイルもない。表の「Google」は権利者の名前で、写さなければならない表示の文ではない

| enum | ファイル | 出どころ（アイコン名） | ライセンス | 著作権表示 | 追加 |
|---|---|---|---|---|---|
| `AUDIO_TRACK` | `round_audiotrack_24.xml` | Material Icons（`audiotrack`、Round） | Apache 2.0 | Google | — |
| `AUTO_AWESOME` | `round_auto_awesome_24.xml` | Material Icons（`auto_awesome`、Round） | Apache 2.0 | Google | — |
| `BALLOON` | `sticker_balloon.xml` | Phosphor Icons（`balloon`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `BOLT` | `round_bolt_24.xml` | Material Icons（`bolt`、Round） | Apache 2.0 | Google | — |
| `BRIGHTNESS_1` | `baseline_brightness_1_24.xml` | Material Icons（`brightness_1`、Filled） | Apache 2.0 | Google | — |
| `BRIGHTNESS_2` | `baseline_brightness_2_24.xml` | Material Icons（`brightness_2`、Filled） | Apache 2.0 | Google | — |
| `BRIGHTNESS_3` | `baseline_brightness_3_24.xml` | Material Icons（`brightness_3`、Filled） | Apache 2.0 | Google | — |
| `BURST_BUBBLE` | `sticker_burst_bubble.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `CAKE` | `round_cake_24.xml` | Material Icons（`cake`、Round） | Apache 2.0 | Google | — |
| `CELEBRATION` | `sticker_celebration.xml` | Material Symbols（`celebration`） | Apache 2.0 | Google | v2.9.0（#287） |
| `CHESS` | `chess_24px.xml` | Material Symbols（`chess`） | Apache 2.0 | Google | — |
| `CHESS_QUEEN` | `chess_queen_24px.xml` | Material Symbols（`chess_queen`） | Apache 2.0 | Google | — |
| `CROWN` | `crown_24px.xml` | Material Symbols（`crown`） | Apache 2.0 | Google | — |
| `CROWN_SIMPLE` | `sticker_crown_simple.xml` | Phosphor Icons（`crown-simple`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `CURVED_ARROW` | `sticker_curved_arrow.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `DIAMOND` | `sticker_diamond.xml` | Material Symbols（`diamond`） | Apache 2.0 | Google | v2.9.0（#287） |
| `EMPHASIS_MARKS` | `sticker_emphasis_marks.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `EYEGLASSES` | `rounded_eyeglasses_2_24.xml` | Material Symbols（`eyeglasses_2`、Rounded） | Apache 2.0 | Google | — |
| `FLOWER` | `sticker_flower.xml` | Phosphor Icons（`flower`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `GIFT` | `sticker_gift.xml` | Phosphor Icons（`gift`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `HAND_PEACE` | `sticker_hand_peace.xml` | Phosphor Icons（`hand-peace`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `HEART` | `sticker_heart.xml` | Material Icons（`favorite`、Filled） | Apache 2.0 | Google | — |
| `HEART_ARROW` | `sticker_heart_arrow.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `HEART_CUTE` | `sticker_heart_cute.xml` | SVG Silh（svgsilh.com）の素材 | CC0（サイトの表記） | 表示の義務なし | — |
| `HEART_HORIZONTAL` | `sticker_heart_horizontal.xml` | SVG Silh（svgsilh.com）の素材 | CC0（サイトの表記） | 表示の義務なし | — |
| `HEART_VERTICAL` | `sticker_heart_vertical.xml` | SVG Silh（svgsilh.com）の素材 | CC0（サイトの表記） | 表示の義務なし | — |
| `KISS_LIPS` | `sticker_kiss_lips.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `LOCAL_FIRE_DEPARTMENT` | `round_local_fire_department_24.xml` | Material Icons（`local_fire_department`、Round） | Apache 2.0 | Google | — |
| `MICROPHONE_STAGE` | `sticker_microphone_stage.xml` | Phosphor Icons（`microphone-stage`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `MUSIC_NOTES` | `sticker_music_notes.xml` | Phosphor Icons（`music-notes`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `PAN_TOOL_ALT` | `baseline_pan_tool_alt_24.xml` | Material Icons（`pan_tool_alt`、Filled） | Apache 2.0 | Google | — |
| `PETS` | `round_pets_24.xml` | Material Icons（`pets`、Round） | Apache 2.0 | Google | — |
| `RIBBON` | `sticker_ribbon.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `ROCKET` | `baseline_rocket_24.xml` | Material Icons（`rocket`、Filled） | Apache 2.0 | Google | — |
| `ROCKET_LAUNCH` | `baseline_rocket_launch_24.xml` | Material Icons（`rocket_launch`、Filled） | Apache 2.0 | Google | — |
| `SHOOTING_STAR` | `sticker_shooting_star.xml` | Phosphor Icons（`shooting-star`） | MIT | Copyright (c) 2023 Phosphor Icons | v2.9.0（#287） |
| `SPEECH_BUBBLE` | `sticker_speech_bubble.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |
| `STAR` | `baseline_star_24.xml` | Material Icons（`star`、Filled） | Apache 2.0 | Google | — |
| `STAR_ROUNDED` | `round_star_24.xml` | Material Icons（`star`、Round） | Apache 2.0 | Google | — |
| `THUMB_UP` | `round_thumb_up_24.xml` | Material Icons（`thumb_up`、Round） | Apache 2.0 | Google | — |
| `WAVING_HAND` | `baseline_waving_hand_24.xml` | Material Icons（`waving_hand`、Filled） | Apache 2.0 | Google | — |
| `WINGED_HEART` | `sticker_winged_heart.xml` | 自作 | なし（このアプリ用に作成） | 表示の義務なし | v2.9.0（#287） |

`HEART_CUTE`・`HEART_HORIZONTAL`・`HEART_VERTICAL` は v2.0.0（2026-03-17）で足した。作った経緯の記録がコミットになかったが、オーナーが出どころを SVG Silh（[svgsilh.com](https://svgsilh.com/ja/)）と突き止めた（2026-10-10、#302）。
