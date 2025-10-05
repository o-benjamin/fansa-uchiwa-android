package com.example.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Polymorphic
@Serializable
sealed interface Decoration {
    @Serializable
    data class Text(
        val text: String,
        @Serializable(with = OffsetSerializer::class)
        val offset: Offset,
        @Serializable(with = ColorSerializer::class)
        val color: Color,
        val size: Float
    ) : Decoration

    @Serializable
    data class Sticker(
        val type: String,
        @Serializable(with = OffsetSerializer::class)
        val offset: Offset,
        val size: Float
    ) : Decoration
}
