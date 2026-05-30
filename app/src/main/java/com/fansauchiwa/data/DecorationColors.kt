package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class DecorationColors(
    private val colorValue: Long
) {
    RED(0xFFFF0000),
    PINK(0xFFFF3399),
    ORANGE(0xFFFF6600),
    YELLOW(0xFFFFCC00),
    LIGHT_GREEN(0xFF66FF00),
    GREEN(0xFF00CC44),
    LIGHT_BLUE(0xFF33CCFF),
    BLUE(0xFF0044FF),
    PURPLE(0xFF9900FF),
    GRAY(0xFF999999),
    WHITE(0xFFFFFFFF),
    BLACK(0xFF000000);

    val value: Color
        get() = Color(colorValue)
}