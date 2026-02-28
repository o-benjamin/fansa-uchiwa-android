package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class CalculateTransformationsTest {

    @Test
    fun calculateTransformations_returnsPositiveScaleDiff_whenDraggingAwayFromCenter() {
        val result = calculateTransformations(
            cumulativeOffset = Offset(10f, 0f),
            dragStartPosition = Offset(50f, 0f)
        )

        assertTrue(result.scaleDiff > 0f)
    }

    @Test
    fun calculateTransformations_returnsNegativeScaleDiff_whenDraggingTowardCenter() {
        val result = calculateTransformations(
            cumulativeOffset = Offset(-10f, 0f),
            dragStartPosition = Offset(50f, 0f)
        )

        assertTrue(result.scaleDiff < 0f)
    }

    @Test
    fun calculateTransformations_returnsPositiveRotationDiff_whenDraggingCounterClockwise() {
        val result = calculateTransformations(
            cumulativeOffset = Offset(0f, -50f),
            dragStartPosition = Offset(50f, 0f)
        )

        assertTrue(result.rotationDiff < 0f)
    }

    @Test
    fun calculateTransformations_returnsZeroRotationDiff_whenDraggingRadially() {
        val result = calculateTransformations(
            cumulativeOffset = Offset(10f, 0f),
            dragStartPosition = Offset(50f, 0f)
        )

        assertEquals(0f, result.rotationDiff, FLOAT_DELTA)
    }
}

