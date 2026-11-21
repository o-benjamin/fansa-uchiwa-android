package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.StickerAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        val styled = savedUchiwa.applyTemplateMainColor(
            mainColor = selectedMainColor,
            isNameInputPlaceholderEnabled = true
        )
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

    // 名前を間違えたステッカーは例外を出さず描かれないので、ここで止める
    @Test
    fun templateList_stickers_existInStickerAssets() {
        val knownLabels = StickerAsset.entries.map { it.type }
        val labels = templateList
            .flatMap { it.savedUchiwa.decorations }
            .filterIsInstance<Decoration.Sticker>()
            .map { it.label }

        assertTrue(knownLabels.containsAll(labels))
    }

    @Test
    fun templateList_ids_areUnique() {
        val ids = templateList.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun templateList_decorationIds_areUniqueWithinTemplate() {
        templateList.forEach { template ->
            val ids = template.savedUchiwa.decorations.map { it.id }

            assertEquals(template.id, ids.size, ids.toSet().size)
        }
    }
}
