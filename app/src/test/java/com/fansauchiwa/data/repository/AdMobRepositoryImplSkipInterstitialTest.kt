package com.fansauchiwa.data.repository

import android.app.Activity
import android.content.Context
import com.fansauchiwa.analytics.AnalyticsDataSource
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdMobRepositoryImplSkipInterstitialTest {

    private val analyticsDataSource = mockk<AnalyticsDataSource>()
    private val activity = mockk<Activity>()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showInterstitialAd_afterSkipNext_closesImmediatelyWithoutShowing() {
        val repository = AdMobRepositoryImpl(mockk<Context>(), analyticsDataSource)
        var closedCount = 0

        repository.skipNextInterstitialAd()
        repository.showInterstitialAd(activity, placement = "image_preview_screen") { closedCount++ }

        assertEquals(1, closedCount)
        confirmVerified(analyticsDataSource, activity)
    }
}
