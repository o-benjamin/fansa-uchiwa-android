package com.fansauchiwa.edit

import androidx.compose.ui.text.font.FontFamily
import com.fansauchiwa.ui.theme.akazukinPopFontFamily
import com.fansauchiwa.ui.theme.delaGothicOneFontFamily
import com.fansauchiwa.ui.theme.dotGothic16FontFamily
import com.fansauchiwa.ui.theme.hachiMaruPopFontFamily
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
import com.fansauchiwa.ui.theme.notoSansJPFontFamily
import com.fansauchiwa.ui.theme.notoSerifJPFontFamily
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
 * 並びは GA4 の `select_edit_text_font` の選択回数の多い順（#241、2026-08-23〜09-19 の28日間）。
 * 並べ替えの手順と `isNew` の運用ルールは `.agents/sticker-font-order.md` を参照。
 */
@Serializable
enum class FontFamilies(val value: FontFamily, val isNew: Boolean = false) {
    M_PLUS_ROUNDED_1C(mPlusRounded1cFontFamily),
    MOCHIY_POP_ONE(mochiyPopOneFontFamily),
    MOCHIY_POP_P_ONE(mochiyPopPOneFontFamily),
    DELA_GOTHIC_ONE(delaGothicOneFontFamily),
    KEI_FONT(keiFontFamily),
    LIGHT_NOVEL_POP(lightNovelPopFontFamily),
    AKAZUKI_POP(akazukinPopFontFamily),
    ZEN_MARU_GOTHIC(zenMaruGothicFontFamily),
    M_PLUS_1P(mPlus1pFontFamily),
    HACHI_MARU_POP(hachiMaruPopFontFamily),
    RAMPART_ONE(rampartOneFontFamily),
    POTTA_ONE(pottaOneFontFamily),
    SHIPPORI_MINCHO_B1(shipporiMinchoB1FontFamily),
    ZEN_KAKU_GOTHIC_NEW(zenKakuGothicNewFontFamily),
    ROCKNROLL_ONE(rocknRollOneFontFamily),
    YUSEI_MAGIC(yuseiMagicFontFamily),
    SHIPPORI_MINCHO(shipporiMinchoFontFamily),
    SHIPPORI_ANTIQUE_B1(shipporiAntiqueBFontFamily),
    M_PLUS_1_CODE(mPlus1CodeFontFamily),
    ZEN_ANTIQUE(zenAntiqueFontFamily),
    M_PLUS_1(mPlus1FontFamily),
    REGGAE_ONE(reggaeOneFontFamily),
    M_PLUS_2(mPlus2FontFamily),
    KIWI_MARU(kiwiMaruFontFamily),
    ZEN_ANTIQUE_SOFT(zenAntiqueSoftFontFamily),
    TRAIN_ONE(trainOneFontFamily),
    KOSUGI(kosugiFontFamily),
    SHIPPORI_ANTIQUE(shipporiAntiqueFontFamily),
    KOSUGI_MARU(kosugiMaruFontFamily),
    NOTO_SANS_JP(notoSansJPFontFamily),
    ZEN_OLD_MINCHO(zenOldMinchoFontFamily),
    ZEN_KURENAIDO(zenKurenaidoFontFamily),
    YOMOGI(yomogiFontFamily),
    SAWARABI_GOTHIC(sawarabiGothicFontFamily),
    STICK(stickyFontFamily),
    DOT_GOTHIC_16(dotGothic16FontFamily),
    SAWARABI_MINCHO(sawarabiMinchoFontFamily),
    NOTO_SERIF_JP(notoSerifJPFontFamily),
    ZEN_KAKU_GOTHIC_ANTIQUE(zenKakuGothicAntiqueFontFamily),
    KLEE_ONE(kleeOneFontFamily),
}
