package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateListTest {

    @Test
    fun applyQuickTemplateStyle_updatesTemplateColorsAndFixedBorders() {
        val selectedMainColor = DecorationColors.GREEN.value
        val savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    id = "text-1",
                    text = "推し",
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.MAGENTA.value,
                    strokeWidth = 12f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0f,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "sticker-1",
                    color = DecorationColors.BLUE.value,
                    strokeColor = DecorationColors.MAGENTA.value,
                    strokeWidth = 3f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0f
                ),
                Decoration.Image(
                    id = "image-1",
                    imageId = "image-ref",
                    color = Color.White
                )
            ),
            uchiwaColor = Color.Yellow,
            backgroundColor = Color.Black
        )

        val styled = savedUchiwa.applyQuickTemplateStyle(selectedMainColor)
        val text = styled.decorations.filterIsInstance<Decoration.Text>().single()
        val sticker = styled.decorations.filterIsInstance<Decoration.Sticker>().single()
        val image = styled.decorations.filterIsInstance<Decoration.Image>().single()

        assertEquals(selectedMainColor, text.color)
        assertEquals(DecorationColors.WHITE.value, text.strokeColor)
        assertEquals(DecorationColors.BLACK.value, text.secondBorderColor)
        assertEquals(12f, text.secondBorderWidth)
        assertEquals(selectedMainColor, sticker.color)
        assertEquals(DecorationColors.WHITE.value, sticker.strokeColor)
        assertEquals(DecorationColors.BLACK.value, sticker.secondStrokeColor)
        assertEquals(3f, sticker.secondStrokeWidth)
        assertEquals(Color.White, image.color)
    }
}
