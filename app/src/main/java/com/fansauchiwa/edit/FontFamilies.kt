package com.fansauchiwa.edit

import androidx.compose.ui.text.font.FontFamily
import com.fansauchiwa.ui.theme.delaGothicOneFontFamily
import com.fansauchiwa.ui.theme.dotGothic16FontFamily
import com.fansauchiwa.ui.theme.hachiMaruPopFontFamily
import com.fansauchiwa.ui.theme.kiwiMaruFontFamily
import com.fansauchiwa.ui.theme.kleeOneFontFamily
import com.fansauchiwa.ui.theme.kosugiFontFamily
import com.fansauchiwa.ui.theme.kosugiMaruFontFamily
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
enum class FontFamilies(val value: FontFamily) {
    HACHI_MARU_POP(hachiMaruPopFontFamily),
    ZEN_MARU_GOTHIC(zenMaruGothicFontFamily),
    M_PLUS_ROUNDED_1C(mPlusRounded1cFontFamily),
    M_PLUS_1P(mPlus1pFontFamily),
    ZEN_KAKU_GOTHIC_NEW(zenKakuGothicNewFontFamily),
    DELA_GOTHIC_ONE(delaGothicOneFontFamily),
    DOT_GOTHIC_16(dotGothic16FontFamily),
    RAMPART_ONE(rampartOneFontFamily),
    ROCKNROLL_ONE(rocknRollOneFontFamily),
    YUSEI_MAGIC(yuseiMagicFontFamily),
    KIWI_MARU(kiwiMaruFontFamily),
    KLEE_ONE(kleeOneFontFamily),
    KOSUGI(kosugiFontFamily),
    KOSUGI_MARU(kosugiMaruFontFamily),
    MOCHIY_POP_ONE(mochiyPopOneFontFamily),
    MOCHIY_POP_P_ONE(mochiyPopPOneFontFamily),
    M_PLUS_2(mPlus2FontFamily),
    M_PLUS_1(mPlus1FontFamily),
    M_PLUS_1_CODE(mPlus1CodeFontFamily),
    NOTO_SANS_JP(notoSansJPFontFamily),
    NOTO_SERIF_JP(notoSerifJPFontFamily),
    POTTA_ONE(pottaOneFontFamily),
    REGGAE_ONE(reggaeOneFontFamily),
    SAWARABI_GOTHIC(sawarabiGothicFontFamily),
    SAWARABI_MINCHO(sawarabiMinchoFontFamily),
    SHIPPORI_ANTIQUE_B1(shipporiAntiqueBFontFamily),
    SHIPPORI_ANTIQUE(shipporiAntiqueFontFamily),
    SHIPPORI_MINCHO(shipporiMinchoFontFamily),
    SHIPPORI_MINCHO_B1(shipporiMinchoB1FontFamily),
    STICK(stickyFontFamily),
    TRAIN_ONE(trainOneFontFamily),
    YOMOGI(yomogiFontFamily),
    ZEN_ANTIQUE(zenAntiqueFontFamily),
    ZEN_ANTIQUE_SOFT(zenAntiqueSoftFontFamily),
    ZEN_KAKU_GOTHIC_ANTIQUE(zenKakuGothicAntiqueFontFamily),
    ZEN_KURENAIDO(zenKurenaidoFontFamily),
    ZEN_OLD_MINCHO(zenOldMinchoFontFamily),
}
