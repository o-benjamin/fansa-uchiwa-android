package com.fansauchiwa.home

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenLogicTest {

    @Test
    fun splitFirstNameForNameTemplate_returnsEmptyPartsForEmptyName() {
        assertEquals("" to "", splitFirstNameForNameTemplate(""))
    }

    @Test
    fun splitFirstNameForNameTemplate_returnsSingleCharacterAndEmptyTail() {
        assertEquals("潤" to "", splitFirstNameForNameTemplate("潤"))
    }

    @Test
    fun splitFirstNameForNameTemplate_splitsFirstCharacterAndRemainingCharacters() {
        assertEquals("勝" to "利", splitFirstNameForNameTemplate("勝利"))
    }

    @Test
    fun hasNameInputPlaceholder_returnsTrueOnlyForTemplatesWithNamePlaceholder() {
        val namedTemplate = Template(
            id = "named",
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "〇〇くん",
                        id = "name-placeholder",
                        font = FontFamilies.M_PLUS_ROUNDED_1C
                    )
                ),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )
        )
        val regularTemplate = Template(
            id = "regular",
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "プロポーズ",
                        id = "fixed-text",
                        font = FontFamilies.M_PLUS_ROUNDED_1C
                    )
                ),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )
        )

        assertTrue(namedTemplate.hasNameInputPlaceholder())
        assertFalse(regularTemplate.hasNameInputPlaceholder())
    }
}
