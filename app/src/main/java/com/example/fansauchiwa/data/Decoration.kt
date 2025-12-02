package com.example.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import com.example.fansauchiwa.edit.FontFamilies
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
    val strokeColor: Int
    val strokeWidth: Float


    @Serializable
    data class Text(
        val text: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset,
        override val rotation: Float,
        override val scale: Float,
        override val color: Int,
        override val strokeColor: Int,
        override val strokeWidth: Float,
        val width: Int,
        val font: FontFamilies
    ) : Decoration

    @Serializable
    data class Sticker(
        val label: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset,
        override val rotation: Float,
        override val scale: Float,
        override val color: Int,
        override val strokeColor: Int,
        override val strokeWidth: Float
    ) : Decoration {
        val resId = StickerAsset.entries.find { it.type == label }?.resId ?: 0
    }
}