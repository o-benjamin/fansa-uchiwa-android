package com.fansauchiwa.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.edit.FontFamilies
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun previewTemplates(): List<Template> {
        return (1..3).map { index ->
            Template(
                id = "template_$index",
                previewImageResId = R.drawable.uchiwa_shape,
                savedUchiwa = SavedUchiwa(
                    decorations = listOf(
                        Decoration.Text(
                            id = "preview_text_$index",
                            text = "サンプル$index",
                            color = Color.White,
                            strokeColor = Color(0xFF65E8FF),
                            strokeWidth = 24f,
                            font = FontFamilies.M_PLUS_ROUNDED_1C
                        )
                    ),
                    uchiwaColor = Color(0xFFF6D6FF),
                    backgroundColor = Color(0x11000000)
                )
            )
        }
    }

    private val sampleMasterpieces = (1..6).map { "masterpiece_$it" }

    @Test
    fun homeNavigationBar_switchesVisibleContentByTab() {
        val templates = previewTemplates()
        composeTestRule.setContent {
            val selectedTabState = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(HomeTab.HOME)
            }

            androidx.compose.foundation.layout.Column {
                HomeNavigationBar(
                    selectedTab = selectedTabState.value,
                    onTabSelected = { selectedTabState.value = it }
                )
                val selectedMemberColorState = androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(DecorationColors.PINK)
                }

                HomeTabContent(
                    selectedTab = selectedTabState.value,
                    masterpiecePathList = sampleMasterpieces,
                    templates = templates,
                    selectedMainColor = selectedMemberColorState.value,
                    onMainColorSelected = { selectedMemberColorState.value = it },
                    isSelectionMode = false,
                    selectedPaths = emptyList(),
                    onImageClick = {},
                    onTemplateClick = {},
                    onImageLongPress = {},
                    statusBarPadding = 0.dp,
                    isPreview = true
                )
            }
        }

        val context = getContext()
        val homeTabLabel = context.getString(R.string.home)
        val myDesignTabLabel = context.getString(R.string.my_design)
        val templateSectionTitle = context.getString(R.string.template_section_title)
        val myDesignSectionTitle = context.getString(R.string.my_design_section_title)

        composeTestRule.onNodeWithText(homeTabLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(myDesignTabLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(templateSectionTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag("template-preview-template_1").assertIsDisplayed()
        composeTestRule.onNodeWithText(myDesignSectionTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText("masterpiece_1").assertDoesNotExist()

        composeTestRule.onNodeWithText(myDesignTabLabel).performClick()

        composeTestRule.onNodeWithText(templateSectionTitle).assertDoesNotExist()
        composeTestRule.onNodeWithTag("template-preview-template_1").assertDoesNotExist()
        composeTestRule.onNodeWithText(myDesignSectionTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("masterpiece_1").assertIsDisplayed()
    }

    @Test
    fun createTabColorSelector_updatesSelectedChipState() {
        val templates = previewTemplates()
        val targetColor = DecorationColors.BLUE

        composeTestRule.setContent {
            val selectedMemberColorState = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(DecorationColors.PINK)
            }

            HomeTabContent(
                selectedTab = HomeTab.HOME,
                masterpiecePathList = emptyList(),
                templates = templates,
                selectedMainColor = selectedMemberColorState.value,
                onMainColorSelected = { selectedMemberColorState.value = it },
                isSelectionMode = false,
                selectedPaths = emptyList(),
                onImageClick = {},
                onTemplateClick = {},
                onImageLongPress = {},
                statusBarPadding = 0.dp,
                isPreview = true
            )
        }

        composeTestRule.onNodeWithTag(mainColorChipTag(targetColor.value)).performClick()
        composeTestRule.onNodeWithTag(mainColorChipTag(targetColor.value)).assertIsSelected()
    }

}
