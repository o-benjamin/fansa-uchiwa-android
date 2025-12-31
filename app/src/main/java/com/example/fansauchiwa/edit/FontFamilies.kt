package com.example.fansauchiwa.edit

import androidx.compose.ui.text.font.FontFamily
import com.example.fansauchiwa.ui.theme.delaGothicOneFontFamily
import com.example.fansauchiwa.ui.theme.dotGothic16FontFamily
import com.example.fansauchiwa.ui.theme.hachiMaruPopFontFamily
import com.example.fansauchiwa.ui.theme.kiwiMaruFontFamily
import com.example.fansauchiwa.ui.theme.kleeOneFontFamily
import com.example.fansauchiwa.ui.theme.kosugiFontFamily
import com.example.fansauchiwa.ui.theme.kosugiMaruFontFamily
import com.example.fansauchiwa.ui.theme.mPlus1CodeFontFamily
import com.example.fansauchiwa.ui.theme.mPlus1FontFamily
import com.example.fansauchiwa.ui.theme.mPlus1pFontFamily
import com.example.fansauchiwa.ui.theme.mPlus2FontFamily
import com.example.fansauchiwa.ui.theme.mPlusRounded1cFontFamily
import com.example.fansauchiwa.ui.theme.mochiyPopOneFontFamily
import com.example.fansauchiwa.ui.theme.mochiyPopPOneFontFamily
import com.example.fansauchiwa.ui.theme.notoSansJPFontFamily
import com.example.fansauchiwa.ui.theme.notoSerifJPFontFamily
import com.example.fansauchiwa.ui.theme.pottaOneFontFamily
import com.example.fansauchiwa.ui.theme.rampartOneFontFamily
import com.example.fansauchiwa.ui.theme.reggaeOneFontFamily
import com.example.fansauchiwa.ui.theme.rocknRollOneFontFamily
import com.example.fansauchiwa.ui.theme.sawarabiGothicFontFamily
import com.example.fansauchiwa.ui.theme.sawarabiMinchoFontFamily
import com.example.fansauchiwa.ui.theme.shipporiAntiqueBFontFamily
import com.example.fansauchiwa.ui.theme.shipporiAntiqueFontFamily
import com.example.fansauchiwa.ui.theme.shipporiMinchoB1FontFamily
import com.example.fansauchiwa.ui.theme.shipporiMinchoFontFamily
import com.example.fansauchiwa.ui.theme.stickyFontFamily
import com.example.fansauchiwa.ui.theme.trainOneFontFamily
import com.example.fansauchiwa.ui.theme.yomogiFontFamily
import com.example.fansauchiwa.ui.theme.yuseiMagicFontFamily
import com.example.fansauchiwa.ui.theme.zenAntiqueFontFamily
import com.example.fansauchiwa.ui.theme.zenAntiqueSoftFontFamily
import com.example.fansauchiwa.ui.theme.zenKakuGothicAntiqueFontFamily
import com.example.fansauchiwa.ui.theme.zenKakuGothicNewFontFamily
import com.example.fansauchiwa.ui.theme.zenKurenaidoFontFamily
import com.example.fansauchiwa.ui.theme.zenMaruGothicFontFamily
import com.example.fansauchiwa.ui.theme.zenOldMinchoFontFamily
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
