package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

enum class DecorationColors(
    private val colorValue: Long
) {
    CYAN(0xFF000000),
    RED(0xFFFF0000),
    MAGENTA(0xFFFF00FF),
    BLUE(0xFF0000FF),
    GREEN(0xFF00FF00),
    YELLOW(0xFFFFFF00),
    WHITE(0xFFFFFFFF);

    val value: Color
        get() = Color(colorValue)
}
