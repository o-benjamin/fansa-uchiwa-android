package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.infra.PushMessagingDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PushMessagingRepositoryImplTest {

    private val pushMessagingDataSource: PushMessagingDataSource = mockk(relaxed = true)
    private val crashReportingRepository: CrashReportingRepository = mockk(relaxed = true)
    private val repository = PushMessagingRepositoryImpl(pushMessagingDataSource, crashReportingRepository)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun subscribeToAnnouncements_called_subscribesToAnnouncementTopic() = runTest {
        repository.subscribeToAnnouncements()

        coVerify(exactly = 1) { pushMessagingDataSource.subscribeToTopic("all") }
    }

    @Test
    fun subscribeToAnnouncements_subscribeFails_doesNotThrow() = runTest {
        coEvery { pushMessagingDataSource.subscribeToTopic(any()) } throws IOException("SERVICE_NOT_AVAILABLE")

        // 購読できなくても起動を止めない
        repository.subscribeToAnnouncements()

        // 通信の都合の失敗は Crashlytics に送らない
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun subscribeToAnnouncements_invalidTopic_recordsException() = runTest {
        val exception = IllegalArgumentException("Invalid topic name")
        coEvery { pushMessagingDataSource.subscribeToTopic(any()) } throws exception

        repository.subscribeToAnnouncements()

        verify(exactly = 1) { crashReportingRepository.recordException(exception) }
    }
}
