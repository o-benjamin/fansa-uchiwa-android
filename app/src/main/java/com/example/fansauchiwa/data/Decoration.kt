package com.example.fansauchiwa.data

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import com.example.fansauchiwa.R
import com.example.fansauchiwa.edit.FontFamilies
import com.example.fansauchiwa.edit.FontFamiliesParceler
import com.example.fansauchiwa.ui.StickerAsset
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Polymorphic
@Serializable
sealed interface Decoration : Parcelable {
    val id: String
    val offset: Offset
    val rotation: Float
    val scale: Float
    val color: Int
    val strokeColor: Int
    val strokeWidth: Float


    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    @TypeParceler<FontFamilies, FontFamiliesParceler>
    data class Text(
        val text: String = "テキストを入力",
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        override val color: Int = R.color.decoration_white,
        override val strokeColor: Int = R.color.decoration_black,
        override val strokeWidth: Float = 30f,
        val width: Int = FontWeight.W900.weight,
        val font: FontFamilies
    ) : Decoration

    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    data class Sticker(
        val label: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        override val color: Int = R.color.decoration_white,
        override val strokeColor: Int = R.color.decoration_black,
        override val strokeWidth: Float = 30f,
    ) : Decoration {
        val resId = StickerAsset.entries.find { it.type == label }?.resId ?: 0
    }

    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    data class Image(
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        override val color: Int = R.color.decoration_white,
        override val strokeColor: Int = R.color.decoration_black,
        override val strokeWidth: Float = 30f,
        ) : Decoration
}
