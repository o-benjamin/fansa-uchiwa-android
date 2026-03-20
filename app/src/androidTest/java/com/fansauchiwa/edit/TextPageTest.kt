package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.pager.TextPage
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContentWithSelectedFont(selectedFont: FontFamilies) {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextPage(
                    onTextClick = {},
                    onFontChanged = {},
                    onColorSelected = {},
                    onTextWeightChanged = {},
                    onStrokeColorSelected = {},
                    onStrokeWeightChanged = {},
                    onSecondBorderColorSelected = {},
                    onSecondBorderWeightChanged = {},
                    selectedDecoration = Decoration.Text(
                        id = "test-id",
                        font = selectedFont,
                        text = "テスト",
                        color = Color.Black,
                        strokeColor = Color.White,
                        width = 700,
                        strokeWidth = 2.5f
                    )
                )
            }
        }
    }

    @Test
    fun fontButton_selectedFontIsHachiMaruPop_isSelected() {
        setContentWithSelectedFont(FontFamilies.HACHI_MARU_POP)

        composeTestRule
            .onNode(hasTestTag("font_button_${FontFamilies.HACHI_MARU_POP.name}"))
            .assertIsSelected()
    }

    @Test
    fun fontButton_notoSansJpWhenHachiMaruPopSelected_isNotSelected() {
        setContentWithSelectedFont(FontFamilies.HACHI_MARU_POP)

        composeTestRule
            .onNode(hasTestTag("font_button_${FontFamilies.NOTO_SANS_JP.name}"))
            .assertIsNotSelected()
    }

    @Test
    fun fontButton_allOtherFontsWhenHachiMaruPopSelected_areNotSelected() {
        setContentWithSelectedFont(FontFamilies.HACHI_MARU_POP)

        FontFamilies.entries
            .filter { it != FontFamilies.HACHI_MARU_POP }
            .forEach { fontFamily ->
                composeTestRule
                    .onNode(hasTestTag("font_button_${fontFamily.name}"))
                    .assertIsNotSelected()
            }
    }
}

