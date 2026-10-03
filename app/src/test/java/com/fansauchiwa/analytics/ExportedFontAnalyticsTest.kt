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

class ExportedFontAnalyticsTest {

    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var analytics: ExportedFontAnalytics

    @Before
    fun setUp() {
        localDatabaseRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        analytics = ExportedFontAnalytics(localDatabaseRepository, crashReportingRepository)
    }

    private fun givenUchiwa(decorations: List<Decoration>) {
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } returns Uchiwa(
            id = "uchiwa-1",
            decorations = decorations,
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )
    }

    private fun text(id: String, font: FontFamilies) = Decoration.Text(id = id, text = "テスト", font = font)

    private fun fontEvent(font: FontFamilies) = AnalyticsEvent(
        AnalyticsActions.EXPORT_UCHIWA_FONT,
        mapOf(ExportedFontParams.PARAM_FONT_FAMILY to font.name)
    )

    @Test
    fun fontEvents_sameFontUsedTwice_returnsOneEventPerFont() = runTest {
        givenUchiwa(
            listOf(
                text("t1", FontFamilies.NOTO_SANS_JP),
                text("t2", FontFamilies.DELA_GOTHIC_ONE),
                text("t3", FontFamilies.NOTO_SANS_JP)
            )
        )

        val events = analytics.fontEvents("uchiwa-1")

        assertEquals(
            listOf(fontEvent(FontFamilies.NOTO_SANS_JP), fontEvent(FontFamilies.DELA_GOTHIC_ONE)),
            events
        )
    }

    @Test
    fun fontEvents_noTextDecoration_returnsEmpty() = runTest {
        givenUchiwa(listOf(Decoration.Sticker(label = "heart", id = "s1")))

        assertEquals(emptyList<AnalyticsEvent>(), analytics.fontEvents("uchiwa-1"))
    }

    @Test
    fun fontEvents_noDecoration_returnsEmpty() = runTest {
        givenUchiwa(emptyList())

        assertEquals(emptyList<AnalyticsEvent>(), analytics.fontEvents("uchiwa-1"))
    }

    @Test
    fun fontEvents_uchiwaNotFound_returnsEmpty() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("missing") } returns null

        assertEquals(emptyList<AnalyticsEvent>(), analytics.fontEvents("missing"))
    }

    @Test
    fun fontEvents_nullId_returnsEmptyWithoutReadingDatabase() = runTest {
        assertEquals(emptyList<AnalyticsEvent>(), analytics.fontEvents(null))
        coVerify(exactly = 0) { localDatabaseRepository.getUchiwa(any()) }
    }

    @Test
    fun fontEvents_databaseThrows_returnsEmptyAndRecordsException() = runTest {
        val error = IllegalStateException("db error")
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws error

        val events = analytics.fontEvents("uchiwa-1")

        assertEquals(emptyList<AnalyticsEvent>(), events)
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun fontEvents_cancelled_rethrowsWithoutRecording() = runTest {
        coEvery { localDatabaseRepository.getUchiwa("uchiwa-1") } throws CancellationException("cancelled")

        val isRethrown = try {
            analytics.fontEvents("uchiwa-1")
            false
        } catch (e: CancellationException) {
            true
        }

        assertTrue(isRethrown)
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }
}
