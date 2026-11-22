package com.fansauchiwa.analytics

import android.content.ComponentName
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ShareTargetReceiver.callbackIntent] に載せた extras が、システムが選ばれたアプリを書き足したあとも
 * [ShareTargetReceiver.paramsFrom] で読めることを確かめる（Intent はユニットテストでは作れないため、ここで見る）
 */
@RunWith(AndroidJUnit4::class)
class ShareTargetReceiverTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun Intent.withChosenPackage(packageName: String): Intent =
        putExtra(Intent.EXTRA_CHOSEN_COMPONENT, ComponentName(packageName, "$packageName.ShareActivity"))

    @Test
    fun paramsFrom_ChosenFromDailyPromptUchiwa_ReturnsTargetEntryPointAndSource() {
        val intent = ShareTargetReceiver
            .callbackIntent(context, ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE, isStartedFromDailyPrompt = true)
            .withChosenPackage("com.twitter.android")

        assertEquals(
            mapOf(
                ShareAnalyticsParams.PARAM_TARGET to "com.twitter.android",
                ShareAnalyticsParams.PARAM_ENTRY_POINT to ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE,
                DailyPromptAnalyticsParams.PARAM_SOURCE to DailyPromptAnalyticsParams.SOURCE_DAILY_PROMPT
            ),
            ShareTargetReceiver.paramsFrom(intent)
        )
    }

    @Test
    fun paramsFrom_ChosenFromOtherUchiwa_ReturnsWithoutSource() {
        val intent = ShareTargetReceiver
            .callbackIntent(context, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON, isStartedFromDailyPrompt = false)
            .withChosenPackage("jp.naver.line.android")

        assertEquals(
            mapOf(
                ShareAnalyticsParams.PARAM_TARGET to "jp.naver.line.android",
                ShareAnalyticsParams.PARAM_ENTRY_POINT to ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON
            ),
            ShareTargetReceiver.paramsFrom(intent)
        )
    }

    @Test
    fun paramsFrom_NoChosenComponent_ReturnsParamsWithoutTarget() {
        val intent = ShareTargetReceiver
            .callbackIntent(context, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON, isStartedFromDailyPrompt = false)

        assertEquals(
            mapOf(ShareAnalyticsParams.PARAM_ENTRY_POINT to ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON),
            ShareTargetReceiver.paramsFrom(intent)
        )
    }
}
