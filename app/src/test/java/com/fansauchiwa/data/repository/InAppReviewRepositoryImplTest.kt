package com.fansauchiwa.data.repository

import android.app.Activity
import android.util.Log
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.infra.AppInstallDataSource
import com.fansauchiwa.data.infra.InAppReviewDataSource
import com.fansauchiwa.data.infra.InAppReviewHistoryDataSource
import com.google.android.play.core.review.ReviewInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class InAppReviewRepositoryImplTest {

    private lateinit var historyDataSource: InAppReviewHistoryDataSource
    private lateinit var appInstallDataSource: AppInstallDataSource
    private lateinit var inAppReviewDataSource: InAppReviewDataSource
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var repository: InAppReviewRepositoryImpl
    private val activity = mockk<Activity>()
    private val reviewInfo = mockk<ReviewInfo>()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0

        historyDataSource = mockk(relaxed = true)
        appInstallDataSource = mockk()
        inAppReviewDataSource = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        repository = InAppReviewRepositoryImpl(
            historyDataSource,
            appInstallDataSource,
            inAppReviewDataSource,
            analyticsRepository,
            crashReportingRepository
        )

        every { appInstallDataSource.getFirstInstallTimeMillisStream() } returns
            flowOf(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(100))
        every { historyDataSource.getLastRequestedAtMillisStream() } returns flowOf(null)
        coEvery { inAppReviewDataSource.requestReviewInfo() } returns reviewInfo
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun requestReviewIfEligible_eligible_recordsAttemptThenLaunchesFlow() = runTest {
        every { historyDataSource.getSaveSuccessCountStream() } returns flowOf(2)

        repository.requestReviewIfEligible(activity)

        coVerifyOrder {
            inAppReviewDataSource.requestReviewInfo()
            historyDataSource.setLastRequestedAtMillis(any())
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.IN_APP_REVIEW_REQUEST))
            inAppReviewDataSource.launchReviewFlow(activity, reviewInfo)
        }
    }

    @Test
    fun requestReviewIfEligible_requestReviewInfoFails_doesNotRecordAttempt() = runTest {
        every { historyDataSource.getSaveSuccessCountStream() } returns flowOf(2)
        coEvery { inAppReviewDataSource.requestReviewInfo() } throws
            IllegalStateException("Play Store unavailable")

        repository.requestReviewIfEligible(activity)

        // 依頼の準備で止まった場合は「試みた」ことにせず、次の保存でもう一度試す
        coVerify(exactly = 0) { historyDataSource.setLastRequestedAtMillis(any()) }
        coVerify(exactly = 0) { inAppReviewDataSource.launchReviewFlow(any(), any()) }
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun requestReviewIfEligible_firstSave_doesNothing() = runTest {
        every { historyDataSource.getSaveSuccessCountStream() } returns flowOf(1)

        repository.requestReviewIfEligible(activity)

        coVerify(exactly = 0) { historyDataSource.setLastRequestedAtMillis(any()) }
        coVerify(exactly = 0) { inAppReviewDataSource.requestReviewInfo() }
    }

    @Test
    fun requestReviewIfEligible_launchFails_doesNotThrowAndKeepsAttemptRecorded() = runTest {
        every { historyDataSource.getSaveSuccessCountStream() } returns flowOf(3)
        coEvery { inAppReviewDataSource.launchReviewFlow(activity, reviewInfo) } throws
            IllegalStateException("Play Store unavailable")

        repository.requestReviewIfEligible(activity)

        coVerify(exactly = 1) { historyDataSource.setLastRequestedAtMillis(any()) }
    }

    @Test
    fun recordSaveSuccess_dataStoreFails_doesNotThrow() = runTest {
        coEvery { historyDataSource.incrementSaveSuccessCount() } throws
            IOException("disk full")

        repository.recordSaveSuccess()

        coVerify(exactly = 1) { historyDataSource.incrementSaveSuccessCount() }
        // 記録に失敗すると依頼がずっと出なくなるため、Crashlytics に残す
        verify(exactly = 1) { crashReportingRepository.recordException(any<IOException>()) }
    }
}
