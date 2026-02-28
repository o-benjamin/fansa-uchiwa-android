package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class RotatedDragAmountTest {

    @Test
    fun rotatedDragAmount_returnsOriginalDrag_whenRotationIsZeroAndScaleIsOne() {
        val result = rotatedDragAmount(
            currentRotation = 0f,
            currentScale = 1f,
            dragAmount = Offset(10f, 5f)
        )

        assertEquals(10f, result.x, FLOAT_DELTA)
        assertEquals(5f, result.y, FLOAT_DELTA)
    }

    @Test
    fun rotatedDragAmount_scalesDragAmount_beforeRotating() {
        val result = rotatedDragAmount(
            currentRotation = 0f,
            currentScale = 2f,
            dragAmount = Offset(10f, 5f)
        )

        assertEquals(20f, result.x, FLOAT_DELTA)
        assertEquals(10f, result.y, FLOAT_DELTA)
    }

    @Test
    fun rotatedDragAmount_rotates90Degrees_swapsXAndY() {
        val result = rotatedDragAmount(
            currentRotation = 90f,
            currentScale = 1f,
            dragAmount = Offset(10f, 0f)
        )

        assertEquals(0f, result.x, FLOAT_DELTA)
        assertEquals(10f, result.y, FLOAT_DELTA)
    }

    @Test
    fun rotatedDragAmount_rotates180Degrees_reversesDirection() {
        val result = rotatedDragAmount(
            currentRotation = 180f,
            currentScale = 1f,
            dragAmount = Offset(10f, 0f)
        )

        assertEquals(-10f, result.x, FLOAT_DELTA)
        assertEquals(0f, result.y, FLOAT_DELTA)
    }
}

