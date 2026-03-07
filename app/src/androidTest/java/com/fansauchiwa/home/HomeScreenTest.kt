package com.fansauchiwa.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
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
                    decorations = emptyList(),
                    uchiwaColor = Color.White,
                    backgroundColor = Color.White
                )
            )
        }
    }

    private val sampleMasterpieces = (1..6).map { "masterpiece_$it" }

    @Test
    fun templatesAndMasterpieces_displaysBothSections() {
        composeTestRule.setContent {
            HomeScreenContent(
                masterpiecePathList = sampleMasterpieces,
                templates = previewTemplates(),
                isSelectionMode = false,
                selectedPaths = emptyList(),
                onImageClick = {},
                onTemplateClick = {},
                onImageLongPress = {},
                statusBarPadding = 0.dp,
                isPreview = true
            )
        }

        val context = getContext()
        val templateSectionTitle = context.getString(R.string.template_section_title)
        val myDesignSectionTitle = context.getString(R.string.my_design_section_title)

        composeTestRule.onNodeWithText(templateSectionTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(myDesignSectionTitle).assertIsDisplayed()

        composeTestRule.onNodeWithText("template_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("masterpiece_1").assertIsDisplayed()
    }

    @Test
    fun templatesOnly_displaysTemplateSectionAndEmptyMessage() {
        composeTestRule.setContent {
            HomeScreenContent(
                masterpiecePathList = emptyList(),
                templates = previewTemplates(),
                isSelectionMode = false,
                selectedPaths = emptyList(),
                onImageClick = {},
                onTemplateClick = {},
                onImageLongPress = {},
                statusBarPadding = 0.dp,
                isPreview = true
            )
        }

        val context = getContext()
        val templateSectionTitle = context.getString(R.string.template_section_title)
        val myDesignSectionTitle = context.getString(R.string.my_design_section_title)
        val emptyMessageTitle = context.getString(R.string.empty_masterpiece_title)

        composeTestRule.onNodeWithText(templateSectionTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("template_1").assertIsDisplayed()

        composeTestRule.onNodeWithText(myDesignSectionTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(emptyMessageTitle).assertIsDisplayed()
    }

    @Test
    fun masterpiecesOnly_displaysMyDesignSection() {
        composeTestRule.setContent {
            HomeScreenContent(
                masterpiecePathList = sampleMasterpieces,
                templates = emptyList(),
                isSelectionMode = false,
                selectedPaths = emptyList(),
                onImageClick = {},
                onTemplateClick = {},
                onImageLongPress = {},
                statusBarPadding = 0.dp,
                isPreview = true
            )
        }

        val context = getContext()
        val templateSectionTitle = context.getString(R.string.template_section_title)
        val myDesignSectionTitle = context.getString(R.string.my_design_section_title)

        composeTestRule.onNodeWithText(templateSectionTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText("template_1").assertDoesNotExist()

        composeTestRule.onNodeWithText(myDesignSectionTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("masterpiece_1").assertIsDisplayed()
    }

    @Test
    fun empty_displaysEmptyMessageOnly() {
        composeTestRule.setContent {
            HomeScreenContent(
                masterpiecePathList = emptyList(),
                templates = emptyList(),
                isSelectionMode = false,
                selectedPaths = emptyList(),
                onImageClick = {},
                onTemplateClick = {},
                onImageLongPress = {},
                statusBarPadding = 0.dp,
                isPreview = true
            )
        }

        val context = getContext()
        val templateSectionTitle = context.getString(R.string.template_section_title)
        val myDesignSectionTitle = context.getString(R.string.my_design_section_title)
        val emptyMessageTitle = context.getString(R.string.empty_masterpiece_title)

        composeTestRule.onNodeWithText(templateSectionTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(myDesignSectionTitle).assertDoesNotExist()

        composeTestRule.onNodeWithText(emptyMessageTitle).assertIsDisplayed()
    }
}
