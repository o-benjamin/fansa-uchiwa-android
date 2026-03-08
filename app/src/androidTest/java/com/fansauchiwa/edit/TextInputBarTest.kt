package com.fansauchiwa.edit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextInputBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun textInputBar_displaysInitialText() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextInputBar(
                    initialText = "Hello",
                    onTextChanged = {},
                    onDone = {},
                    onDismissBlocked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
    }

    @Test
    fun textInputBar_callsOnTextChanged_whenTextUpdated() {
        var updatedText = ""
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextInputBar(
                    initialText = "Hello",
                    onTextChanged = { updatedText = it },
                    onDone = {},
                    onDismissBlocked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hello").performTextClearance()
        composeTestRule.onNodeWithText("").performTextInput("World")
        assert(updatedText == "World") {
            "Expected 'World' but got '$updatedText'"
        }
    }

    @Test
    fun textInputBar_callsOnDone_whenDoneButtonClicked() {
        var isDone = false
        val doneLabel = getContext().getString(R.string.done)
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextInputBar(
                    initialText = "Hello",
                    onTextChanged = {},
                    onDone = { isDone = true },
                    onDismissBlocked = {}
                )
            }
        }

        composeTestRule.onNodeWithText(doneLabel).performClick()
        assert(isDone) { "Expected onDone to be called" }
    }

    @Test
    fun textInputBar_displaysEmptyText_whenInitialTextIsEmpty() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextInputBar(
                    initialText = "",
                    onTextChanged = {},
                    onDone = {},
                    onDismissBlocked = {}
                )
            }
        }

        // TextField should exist even with empty text
        composeTestRule.onNodeWithText("").assertExists()
    }
}

