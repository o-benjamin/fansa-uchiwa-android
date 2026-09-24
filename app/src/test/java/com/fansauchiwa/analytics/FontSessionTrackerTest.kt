package com.fansauchiwa.analytics

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import com.fansauchiwa.edit.FontFamilies
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FontSessionTrackerTest {

    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var masterpieceRepository: MasterpieceRepository
    private var nowMillis = 0L
    private lateinit var tracker: FontSessionTracker

    @Before
    fun setUp() {
        localDatabaseRepository = mockk(relaxed = true)
        masterpieceRepository = mockk(relaxed = true)
        every { masterpieceRepository.loadAllMasterpieces() } returns emptyList()
        nowMillis = 0L
        tracker = FontSessionTracker(localDatabaseRepository, masterpieceRepository) { nowMillis }
        tracker.startSession()
    }

    private fun text(id: String, font: FontFamilies) = Decoration.Text(id = id, text = "テスト", font = font)

    /** 保存済みのうちわ画像の並び（新しい順）と、直前のうちわの装飾を用意する */
    private fun givenSavedUchiwas(currentId: String, previousId: String?, previousFonts: List<FontFamilies>) {
        val paths = listOfNotNull(currentId, previousId).map { "/data/masterpiece/$it.png" }
        every { masterpieceRepository.loadAllMasterpieces() } returns paths
        if (previousId != null) {
            coEvery { localDatabaseRepository.getUchiwa(previousId) } returns Uchiwa(
                id = previousId,
                decorations = previousFonts.mapIndexed { index, font -> text("prev-$index", font) },
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )
        }
    }

    @Test
    fun discardParams_noSwitch_returnsZeroSwitchBucketAndElapsedDuration() {
        nowMillis = 30_000L

        val params = tracker.discardParams()

        assertEquals("0", params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET])
        assertEquals("0-1m", params[FontSessionAnalyticsParams.EDIT_DURATION_BUCKET])
        assertEquals(2, params.size)
    }

    @Test
    fun discardParams_afterThreeSwitches_returnsThreeToFiveBucket() {
        repeat(3) { tracker.onFontSwitched("text-1") }

        val params = tracker.discardParams()

        assertEquals("3-5", params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET])
    }

    @Test
    fun exportParams_sessionNotFinished_returnsEmpty() = runTest {
        tracker.onFontSwitched("text-1")

        assertTrue(tracker.exportParams(currentUchiwaId = "current").isEmpty())
    }

    @Test
    fun exportParams_afterSwitches_sendsAllFourParamsWithLastSwitchedFont() = runTest {
        givenSavedUchiwas(currentId = "current", previousId = "prev", previousFonts = listOf(FontFamilies.KEI_FONT))
        tracker.onFontSwitched("text-1")
        tracker.onFontSwitched("text-2")
        nowMillis = 4 * 60_000L
        tracker.finishSessionForPreview(
            listOf(text("text-1", FontFamilies.KOSUGI), text("text-2", FontFamilies.KEI_FONT))
        )

        val params = tracker.exportParams(currentUchiwaId = "current")

        assertEquals("1-2", params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET])
        assertEquals("3-5m", params[FontSessionAnalyticsParams.EDIT_DURATION_BUCKET])
        assertEquals(
            finalFontRankBucket(FontFamilies.KEI_FONT),
            params[FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET]
        )
        assertEquals("true", params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST])
    }

    @Test
    fun exportParams_switchedDecorationDeleted_fallsBackToFirstTextDecorationFont() = runTest {
        givenSavedUchiwas(currentId = "current", previousId = "prev", previousFonts = listOf(FontFamilies.KOSUGI))
        tracker.onFontSwitched("deleted-text")
        tracker.finishSessionForPreview(listOf(text("text-2", FontFamilies.KOSUGI)))

        val params = tracker.exportParams(currentUchiwaId = "current")

        assertEquals("1-2", params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET])
        assertEquals(
            finalFontRankBucket(FontFamilies.KOSUGI),
            params[FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET]
        )
        assertEquals("true", params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST])
    }

    @Test
    fun exportParams_noTextDecoration_returnsOnlySwitchAndDurationBuckets() = runTest {
        tracker.finishSessionForPreview(emptyList())

        val params = tracker.exportParams(currentUchiwaId = "current")

        assertEquals(
            setOf(FontSessionAnalyticsParams.FONT_SWITCH_BUCKET, FontSessionAnalyticsParams.EDIT_DURATION_BUCKET),
            params.keys
        )
    }

    @Test
    fun exportParams_noPreviousUchiwa_sendsSameAsLastFalse() = runTest {
        givenSavedUchiwas(currentId = "current", previousId = null, previousFonts = emptyList())
        tracker.finishSessionForPreview(listOf(text("text-1", FontFamilies.KEI_FONT)))

        val params = tracker.exportParams(currentUchiwaId = "current")

        assertEquals("false", params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST])
    }

    @Test
    fun exportParams_previousUchiwaUsesOtherFont_sendsSameAsLastFalse() = runTest {
        givenSavedUchiwas(currentId = "current", previousId = "prev", previousFonts = listOf(FontFamilies.KOSUGI))
        tracker.finishSessionForPreview(listOf(text("text-1", FontFamilies.KEI_FONT)))

        val params = tracker.exportParams(currentUchiwaId = "current")

        assertEquals("false", params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST])
    }

    @Test
    fun exportParams_calledTwice_comparesWithPreviousUchiwaNotItself() = runTest {
        // 同じPreview画面で2回保存しても、比べる相手は今回のうちわではなく直前のうちわのまま
        givenSavedUchiwas(currentId = "current", previousId = "prev", previousFonts = listOf(FontFamilies.KOSUGI))
        tracker.finishSessionForPreview(listOf(text("text-1", FontFamilies.KEI_FONT)))

        tracker.exportParams(currentUchiwaId = "current")
        val secondParams = tracker.exportParams(currentUchiwaId = "current")

        assertEquals("false", secondParams[FontSessionAnalyticsParams.FONT_SAME_AS_LAST])
    }

    @Test
    fun finishSessionForPreview_thenSwitchAgain_countsOnlyNewSwitches() {
        repeat(4) { tracker.onFontSwitched("text-1") }
        tracker.finishSessionForPreview(listOf(text("text-1", FontFamilies.KEI_FONT)))

        tracker.onFontSwitched("text-1")

        assertEquals("1-2", tracker.discardParams()[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET])
    }

    @Test
    fun startSession_afterFinish_dropsPreviousSnapshot() = runTest {
        tracker.finishSessionForPreview(listOf(text("text-1", FontFamilies.KEI_FONT)))

        tracker.startSession()

        assertTrue(tracker.exportParams(currentUchiwaId = "current").isEmpty())
    }
}
