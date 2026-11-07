package com.fansauchiwa.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyPromptCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val prompt = DailyPrompt(id = "default_peace", text = "ピースして")

    private fun setCard(
        onShown: () -> Unit = {},
        onCreate: () -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                DailyPromptCard(
                    prompt = prompt,
                    onShown = onShown,
                    onCreate = onCreate,
                    onDismiss = onDismiss
                )
            }
        }
    }

    @Test
    fun dailyPromptCard_shown_showsPromptTextAndCallsOnShownOnce() {
        var shownCount = 0
        setCard(onShown = { shownCount++ })

        composeTestRule.onNodeWithText(prompt.text).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(1, shownCount)
        }
    }

    @Test
    fun dailyPromptCard_helpTapped_showsHelpUntilClosed() {
        setCard()

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.daily_prompt_help_description))
            .performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.daily_prompt_help_message)).assertIsDisplayed()

        composeTestRule.onNodeWithText(context.getString(R.string.daily_prompt_help_close)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.daily_prompt_help_message)).assertDoesNotExist()
    }

    @Test
    fun dailyPromptCard_closeTapped_callsOnDismissOnce() {
        var dismissCount = 0
        setCard(onDismiss = { dismissCount++ })

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.daily_prompt_close_description))
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
        }
    }

    @Test
    fun dailyPromptCard_createTapped_callsOnCreateOnce() {
        var createCount = 0
        setCard(onCreate = { createCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.daily_prompt_create_button)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, createCount)
        }
    }
}
