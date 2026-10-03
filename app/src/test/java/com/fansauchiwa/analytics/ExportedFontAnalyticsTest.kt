package com.fansauchiwa.analytics

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.edit.FontFamilies
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExportedFontAnalyticsTest {

    private lateinit var uchiwaReader: AnalyticsUchiwaReader
    private lateinit var analytics: ExportedFontAnalytics

    @Before
    fun setUp() {
        uchiwaReader = mockk()
        analytics = ExportedFontAnalytics(uchiwaReader)
    }

    private fun givenUchiwa(decorations: List<Decoration>) {
        coEvery { uchiwaReader.readOrNull("uchiwa-1") } returns Uchiwa(
            id = "uchiwa-1",
            decorations = decorations,
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )
    }

    private fun text(id: String, font: FontFamilies) = Decoration.Text(id = id, text = "テスト", font = font)

    private fun fontEvent(font: FontFamilies) = AnalyticsEvent(
        AnalyticsActions.EXPORT_UCHIWA_FONT,
        mapOf(FontFamilyParams.PARAM_FONT_FAMILY to font.name)
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
    fun fontEvents_uchiwaUnreadable_returnsEmpty() = runTest {
        // 見つからない・IDが無い・読み込みに失敗した、のどれも reader は null を返す（AnalyticsUchiwaReaderTest）
        coEvery { uchiwaReader.readOrNull(any()) } returns null

        assertEquals(emptyList<AnalyticsEvent>(), analytics.fontEvents("uchiwa-1"))
    }
}
