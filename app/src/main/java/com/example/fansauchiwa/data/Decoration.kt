package com.example.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import com.example.fansauchiwa.ui.StickerAsset
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Polymorphic
@Serializable
sealed interface Decoration {
    val id: String
    val offset: Offset
    val rotation: Float
    val scale: Float
    val color: Int


    @Serializable
    data class Text(
        val text: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset,
        override val rotation: Float,
        override val scale: Float,
        override val color: Int,
        val width: Int
    ) : Decoration

    @Serializable
    data class Sticker(
        val label: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset,
        override val rotation: Float,
        override val scale: Float,
        override val color: Int
    ) : Decoration {
        val resId = StickerAsset.entries.find { it.type == label }?.resId ?: 0
    }
}