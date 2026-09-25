package com.fansauchiwa.home.featuredoor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.featuredoor.FeatureDoor
import com.fansauchiwa.analytics.featuredoor.FeatureDoorAnalytics
import com.fansauchiwa.analytics.featuredoor.FeatureDoorAnswer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeatureDoorSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun featureDoorSection_tapDoorAndAnswer_sendsEventsAndClosesDialog() {
        val events = mutableListOf<AnalyticsEvent>()
        composeTestRule.setContent {
            FeatureDoorSection(doors = FeatureDoor.entries, onEvent = { events += it })
        }
        val dialogTitle = context.getString(R.string.feature_door_dialog_title)

        FeatureDoor.entries.forEach {
            composeTestRule.onNodeWithText(context.getString(it.titleResId)).assertIsDisplayed()
        }
        composeTestRule.onNodeWithText(context.getString(FeatureDoor.COUNTDOWN.titleResId)).performClick()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.feature_door_answer_daily)).performClick()

        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
        assertEquals(
            listOf(
                FeatureDoorAnalytics.tapEvent(FeatureDoor.COUNTDOWN),
                FeatureDoorAnalytics.answerEvent(FeatureDoor.COUNTDOWN, FeatureDoorAnswer.DAILY)
            ),
            events
        )
    }
}
