package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateListTest {

    @Test
    fun applyTemplateMainColor_updatesOverallBorderColorAndSpecificTextColors() {
        val selectedMainColor = DecorationColors.GREEN.value
        val savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    id = "text-name",
                    text = "名",
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.PURPLE.value,
                    strokeWidth = 12f,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    id = "text-front",
                    text = "前",
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.PURPLE.value,
                    strokeWidth = 12f,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    id = "text-other",
                    text = "くん",
                    color = Color.White,
                    strokeColor = Color.Black,
                    strokeWidth = 12f,
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
                )
            ),
            uchiwaColor = Color.Yellow,
            backgroundColor = Color.Black,
            overallBorderColor = Color.White
        )

        val styled = savedUchiwa.applyTemplateMainColor(selectedMainColor)
        val nameText = styled.decorations.find { (it as? Decoration.Text)?.text == "名" } as Decoration.Text
        val frontText = styled.decorations.find { (it as? Decoration.Text)?.text == "前" } as Decoration.Text
        val otherText = styled.decorations.find { (it as? Decoration.Text)?.text == "くん" } as Decoration.Text
        val sticker = styled.decorations.filterIsInstance<Decoration.Sticker>().single()

        assertEquals(selectedMainColor, styled.overallBorderColor)
        assertEquals(selectedMainColor, nameText.color)
        assertEquals(selectedMainColor, frontText.color)
        assertEquals(Color.White, otherText.color)
        assertEquals(DecorationColors.BLUE.value, sticker.color)
    }
}
