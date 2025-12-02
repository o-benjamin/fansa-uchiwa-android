package com.example.fansauchiwa.edit

import androidx.compose.ui.text.font.FontFamily
import com.example.fansauchiwa.ui.theme.hachiMaruPop
import com.example.fansauchiwa.ui.theme.zenMaruGothicFontFamily
import kotlinx.serialization.Serializable

@Serializable
enum class FontFamilies(val value: FontFamily) {
    HACHI_MARU_POP(hachiMaruPop),
    ZEN_MARU_GOTHIC(zenMaruGothicFontFamily)
}