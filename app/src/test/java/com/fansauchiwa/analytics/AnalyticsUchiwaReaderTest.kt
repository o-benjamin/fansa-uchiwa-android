package com.fansauchiwa.analytics

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyticsUchiwaReaderTest {

    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var reader: AnalyticsUchiwaReader

    @Before
    fun setUp() {
        localDatabaseRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        reader = AnalyticsUchiwaReader(localDatabaseRepository, crashReportingRepository)
    }

    @Test
    fun readOrNull_uchiwaExists_returnsUchiwa() = runTest {
        val uchiwa = Uchiwa(
            id = "uchiwa-1",
            decorations = emptyList(),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } returns uchiwa

        assertEquals(uchiwa, reader.readOrNull("uchiwa-1"))
    }

    @Test
    fun readOrNull_uchiwaNotFound_returnsNull() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("missing") } returns null

        assertNull(reader.readOrNull("missing"))
    }

    @Test
    fun readOrNull_nullId_returnsNullWithoutReadingDatabase() = runTest {
        assertNull(reader.readOrNull(null))
        coVerify(exactly = 0) { localDatabaseRepository.getUchiwa(any()) }
    }

    @Test
    fun readOrNull_databaseThrows_returnsNullAndRecordsException() = runTest {
        val error = IllegalStateException("db error")
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws error

        assertNull(reader.readOrNull("uchiwa-1"))
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun readOrNull_cancelled_rethrowsWithoutRecording() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws CancellationException("cancelled")

        val isRethrown = try {
            reader.readOrNull("uchiwa-1")
            false
        } catch (e: CancellationException) {
            true
        }

        assertTrue(isRethrown)
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }
}
