package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class RotateByTest {

    @Test
    fun rotateBy_rotates360Degrees_returnsOriginalOffset() {
        val original = Offset(30f, 40f)
        val result = original.rotateBy(360f)

        assertEquals(original.x, result.x, FLOAT_DELTA)
        assertEquals(original.y, result.y, FLOAT_DELTA)
    }

    @Test
    fun rotateBy_rotatesZeroDegrees_returnsOriginalOffset() {
        val original = Offset(30f, 40f)
        val result = original.rotateBy(0f)

        assertEquals(original.x, result.x, FLOAT_DELTA)
        assertEquals(original.y, result.y, FLOAT_DELTA)
    }
}

