package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class ApplySnapToCenterTest {

    @Test
    fun snapToCenter_xAndY_bothSnapped_whenBothWithinThreshold() {
        val result = applySnapToCenter(
            decorationOffset = Offset(2f, 3f),
            offsetDiff = Offset(0f, 0f),
            snapThreshold = 5f
        )

        assertTrue(result.snappedX)
        assertTrue(result.snappedY)
        assertEquals(Offset(-2f, -3f), result.offsetDiff)
    }

    @Test
    fun snapToCenter_xSnapped_yNotSnapped_whenOnlyXWithinThreshold() {
        val result = applySnapToCenter(
            decorationOffset = Offset(3f, 0f),
            offsetDiff = Offset(0f, 10f),
            snapThreshold = 5f
        )

        assertTrue(result.snappedX)
        assertFalse(result.snappedY)
        assertEquals(-3f, result.offsetDiff.x, FLOAT_DELTA)
        assertEquals(10f, result.offsetDiff.y, FLOAT_DELTA)
    }

    @Test
    fun snapToCenter_ySnapped_xNotSnapped_whenOnlyYWithinThreshold() {
        val result = applySnapToCenter(
            decorationOffset = Offset(0f, 2f),
            offsetDiff = Offset(20f, 0f),
            snapThreshold = 5f
        )

        assertFalse(result.snappedX)
        assertTrue(result.snappedY)
        assertEquals(20f, result.offsetDiff.x, FLOAT_DELTA)
        assertEquals(-2f, result.offsetDiff.y, FLOAT_DELTA)
    }

    @Test
    fun snapToCenter_neitherAxisSnapped_whenBothExceedThreshold() {
        val result = applySnapToCenter(
            decorationOffset = Offset(10f, 10f),
            offsetDiff = Offset(5f, 5f),
            snapThreshold = 5f
        )

        assertFalse(result.snappedX)
        assertFalse(result.snappedY)
        assertEquals(5f, result.offsetDiff.x, FLOAT_DELTA)
        assertEquals(5f, result.offsetDiff.y, FLOAT_DELTA)
    }

    @Test
    fun snapToCenter_snapsAtExactThresholdBoundary() {
        val result = applySnapToCenter(
            decorationOffset = Offset(0f, 0f),
            offsetDiff = Offset(5f, 5f),
            snapThreshold = 5f
        )

        assertTrue(result.snappedX)
        assertTrue(result.snappedY)
    }

    @Test
    fun snapToCenter_alreadyAtCenter_remainsSnapped() {
        val result = applySnapToCenter(
            decorationOffset = Offset(0f, 0f),
            offsetDiff = Offset(0f, 0f),
            snapThreshold = 5f
        )

        assertTrue(result.snappedX)
        assertTrue(result.snappedY)
        assertEquals(0f, result.offsetDiff.x, FLOAT_DELTA)
        assertEquals(0f, result.offsetDiff.y, FLOAT_DELTA)
    }
}

