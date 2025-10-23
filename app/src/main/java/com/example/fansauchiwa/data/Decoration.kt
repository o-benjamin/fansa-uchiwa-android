package com.example.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.fansauchiwa.ui.StickerAsset
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
        val rotation: Float,
        @Serializable(with = ColorSerializer::class)
        val color: Color,
        val scale: Float
    ) : Decoration

    @Serializable
    data class Sticker(
        val label: String,
        @Serializable(with = OffsetSerializer::class)
        val offset: Offset,
        val rotation: Float,
        val scale: Float
    ) : Decoration {
        val resId = StickerAsset.entries.find { it.type == label }?.resId ?: 0
    }
}
