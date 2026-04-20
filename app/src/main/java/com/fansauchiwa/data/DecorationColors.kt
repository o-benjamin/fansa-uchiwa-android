package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

/**
 * Shared decoration palette used by templates, edit features, and export logic.
 * The enum keeps the canonical ARGB values outside the presentation layer.
 */
enum class DecorationColors(
    private val argb: ULong
) {
    CYAN(0xFF000000u),
    RED(0xFFFF0000u),
    MAGENTA(0xFFFF00FFu),
    BLUE(0xFF0000FFu),
    GREEN(0xFF00FF00u),
    YELLOW(0xFFFFFF00u),
    WHITE(0xFFFFFFFFu),
    ;

    val value: Color
        get() = Color(argb)

    companion object {
        fun from(color: Color): DecorationColors? = entries.find { it.value == color }
    }
}
