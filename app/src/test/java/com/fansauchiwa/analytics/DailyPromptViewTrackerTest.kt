package com.fansauchiwa.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyPromptViewTrackerTest {

    @Test
    fun viewEventIfFirstShown_firstTime_returnsViewEventWithPromptId() {
        val tracker = DailyPromptViewTracker()

        val event = tracker.viewEventIfFirstShown("20261104")

        assertEquals(
            AnalyticsEvent(
                AnalyticsActions.VIEW_DAILY_PROMPT,
                mapOf(DailyPromptAnalyticsParams.PARAM_PROMPT_ID to "20261104")
            ),
            event
        )
    }

    @Test
    fun viewEventIfFirstShown_samePromptAgain_returnsNull() {
        val tracker = DailyPromptViewTracker()
        tracker.viewEventIfFirstShown("20261104")

        assertNull(tracker.viewEventIfFirstShown("20261104"))
    }
}
