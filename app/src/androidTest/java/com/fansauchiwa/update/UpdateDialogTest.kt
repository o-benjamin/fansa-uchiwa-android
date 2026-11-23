package com.fansauchiwa.update

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setDialog(type: UpdatePromptType, onUpdate: () -> Unit = {}, onDismiss: () -> Unit = {}) {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                UpdateDialog(type = type, onUpdate = onUpdate, onDismiss = onDismiss)
            }
        }
    }

    @Test
    fun updateDialog_min_showsMessageAndLaterClosesIt() {
        var dismissCount = 0
        setDialog(UpdatePromptType.MIN, onDismiss = { dismissCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_message)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_later)).performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun updateDialog_min_updateCallsOnUpdate() {
        var updateCount = 0
        setDialog(UpdatePromptType.MIN, onUpdate = { updateCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_update)).performClick()

        assertEquals(1, updateCount)
    }

    @Test
    fun updateDialog_forceFallback_showsCriticalMessageAndLaterClosesIt() {
        var dismissCount = 0
        setDialog(UpdatePromptType.FORCE_FALLBACK, onDismiss = { dismissCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_message_critical)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_later)).performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun updateDialog_force_hasNoLaterButtonAndIgnoresBack() {
        var dismissCount = 0
        setDialog(UpdatePromptType.FORCE, onDismiss = { dismissCount++ })

        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_message_critical)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_later)).assertDoesNotExist()
        Espresso.pressBackUnconditionally()
        composeTestRule.onNodeWithText(context.getString(R.string.update_dialog_update)).assertIsDisplayed()

        assertEquals(0, dismissCount)
    }
}
