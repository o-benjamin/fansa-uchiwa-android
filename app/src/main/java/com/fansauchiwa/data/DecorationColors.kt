package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

/**
 * Shared decoration color definitions used by templates, exported code, and editor UI.
 * Keeping them outside the presentation package prevents lower layers from depending on UI namespaces.
 */
enum class DecorationColors(
    private val colorValue: ULong
) {
    CYAN(0xFF000000u),
    RED(0xFFFF0000u),
    MAGENTA(0xFFFF00FFu),
    BLUE(0xFF0000FFu),
    GREEN(0xFF00FF00u),
    YELLOW(0xFFFFFF00u),
    WHITE(0xFFFFFFFFu);

    val value: Color
        get() = Color(colorValue)
}
