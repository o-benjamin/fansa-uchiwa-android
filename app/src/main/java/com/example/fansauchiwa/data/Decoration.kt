package com.example.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

sealed interface Decoration {
    data class Text(
        val text: String,
        val offset: Offset,
        val color: Color,
        val size: Float
    ) : Decoration

    data class Sticker(
        val type: String,
        val offset: Offset,
        val size: Float
    ) : Decoration
}
