package com.fansauchiwa.data

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.FontFamiliesParceler
import com.fansauchiwa.ui.StickerAsset
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

const val DEFAULT_DECORATION_TEXT = "テキストを入力"

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
        val text: String = DEFAULT_DECORATION_TEXT,
        override val id: String,
        @Serializable(with = OffsetSerializer::class)
        override val offset: Offset = Offset.Zero,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        @Serializable(with = ColorSerializer::class)
        override val color: Color = DecorationColors.WHITE.value,
        @Serializable(with = ColorSerializer::class)
        override val strokeColor: Color = DecorationColors.MAGENTA.value,
        override val strokeWidth: Float = 30f,
        @Serializable(with = ColorSerializer::class)
        val secondBorderColor: Color = DecorationColors.WHITE.value,
        val secondBorderWidth: Float = 0f,
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
        override val color: Color = DecorationColors.MAGENTA.value,
        @Serializable(with = ColorSerializer::class)
        override val strokeColor: Color = DecorationColors.WHITE.value,
        override val strokeWidth: Float = 3f,
        @Serializable(with = ColorSerializer::class)
        val secondStrokeColor: Color = DecorationColors.WHITE.value,
        val secondStrokeWidth: Float = 0f,
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
