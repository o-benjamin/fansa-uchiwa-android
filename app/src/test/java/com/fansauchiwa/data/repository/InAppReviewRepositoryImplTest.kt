package com.fansauchiwa.data.repository

import android.app.Activity
import android.util.Log
import com.fansauchiwa.data.infra.AppInstallDataSource
import com.fansauchiwa.data.infra.InAppReviewDataSource
import com.fansauchiwa.data.infra.InAppReviewHistoryDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
    private lateinit var repository: InAppReviewRepositoryImpl
    private val activity = mockk<Activity>()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0

        historyDataSource = mockk(relaxed = true)
        appInstallDataSource = mockk()
        inAppReviewDataSource = mockk(relaxed = true)
        repository = InAppReviewRepositoryImpl(
            historyDataSource,
            appInstallDataSource,
            inAppReviewDataSource
        )

        every { appInstallDataSource.getFirstInstallTimeMillisStream() } returns
            flowOf(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(100))
        coEvery { historyDataSource.getLastRequestedAtMillis() } returns null
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun requestReviewIfEligible_eligible_recordsAttemptThenLaunchesFlow() = runTest {
        coEvery { historyDataSource.getSaveSuccessCount() } returns 2

        repository.requestReviewIfEligible(activity)

        coVerify(exactly = 1) { historyDataSource.setLastRequestedAtMillis(any()) }
        coVerify(exactly = 1) { inAppReviewDataSource.launchReviewFlow(activity) }
    }

    @Test
    fun requestReviewIfEligible_firstSave_doesNothing() = runTest {
        coEvery { historyDataSource.getSaveSuccessCount() } returns 1

        repository.requestReviewIfEligible(activity)

        coVerify(exactly = 0) { historyDataSource.setLastRequestedAtMillis(any()) }
        coVerify(exactly = 0) { inAppReviewDataSource.launchReviewFlow(any()) }
    }

    @Test
    fun requestReviewIfEligible_launchFails_doesNotThrowAndKeepsAttemptRecorded() = runTest {
        coEvery { historyDataSource.getSaveSuccessCount() } returns 3
        coEvery { inAppReviewDataSource.launchReviewFlow(activity) } throws
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
    }
}
