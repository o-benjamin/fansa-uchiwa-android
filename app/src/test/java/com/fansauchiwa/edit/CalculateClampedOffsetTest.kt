package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class CalculateClampedOffsetTest {

    @Test
    fun calculateClampedOffset_returnsSimpleSum_whenBoundaryIsNull() {
        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(10f, 10f),
            cumulativeOffset = Offset(5f, 5f),
            dragAmount = Offset(3f, 3f),
            boundarySize = null
        )

        assertEquals(Offset(8f, 8f), result)
    }

    @Test
    fun calculateClampedOffset_returnsSimpleSum_whenBoundarySizeIsZero() {
        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(10f, 10f),
            cumulativeOffset = Offset(5f, 5f),
            dragAmount = Offset(3f, 3f),
            boundarySize = IntSize(0, 0)
        )

        assertEquals(Offset(8f, 8f), result)
    }

    @Test
    fun calculateClampedOffset_returnsNormalMovement_whenWithinBoundary() {
        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(0f, 0f),
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(10f, 10f),
            boundarySize = IntSize(400, 400)
        )

        assertEquals(10f, result.x, FLOAT_DELTA)
        assertEquals(10f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateClampedOffset_clampsToMaxX_whenDragExceedsRightBoundary() {
        val boundarySize = IntSize(200, 400)
        val halfWidth = (boundarySize.width / 2f) * 0.7f

        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(0f, 0f),
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(1000f, 0f),
            boundarySize = boundarySize
        )

        assertEquals(halfWidth, result.x, FLOAT_DELTA)
    }

    @Test
    fun calculateClampedOffset_clampsToMinX_whenDragExceedsLeftBoundary() {
        val boundarySize = IntSize(200, 400)
        val halfWidth = (boundarySize.width / 2f) * 0.7f

        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(0f, 0f),
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(-1000f, 0f),
            boundarySize = boundarySize
        )

        assertEquals(-halfWidth, result.x, FLOAT_DELTA)
    }

    @Test
    fun calculateClampedOffset_clampsToMaxY_whenDragExceedsBottomBoundary() {
        val boundarySize = IntSize(400, 200)
        val halfHeight = (boundarySize.height / 2f) * 0.9f

        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(0f, 0f),
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(0f, 1000f),
            boundarySize = boundarySize
        )

        assertEquals(halfHeight, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateClampedOffset_clampsToMinY_whenDragExceedsTopBoundary() {
        val boundarySize = IntSize(400, 200)
        val halfHeight = (boundarySize.height / 2f) * 0.9f

        val result = calculateClampedOffset(
            currentConfirmedOffset = Offset(0f, 0f),
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(0f, -1000f),
            boundarySize = boundarySize
        )

        assertEquals(-halfHeight, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateClampedOffset_accountsForCurrentConfirmedOffset_whenCalculatingBound() {
        val boundarySize = IntSize(200, 200)
        val halfWidth = (boundarySize.width / 2f) * 0.7f
        val confirmedOffset = Offset(halfWidth - 5f, 0f)

        val result = calculateClampedOffset(
            currentConfirmedOffset = confirmedOffset,
            cumulativeOffset = Offset(0f, 0f),
            dragAmount = Offset(100f, 0f),
            boundarySize = boundarySize
        )

        assertEquals(5f, result.x, FLOAT_DELTA)
    }
}

