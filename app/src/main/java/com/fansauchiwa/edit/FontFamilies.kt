package com.fansauchiwa.edit

import androidx.compose.ui.text.font.FontFamily
import com.fansauchiwa.ui.theme.akazukinPopFontFamily
import com.fansauchiwa.ui.theme.aoboshiOneFontFamily
import com.fansauchiwa.ui.theme.cherryBombOneFontFamily
import com.fansauchiwa.ui.theme.chokokutaiFontFamily
import com.fansauchiwa.ui.theme.darumadropOneFontFamily
import com.fansauchiwa.ui.theme.delaGothicOneFontFamily
import com.fansauchiwa.ui.theme.dotGothic16FontFamily
import com.fansauchiwa.ui.theme.hachiMaruPopFontFamily
import com.fansauchiwa.ui.theme.kaiseiDecolFontFamily
import com.fansauchiwa.ui.theme.kaiseiTokuminFontFamily
import com.fansauchiwa.ui.theme.keiFontFamily
import com.fansauchiwa.ui.theme.kiwiMaruFontFamily
import com.fansauchiwa.ui.theme.kleeOneFontFamily
import com.fansauchiwa.ui.theme.kosugiFontFamily
import com.fansauchiwa.ui.theme.kosugiMaruFontFamily
import com.fansauchiwa.ui.theme.lightNovelPopFontFamily
import com.fansauchiwa.ui.theme.mPlus1CodeFontFamily
import com.fansauchiwa.ui.theme.mPlus1FontFamily
import com.fansauchiwa.ui.theme.mPlus1pFontFamily
import com.fansauchiwa.ui.theme.mPlus2FontFamily
import com.fansauchiwa.ui.theme.mPlusRounded1cFontFamily
import com.fansauchiwa.ui.theme.mochiyPopOneFontFamily
import com.fansauchiwa.ui.theme.mochiyPopPOneFontFamily
import com.fansauchiwa.ui.theme.monomaniacOneFontFamily
import com.fansauchiwa.ui.theme.notoSansJPFontFamily
import com.fansauchiwa.ui.theme.notoSerifJPFontFamily
import com.fansauchiwa.ui.theme.paletteMosaicFontFamily
import com.fansauchiwa.ui.theme.pottaOneFontFamily
import com.fansauchiwa.ui.theme.rampartOneFontFamily
import com.fansauchiwa.ui.theme.reggaeOneFontFamily
import com.fansauchiwa.ui.theme.rocknRollOneFontFamily
import com.fansauchiwa.ui.theme.sawarabiGothicFontFamily
import com.fansauchiwa.ui.theme.sawarabiMinchoFontFamily
import com.fansauchiwa.ui.theme.shipporiAntiqueBFontFamily
import com.fansauchiwa.ui.theme.shipporiAntiqueFontFamily
import com.fansauchiwa.ui.theme.shipporiMinchoB1FontFamily
import com.fansauchiwa.ui.theme.shipporiMinchoFontFamily
import com.fansauchiwa.ui.theme.stickyFontFamily
import com.fansauchiwa.ui.theme.trainOneFontFamily
import com.fansauchiwa.ui.theme.yomogiFontFamily
import com.fansauchiwa.ui.theme.yuseiMagicFontFamily
import com.fansauchiwa.ui.theme.zenAntiqueFontFamily
import com.fansauchiwa.ui.theme.zenAntiqueSoftFontFamily
import com.fansauchiwa.ui.theme.zenKakuGothicAntiqueFontFamily
import com.fansauchiwa.ui.theme.zenKakuGothicNewFontFamily
import com.fansauchiwa.ui.theme.zenKurenaidoFontFamily
import com.fansauchiwa.ui.theme.zenMaruGothicFontFamily
import com.fansauchiwa.ui.theme.zenOldMinchoFontFamily
import kotlinx.serialization.Serializable

/**
 * 文字入れで選べるフォント。宣言順がフォント選択画面の表示順と「1位〜5位」のバッジになる。
 * 並びは GA4 の `select_edit_text_font` の選択回数の多い順（#298、2026-09-19〜10-16 の28日間）。
 * v2.9.0 で足した8個（#286。`licenses/materials.md` の「追加」列が v2.9.0 のフォント）は、10-05 の公開からの12日分しか数えていないが、決まりどおり28日間の合計で並べた。
 * 公開後の12日間だけで並べると `CHERRY_BOMB_ONE` が上位5個に入るが、その間8個は NEW 付きで先頭に並んでいて、目に付きやすい分だけ多めに選ばれているため。
 * `isNew = true` のフォントは先頭に置き、順位の対象から外す。今の NEW はない。
 * `isRetired = true` のフォントは選択画面に出さない（[selectableFontFamilies]）。保存済みのうちわを読み込めるように enum の値だけ残し、末尾に置く（#346）。
 * 並べ替えの手順と `isNew` の運用ルールは `.agents/sticker-font-order.md` を参照。
 */
@Serializable
enum class FontFamilies(
    val value: FontFamily,
    val isNew: Boolean = false,
    val isRetired: Boolean = false,
) {
    MOCHIY_POP_ONE(mochiyPopOneFontFamily),
    M_PLUS_ROUNDED_1C(mPlusRounded1cFontFamily),
    MOCHIY_POP_P_ONE(mochiyPopPOneFontFamily),
    KEI_FONT(keiFontFamily),
    DELA_GOTHIC_ONE(delaGothicOneFontFamily),
    LIGHT_NOVEL_POP(lightNovelPopFontFamily),
    AKAZUKI_POP(akazukinPopFontFamily),
    ZEN_MARU_GOTHIC(zenMaruGothicFontFamily),
    HACHI_MARU_POP(hachiMaruPopFontFamily),
    RAMPART_ONE(rampartOneFontFamily),
    POTTA_ONE(pottaOneFontFamily),
    CHERRY_BOMB_ONE(cherryBombOneFontFamily),
    M_PLUS_1P(mPlus1pFontFamily),
    DARUMADROP_ONE(darumadropOneFontFamily),
    SHIPPORI_MINCHO_B1(shipporiMinchoB1FontFamily),
    YUSEI_MAGIC(yuseiMagicFontFamily),
    SHIPPORI_MINCHO(shipporiMinchoFontFamily),
    MONOMANIAC_ONE(monomaniacOneFontFamily),
    ZEN_KAKU_GOTHIC_NEW(zenKakuGothicNewFontFamily),
    KAISEI_TOKUMIN(kaiseiTokuminFontFamily),
    ROCKNROLL_ONE(rocknRollOneFontFamily),
    PALETTE_MOSAIC(paletteMosaicFontFamily),
    TRAIN_ONE(trainOneFontFamily),
    SHIPPORI_ANTIQUE_B1(shipporiAntiqueBFontFamily),
    REGGAE_ONE(reggaeOneFontFamily),
    ZEN_ANTIQUE(zenAntiqueFontFamily),
    CHOKOKUTAI(chokokutaiFontFamily),
    YOMOGI(yomogiFontFamily),
    AOBOSHI_ONE(aoboshiOneFontFamily),
    M_PLUS_2(mPlus2FontFamily),
    SAWARABI_MINCHO(sawarabiMinchoFontFamily),
    NOTO_SERIF_JP(notoSerifJPFontFamily),
    KAISEI_DECOL(kaiseiDecolFontFamily),
    ZEN_KAKU_GOTHIC_ANTIQUE(zenKakuGothicAntiqueFontFamily),
    ZEN_ANTIQUE_SOFT(zenAntiqueSoftFontFamily),
    SHIPPORI_ANTIQUE(shipporiAntiqueFontFamily),
    M_PLUS_1_CODE(mPlus1CodeFontFamily),
    M_PLUS_1(mPlus1FontFamily),
    KIWI_MARU(kiwiMaruFontFamily),
    ZEN_OLD_MINCHO(zenOldMinchoFontFamily),
    DOT_GOTHIC_16(dotGothic16FontFamily),
    NOTO_SANS_JP(notoSansJPFontFamily),
    KOSUGI_MARU(kosugiMaruFontFamily),
    STICK(stickyFontFamily),
    KOSUGI(kosugiFontFamily),
    ZEN_KURENAIDO(zenKurenaidoFontFamily),
    SAWARABI_GOTHIC(sawarabiGothicFontFamily),
    KLEE_ONE(kleeOneFontFamily),

    // ここから下は選択画面に出さないフォント。順位も持たない（[fontRankIndexMap]）。選べるフォントの並び（GA4 の順）と混ざらないように末尾に置く

    /**
     * v2.9.0 で足したが、Android のダウンロード式フォントの一覧（https://fonts.gstatic.com/s/a/directory.xml）になく、
     * どの端末でも取得できなかった（#346）。取得に失敗すると標準の書体で描かれていたので、保存済みのうちわの見た目が変わらないように [FontFamily.Default] で描く。
     */
    WDXL_LUBRIFONT_JP_N(FontFamily.Default, isRetired = true),
}

/** フォント選択画面（文字タブ・「全体」タブ）に並べるフォント。宣言順のまま、[FontFamilies.isRetired] のものを除く */
val selectableFontFamilies: List<FontFamilies> = FontFamilies.entries.filterNot { it.isRetired }

/**
 * フォント選択画面の「1位〜5位」のバッジと、GA4 の `final_font_rank_bucket` で使う順位（0始まり。NEW と外したフォントは null）。
 * 2つの順位がずれないように、ここ1か所で求める。
 */
val fontRankIndexMap: Map<FontFamilies, Int?> =
    buildRankIndexMap(FontFamilies.entries) { it.isNew || it.isRetired }
