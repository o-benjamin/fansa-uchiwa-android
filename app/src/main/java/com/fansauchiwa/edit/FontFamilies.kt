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

@Serializable
enum class FontFamilies(val value: FontFamily, val isNew: Boolean = false) {
    KEI_FONT(keiFontFamily, isNew = true),
    LIGHT_NOVEL_POP(lightNovelPopFontFamily, isNew = true),
    AKAZUKI_POP(akazukinPopFontFamily, isNew = true),
    M_PLUS_ROUNDED_1C(mPlusRounded1cFontFamily),
    DELA_GOTHIC_ONE(delaGothicOneFontFamily),
    ZEN_MARU_GOTHIC(zenMaruGothicFontFamily),
    MOCHIY_POP_ONE(mochiyPopOneFontFamily),
    MOCHIY_POP_P_ONE(mochiyPopPOneFontFamily),
    M_PLUS_1P(mPlus1pFontFamily),
    ZEN_KAKU_GOTHIC_NEW(zenKakuGothicNewFontFamily),
    SHIPPORI_MINCHO_B1(shipporiMinchoB1FontFamily),
    HACHI_MARU_POP(hachiMaruPopFontFamily),
    YUSEI_MAGIC(yuseiMagicFontFamily),
    RAMPART_ONE(rampartOneFontFamily),
    SHIPPORI_ANTIQUE_B1(shipporiAntiqueBFontFamily),
    ROCKNROLL_ONE(rocknRollOneFontFamily),
    SHIPPORI_MINCHO(shipporiMinchoFontFamily),
    POTTA_ONE(pottaOneFontFamily),
    KIWI_MARU(kiwiMaruFontFamily),
    M_PLUS_1_CODE(mPlus1CodeFontFamily),
    KOSUGI(kosugiFontFamily),
    DOT_GOTHIC_16(dotGothic16FontFamily),
    M_PLUS_1(mPlus1FontFamily),
    REGGAE_ONE(reggaeOneFontFamily),
    SHIPPORI_ANTIQUE(shipporiAntiqueFontFamily),
    M_PLUS_2(mPlus2FontFamily),
    KOSUGI_MARU(kosugiMaruFontFamily),
    SAWARABI_MINCHO(sawarabiMinchoFontFamily),
    NOTO_SANS_JP(notoSansJPFontFamily),
    KLEE_ONE(kleeOneFontFamily),
    ZEN_ANTIQUE_SOFT(zenAntiqueSoftFontFamily),
    NOTO_SERIF_JP(notoSerifJPFontFamily),
    ZEN_KAKU_GOTHIC_ANTIQUE(zenKakuGothicAntiqueFontFamily),
    TRAIN_ONE(trainOneFontFamily),
    SAWARABI_GOTHIC(sawarabiGothicFontFamily),
    ZEN_OLD_MINCHO(zenOldMinchoFontFamily),
    ZEN_ANTIQUE(zenAntiqueFontFamily),
    ZEN_KURENAIDO(zenKurenaidoFontFamily),
    YOMOGI(yomogiFontFamily),
    STICK(stickyFontFamily),
}
