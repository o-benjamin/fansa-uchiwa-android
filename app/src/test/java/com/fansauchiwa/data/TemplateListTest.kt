package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateListTest {

    @Test
    fun applyTemplateMainColor_updatesTextColorsOnly() {
        val selectedMainColor = DecorationColors.GREEN.value
        val savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    id = "text-1",
                    text = "推し",
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.PURPLE.value,
                    strokeWidth = 12f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0f,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "sticker-1",
                    color = DecorationColors.BLUE.value,
                    strokeColor = DecorationColors.PURPLE.value,
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

        val styled = savedUchiwa.applyTemplateMainColor(selectedMainColor)
        val text = styled.decorations.filterIsInstance<Decoration.Text>().single()
        val sticker = styled.decorations.filterIsInstance<Decoration.Sticker>().single()
        val image = styled.decorations.filterIsInstance<Decoration.Image>().single()

        assertEquals(selectedMainColor, text.color)
        assertEquals(DecorationColors.PURPLE.value, text.strokeColor)
        assertEquals(DecorationColors.WHITE.value, text.secondBorderColor)
        assertEquals(0f, text.secondBorderWidth)
        assertEquals(DecorationColors.BLUE.value, sticker.color)
        assertEquals(DecorationColors.PURPLE.value, sticker.strokeColor)
        assertEquals(DecorationColors.WHITE.value, sticker.secondStrokeColor)
        assertEquals(0f, sticker.secondStrokeWidth)
        assertEquals(Color.White, image.color)
    }
}
