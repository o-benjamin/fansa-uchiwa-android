package com.fansauchiwa.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareAnalyticsParamsTest {

    @Test
    fun selectShareTargetParams_PackageAndEntryPoint_ReturnsTargetAndEntryPointOnly() {
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = "com.twitter.android",
            entryPoint = ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON,
            isStartedFromDailyPrompt = false
        )

        assertEquals(
            mapOf(
                ShareAnalyticsParams.PARAM_TARGET to "com.twitter.android",
                ShareAnalyticsParams.PARAM_ENTRY_POINT to ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON
            ),
            params
        )
    }

    @Test
    fun selectShareTargetParams_StartedFromDailyPrompt_AddsSourceDailyPrompt() {
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = "jp.naver.line.android",
            entryPoint = ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE,
            isStartedFromDailyPrompt = true
        )

        assertEquals(
            mapOf(
                ShareAnalyticsParams.PARAM_TARGET to "jp.naver.line.android",
                ShareAnalyticsParams.PARAM_ENTRY_POINT to ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE,
                DailyPromptAnalyticsParams.PARAM_SOURCE to DailyPromptAnalyticsParams.SOURCE_DAILY_PROMPT
            ),
            params
        )
    }

    @Test
    fun selectShareTargetParams_NoEntryPoint_ReturnsTargetWithoutEntryPoint() {
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = "com.twitter.android",
            entryPoint = null,
            isStartedFromDailyPrompt = false
        )

        assertEquals(mapOf(ShareAnalyticsParams.PARAM_TARGET to "com.twitter.android"), params)
    }

    @Test
    fun selectShareTargetParams_NoChosenPackage_ReturnsNull() {
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = null,
            entryPoint = ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON,
            isStartedFromDailyPrompt = true
        )

        assertNull(params)
    }

    @Test
    fun selectShareTargetParams_BlankChosenPackage_ReturnsNull() {
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = " ",
            entryPoint = ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON,
            isStartedFromDailyPrompt = false
        )

        assertNull(params)
    }
}
