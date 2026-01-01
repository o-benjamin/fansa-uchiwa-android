package com.example.fansauchiwa.data

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.fansauchiwa.edit.FontFamilies
import com.example.fansauchiwa.edit.FontFamiliesParceler
import com.example.fansauchiwa.ui.DecorationColors
import com.example.fansauchiwa.ui.StickerAsset
import kotlinx.parcelize.IgnoredOnParcel
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

    @Serializable(with = ColorSerializer::class)
    val color: Color

    @Serializable(with = ColorSerializer::class)
    val strokeColor: Color
    val strokeWidth: Float


    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    @TypeParceler<Color, ColorParceler>
    @TypeParceler<FontFamilies, FontFamiliesParceler>
    data class Text(
        val text: String = "テキストを入力",
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        @Serializable(with = ColorSerializer::class)
        override val color: Color = DecorationColors.WHITE.value,
        @Serializable(with = ColorSerializer::class)
        override val strokeColor: Color = DecorationColors.CYAN.value,
        override val strokeWidth: Float = 30f,
        val width: Int = FontWeight.W900.weight,
        val font: FontFamilies
    ) : Decoration

    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    @TypeParceler<Color, ColorParceler>
    data class Sticker(
        val label: String,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        @Serializable(with = ColorSerializer::class)
        override val color: Color = DecorationColors.CYAN.value,
        @Serializable(with = ColorSerializer::class)
        override val strokeColor: Color = DecorationColors.WHITE.value,
        override val strokeWidth: Float = 30f,
    ) : Decoration {
        @IgnoredOnParcel
        val resId = StickerAsset.entries.find { it.type == label }?.resId ?: 0
    }

    @Parcelize
    @Serializable
    @TypeParceler<Offset, OffsetParceler>
    @TypeParceler<Color, ColorParceler>
    data class Image(
        override val id: String,
        val imageId: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        @Serializable(with = ColorSerializer::class)
        override val color: Color = DecorationColors.WHITE.value,
        @Serializable(with = ColorSerializer::class)
        override val strokeColor: Color = DecorationColors.CYAN.value,
        override val strokeWidth: Float = 30f,
    ) : Decoration
}
