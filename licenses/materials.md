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

まだ書いていない（#287 で足す）。
