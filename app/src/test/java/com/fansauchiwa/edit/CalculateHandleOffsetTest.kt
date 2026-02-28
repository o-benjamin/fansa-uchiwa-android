package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class CalculateHandleOffsetTest {

    @Test
    fun calculateHandleOffset_topLeftHandle_isTopLeftOfDecoration() {
        val decorationSize = Size(100f, 60f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 1f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.TopLeft
        )

        assertEquals(-50f, result.x, FLOAT_DELTA)
        assertEquals(-30f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_topRightHandle_isTopRightOfDecoration() {
        val decorationSize = Size(100f, 60f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 1f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.TopRight
        )

        assertEquals(50f, result.x, FLOAT_DELTA)
        assertEquals(-30f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_bottomLeftHandle_isBottomLeftOfDecoration() {
        val decorationSize = Size(100f, 60f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 1f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.BottomLeft
        )

        assertEquals(-50f, result.x, FLOAT_DELTA)
        assertEquals(30f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_bottomRightHandle_isBottomRightOfDecoration() {
        val decorationSize = Size(100f, 60f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 1f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.BottomRight
        )

        assertEquals(50f, result.x, FLOAT_DELTA)
        assertEquals(30f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_scalesCornerOffset_withGivenScale() {
        val decorationSize = Size(100f, 100f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 2f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.BottomRight
        )

        assertEquals(100f, result.x, FLOAT_DELTA)
        assertEquals(100f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_rotates90Degrees_correctlyTransposesCorner() {
        val decorationSize = Size(100f, 100f)
        val result = calculateHandleOffset(
            baseOffset = Offset(0f, 0f),
            scale = 1f,
            rotation = 90f,
            decorationSize = decorationSize,
            corner = HandleCorner.TopRight
        )

        assertEquals(50f, result.x, FLOAT_DELTA)
        assertEquals(50f, result.y, FLOAT_DELTA)
    }

    @Test
    fun calculateHandleOffset_shiftsWithBaseOffset() {
        val decorationSize = Size(100f, 100f)
        val result = calculateHandleOffset(
            baseOffset = Offset(200f, 100f),
            scale = 1f,
            rotation = 0f,
            decorationSize = decorationSize,
            corner = HandleCorner.TopLeft
        )

        assertEquals(150f, result.x, FLOAT_DELTA)
        assertEquals(50f, result.y, FLOAT_DELTA)
    }
}

