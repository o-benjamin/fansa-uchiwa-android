package com.fansauchiwa.analytics.featuredoor

import com.fansauchiwa.analytics.AnalyticsActions
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureDoorAnalyticsTest {

    @Test
    fun tapEvent_countdown_sendsCountdown() {
        val event = FeatureDoorAnalytics.tapEvent(FeatureDoor.COUNTDOWN)

        assertEquals(AnalyticsActions.TAP_FEATURE_DOOR, event.name)
        assertEquals(mapOf(FeatureDoorAnalytics.PARAM_FEATURE_DOOR to "countdown"), event.params)
    }

    @Test
    fun tapEvent_eachDoor_sendsItsOwnValue() {
        val values = FeatureDoor.entries.map {
            FeatureDoorAnalytics.tapEvent(it).params[FeatureDoorAnalytics.PARAM_FEATURE_DOOR]
        }

        assertEquals(listOf("countdown", "daily_prompt", "anniversary"), values)
    }

    @Test
    fun answerEvent_dailyPromptAndDaily_sendsFeatureAndAnswer() {
        val event = FeatureDoorAnalytics.answerEvent(FeatureDoor.DAILY_PROMPT, FeatureDoorAnswer.DAILY)

        assertEquals(AnalyticsActions.ANSWER_FEATURE_DOOR, event.name)
        assertEquals(
            mapOf(
                FeatureDoorAnalytics.PARAM_FEATURE_DOOR to "daily_prompt",
                FeatureDoorAnalytics.PARAM_FEATURE_DOOR_ANSWER to "daily"
            ),
            event.params
        )
    }

    @Test
    fun answerEvent_eachAnswer_sendsItsOwnValue() {
        val values = FeatureDoorAnswer.entries.map {
            FeatureDoorAnalytics.answerEvent(FeatureDoor.ANNIVERSARY, it)
                .params[FeatureDoorAnalytics.PARAM_FEATURE_DOOR_ANSWER]
        }

        assertEquals(listOf("daily", "sometimes", "not_needed"), values)
    }
}
