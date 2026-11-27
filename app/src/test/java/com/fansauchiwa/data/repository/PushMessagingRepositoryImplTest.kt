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
    fun subscribeToAnnouncements_invalidTopic_recordsExceptionWithoutThrowing() = runTest {
        // 購読の Task が失敗するのは、トピック名の形式の誤りのときだけ
        val exception = IllegalArgumentException("Invalid topic name")
        coEvery { pushMessagingDataSource.subscribeToTopic(any()) } throws exception

        // 購読できなくても起動を止めない
        repository.subscribeToAnnouncements()

        verify(exactly = 1) { crashReportingRepository.recordException(exception) }
    }
}
