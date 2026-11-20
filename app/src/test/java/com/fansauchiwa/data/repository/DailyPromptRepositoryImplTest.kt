package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.data.DailyPromptSelector
import com.fansauchiwa.data.DailyPromptsConfig
import com.fansauchiwa.data.TodayProvider
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DailyPromptRepositoryImplTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 24)
    private val defaultForToday = DailyPromptSelector.select(DailyPromptsConfig.Unset, today)
    private val todayJson = """{"prompts": [{"date": "2026-10-24", "id": "20261024", "text": "ピースして"}]}"""
    private val todayPrompt = DailyPrompt("20261024", "ピースして")

    private lateinit var remoteConfigDataSource: RemoteConfigDataSource
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var todayProvider: TodayProvider
    private lateinit var repository: DailyPromptRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        remoteConfigDataSource = mockk()
        crashReportingRepository = mockk(relaxed = true)
        todayProvider = mockk()
        every { todayProvider.today() } returns today
        repository = DailyPromptRepositoryImpl(remoteConfigDataSource, crashReportingRepository, todayProvider)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun fetchDailyPrompt_remoteHasToday_emitsRemotePrompt() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf(todayJson)

        repository.fetchDailyPrompt()

        assertEquals(todayPrompt, repository.getDailyPromptStream().first())
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_cachedValueThenFetched_emitsCachedBeforeFetchCompletes() = runTest {
        val fetchCompleted = CompletableDeferred<Unit>()
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flow {
            emit("")
            fetchCompleted.await()
            emit(todayJson)
        }

        launch { repository.fetchDailyPrompt() }
        runCurrent()

        // 取得を待たずに、端末に残っていた値（ここでは未設定なので控え）が流れている
        assertEquals(defaultForToday, repository.getDailyPromptStream().first())

        fetchCompleted.complete(Unit)
        advanceUntilIdle()

        assertEquals(todayPrompt, repository.getDailyPromptStream().first())
    }

    @Test
    fun fetchDailyPrompt_someItemsDropped_logsPositionsWithoutRecording() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf(
            """
            {"prompts": [
              {"date": "2026-10-24", "id": "20261024", "text": "ピースして"},
              {"date": "2026-10-25", "id": "20261025", "text": "https://a.jp"}
            ]}
            """.trimIndent()
        )

        repository.fetchDailyPrompt()

        assertEquals(todayPrompt, repository.getDailyPromptStream().first())
        verify(exactly = 1) { Log.w("DailyPrompt", match<String> { it.contains("2 件目") }) }
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_remoteLacksToday_emitsDefault() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf(
            """{"prompts": [{"date": "2026-10-25", "id": "20261025", "text": "手ふって"}]}"""
        )

        repository.fetchDailyPrompt()

        assertEquals(defaultForToday, repository.getDailyPromptStream().first())
    }

    @Test
    fun fetchDailyPrompt_emptyValue_emitsDefaultWithoutRecording() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf("")

        repository.fetchDailyPrompt()

        assertEquals(defaultForToday, repository.getDailyPromptStream().first())
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_emptyPrompts_emitsNull() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf("""{"prompts": []}""")

        repository.fetchDailyPrompt()

        assertNull(repository.getDailyPromptStream().first())
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_malformedJson_emitsDefaultAndRecordsException() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf("""{"prompts": [""")

        repository.fetchDailyPrompt()

        assertEquals(defaultForToday, repository.getDailyPromptStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_malformedJsonTwice_recordsExceptionOnlyOnce() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf("""{"prompts": [""")

        repository.fetchDailyPrompt()
        repository.fetchDailyPrompt()

        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_allItemsUnusable_emitsDefaultAndRecordsException() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf(
            """{"prompts": [{"date": "2026-10-24", "id": "a", "text": "https://a.jp"}]}"""
        )

        repository.fetchDailyPrompt()

        assertEquals(defaultForToday, repository.getDailyPromptStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchDailyPrompt_dataSourceThrows_emitsDefaultAndRecordsException() = runTest {
        val error = IOException("読めない")
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flow { throw error }

        repository.fetchDailyPrompt()

        assertEquals(defaultForToday, repository.getDailyPromptStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun fetchDailyPrompt_dataSourceThrowsAfterCachedValue_keepsCachedPrompt() = runTest {
        val error = IOException("取得の途中で失敗")
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flow {
            emit(todayJson)
            throw error
        }

        repository.fetchDailyPrompt()

        assertEquals(todayPrompt, repository.getDailyPromptStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun fetchDailyPrompt_dateChangesBetweenCalls_emitsPromptForNewDate() = runTest {
        every { remoteConfigDataSource.getDailyPromptsJsonStream() } returns flowOf(
            """
            {"prompts": [
              {"date": "2026-10-24", "id": "20261024", "text": "ピースして"},
              {"date": "2026-10-25", "id": "20261025", "text": "手ふって"}
            ]}
            """.trimIndent()
        )
        every { todayProvider.today() } returnsMany listOf(today, today.plusDays(1))

        repository.fetchDailyPrompt()
        repository.fetchDailyPrompt()

        assertEquals(DailyPrompt("20261025", "手ふって"), repository.getDailyPromptStream().first())
    }
}
