package com.fansauchiwa.home

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
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
    fun shouldShowFirstNameRequiredError_returnsTrueWhenFirstNameIsBlank() {
        assertTrue(shouldShowFirstNameRequiredError(""))
        assertTrue(shouldShowFirstNameRequiredError(" "))
    }

    @Test
    fun shouldShowFirstNameRequiredError_returnsFalseWhenFirstNameIsPresent() {
        assertFalse(shouldShowFirstNameRequiredError("潤"))
    }

    @Test
    fun isNameInputPlaceholderEnabled_returnsConfiguredFlag() {
        val namedTemplate = Template(
            id = "named",
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = emptyList(),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            ),
            isNameInputPlaceholderEnabled = true
        )
        val regularTemplate = Template(
            id = "regular",
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = emptyList(),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )
        )

        assertTrue(namedTemplate.isNameInputPlaceholderEnabled)
        assertFalse(regularTemplate.isNameInputPlaceholderEnabled)
    }
}
