package com.fansauchiwa.analytics

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.edit.FontFamilies
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PuffyStateAnalyticsTest {

    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var analytics: PuffyStateAnalytics

    @Before
    fun setUp() {
        localDatabaseRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        analytics = PuffyStateAnalytics(localDatabaseRepository, crashReportingRepository)
    }

    private fun givenUchiwa(textPuffy: Boolean, borderPuffy: Boolean) {
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } returns Uchiwa(
            id = "uchiwa-1",
            decorations = listOf(
                Decoration.Text(
                    id = "t",
                    text = "テスト",
                    font = FontFamilies.NOTO_SANS_JP,
                    isPuffyEnabled = textPuffy
                )
            ),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White,
            isOverallBorderPuffyEnabled = borderPuffy
        )
    }

    @Test
    fun exportParams_allPuffy_returnsOn() = runTest {
        givenUchiwa(textPuffy = true, borderPuffy = true)

        val params = analytics.exportParams("uchiwa-1")

        assertEquals(mapOf(PuffyStateParams.PARAM_PUFFY_STATE to "on"), params)
    }

    @Test
    fun exportParams_nothingPuffy_returnsOff() = runTest {
        givenUchiwa(textPuffy = false, borderPuffy = false)

        val params = analytics.exportParams("uchiwa-1")

        assertEquals(mapOf(PuffyStateParams.PARAM_PUFFY_STATE to "off"), params)
    }

    @Test
    fun exportParams_partlyPuffy_returnsMixed() = runTest {
        givenUchiwa(textPuffy = true, borderPuffy = false)

        val params = analytics.exportParams("uchiwa-1")

        assertEquals(mapOf(PuffyStateParams.PARAM_PUFFY_STATE to "mixed"), params)
    }

    @Test
    fun exportParams_uchiwaNotFound_returnsEmpty() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("missing") } returns null

        assertEquals(emptyMap<String, Any>(), analytics.exportParams("missing"))
    }

    @Test
    fun exportParams_nullId_returnsEmptyWithoutReadingDatabase() = runTest {
        assertEquals(emptyMap<String, Any>(), analytics.exportParams(null))
        coVerify(exactly = 0) { localDatabaseRepository.getUchiwa(any()) }
    }

    @Test
    fun exportParams_databaseThrows_returnsEmptyAndRecordsException() = runTest {
        val error = IllegalStateException("db error")
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws error

        val params = analytics.exportParams("uchiwa-1")

        assertEquals(emptyMap<String, Any>(), params)
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun exportParams_cancelled_rethrowsWithoutRecording() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws CancellationException("cancelled")

        val isRethrown = try {
            analytics.exportParams("uchiwa-1")
            false
        } catch (e: CancellationException) {
            true
        }

        assertTrue(isRethrown)
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }
}
