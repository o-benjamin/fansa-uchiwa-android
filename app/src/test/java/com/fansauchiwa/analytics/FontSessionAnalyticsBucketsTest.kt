package com.fansauchiwa.analytics

import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class FontSessionAnalyticsBucketsTest {

    // region fontSwitchBucket

    @Test
    fun fontSwitchBucket_zero_returnsZeroBucket() {
        assertEquals("0", fontSwitchBucket(0))
    }

    @Test
    fun fontSwitchBucket_negative_returnsZeroBucket() {
        // 呼び出し元が想定外に負の値を渡しても落ちないことを確認する
        assertEquals("0", fontSwitchBucket(-1))
    }

    @Test
    fun fontSwitchBucket_boundaries_returnExpectedBuckets() {
        assertEquals("1-2", fontSwitchBucket(1))
        assertEquals("1-2", fontSwitchBucket(2))
        assertEquals("3-5", fontSwitchBucket(3))
        assertEquals("3-5", fontSwitchBucket(5))
        assertEquals("6-10", fontSwitchBucket(6))
        assertEquals("6-10", fontSwitchBucket(10))
        assertEquals("11-20", fontSwitchBucket(11))
        assertEquals("11-20", fontSwitchBucket(20))
        assertEquals("21+", fontSwitchBucket(21))
        assertEquals("21+", fontSwitchBucket(1000))
    }

    // endregion

    // region finalFontRankBucket

    // NEW を除いた宣言順。フォント選択画面のバッジの順位と同じ
    private val rankedFonts = FontFamilies.entries.filterNot { it.isNew }

    @Test
    fun finalFontRankBucket_firstRankedFont_returnsTopBucket() {
        // NEW を除いた1番目（rank 1）。NEW が先頭に並んでいても1位として数える
        assertEquals("1-5", finalFontRankBucket(rankedFonts.first()))
    }

    @Test
    fun finalFontRankBucket_boundaries_returnExpectedBuckets() {
        assertEquals("1-5", finalFontRankBucket(rankedFonts[4])) // rank 5
        assertEquals("6-10", finalFontRankBucket(rankedFonts[5])) // rank 6
        assertEquals("6-10", finalFontRankBucket(rankedFonts[9])) // rank 10
        assertEquals("11-20", finalFontRankBucket(rankedFonts[10])) // rank 11
        assertEquals("11-20", finalFontRankBucket(rankedFonts[19])) // rank 20
        assertEquals("21+", finalFontRankBucket(rankedFonts[20])) // rank 21
    }

    @Test
    fun finalFontRankBucket_lastRankedFont_returnsBottomBucket() {
        assertEquals("21+", finalFontRankBucket(rankedFonts.last()))
    }

    @Test
    fun finalFontRankBucket_newFonts_returnNewBucket() {
        FontFamilies.entries.filter { it.isNew }.forEach { font ->
            assertEquals("new", finalFontRankBucket(font))
        }
    }

    // endregion

    // region editDurationBucket

    @Test
    fun editDurationBucket_boundaries_returnExpectedBuckets() {
        assertEquals("0-1m", editDurationBucket(0L))
        assertEquals("0-1m", editDurationBucket(59_999L))
        assertEquals("1-3m", editDurationBucket(60_000L))
        assertEquals("1-3m", editDurationBucket(2 * 60_000L + 59_999L))
        assertEquals("3-5m", editDurationBucket(3 * 60_000L))
        assertEquals("3-5m", editDurationBucket(4 * 60_000L + 59_999L))
        assertEquals("5-10m", editDurationBucket(5 * 60_000L))
        assertEquals("5-10m", editDurationBucket(9 * 60_000L + 59_999L))
        assertEquals("10m+", editDurationBucket(10 * 60_000L))
        assertEquals("10m+", editDurationBucket(60 * 60_000L))
    }

    @Test
    fun editDurationBucket_negativeElapsed_returnsZeroToOneMinuteBucket() {
        // Clock skew などで負の値が来ても落ちないことを確認する
        assertEquals("0-1m", editDurationBucket(-1_000L))
    }

    // endregion
}
