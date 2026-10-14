package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AllTextStyleTest {

    private fun text(
        id: String,
        font: FontFamilies = FontFamilies.HACHI_MARU_POP,
        color: Color = Color.Black,
        strokeColor: Color = Color.White,
        strokeWidth: Float = 20f,
        secondBorderColor: Color = Color.Magenta,
        secondBorderWidth: Float = 10f
    ) = Decoration.Text(
        id = id,
        text = "テスト",
        font = font,
        color = color,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        secondBorderColor = secondBorderColor,
        secondBorderWidth = secondBorderWidth
    )

    @Test
    fun of_emptyDecorations_returnsNull() {
        assertNull(AllTextStyle.of(emptyList()))
    }

    @Test
    fun of_onlyStickerAndImage_returnsNull() {
        val decorations = listOf(
            Decoration.Sticker(id = "sticker-1", label = "heart"),
            Decoration.Image(id = "image-decoration-1", imageId = "image-1")
        )

        assertNull(AllTextStyle.of(decorations))
    }

    @Test
    fun of_singleText_returnsItsValues() {
        val style = AllTextStyle.of(
            listOf(text("text-1", FontFamilies.NOTO_SANS_JP, Color.Red, Color.Blue, 30f, Color.Green, 15f))
        )

        assertEquals(
            AllTextStyle(
                font = FontFamilies.NOTO_SANS_JP,
                color = Color.Red,
                strokeColor = Color.Blue,
                strokeWidth = 30f,
                secondBorderColor = Color.Green,
                secondBorderWidth = 15f
            ),
            style
        )
    }

    @Test
    fun of_allTextsSame_returnsCommonValues() {
        val style = AllTextStyle.of(listOf(text("text-1"), text("text-2"), text("text-3")))

        assertEquals(FontFamilies.HACHI_MARU_POP, style?.font)
        assertEquals(Color.Black, style?.color)
        assertEquals(Color.White, style?.strokeColor)
        assertEquals(20f, style?.strokeWidth)
        assertEquals(Color.Magenta, style?.secondBorderColor)
        assertEquals(10f, style?.secondBorderWidth)
    }

    @Test
    fun of_onlyFontDiffers_fontIsNullAndOthersKept() {
        val style = AllTextStyle.of(
            listOf(text("text-1"), text("text-2", font = FontFamilies.NOTO_SANS_JP))
        )

        assertNull(style?.font)
        assertEquals(Color.Black, style?.color)
        assertEquals(Color.White, style?.strokeColor)
        assertEquals(20f, style?.strokeWidth)
    }

    @Test
    fun of_colorAndStrokeDiffer_thoseAreNull() {
        val style = AllTextStyle.of(
            listOf(
                text("text-1"),
                text("text-2", color = Color.Red, strokeColor = Color.Blue, strokeWidth = 0f)
            )
        )

        assertEquals(FontFamilies.HACHI_MARU_POP, style?.font)
        assertNull(style?.color)
        assertNull(style?.strokeColor)
        assertNull(style?.strokeWidth)
    }

    @Test
    fun of_onlySecondBorderDiffers_secondBorderIsNullAndFirstBorderKept() {
        val style = AllTextStyle.of(
            listOf(
                text("text-1"),
                text("text-2", secondBorderColor = Color.Yellow, secondBorderWidth = 0f)
            )
        )

        assertEquals(Color.White, style?.strokeColor)
        assertEquals(20f, style?.strokeWidth)
        assertNull(style?.secondBorderColor)
        assertNull(style?.secondBorderWidth)
    }

    @Test
    fun of_stickerHasDifferentColor_ignoresSticker() {
        val style = AllTextStyle.of(
            listOf(
                text("text-1"),
                Decoration.Sticker(id = "sticker-1", label = "heart", color = Color.Red)
            )
        )

        assertEquals(Color.Black, style?.color)
    }
}
