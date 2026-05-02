package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollToNode
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
                    onPuffyEnabledChanged = {},
                    selectedDecoration = Decoration.Text(
                        id = "test_id",
                        font = selectedFont,
                        color = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 2f,
                        width = 400
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
                    .onNode(hasTestTag("font_family_grid"))
                    .performScrollToNode(hasTestTag("font_button_${fontFamily.name}"))
                composeTestRule
                    .onNode(hasTestTag("font_button_${fontFamily.name}"))
                    .assertIsNotSelected()
            }
    }
}
