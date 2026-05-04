package com.fansauchiwa.edit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectTransformGestureSignificanceTest {

    @Test
    fun isDirectTransformGestureSignificant_zoomChangeBelowThreshold_returnsFalse() {
        val result = isDirectTransformGestureSignificant(
            zoomChange = 1.0005f,
            rotationChange = 0f
        )

        assertFalse(result)
    }

    @Test
    fun isDirectTransformGestureSignificant_zoomChangeAboveThreshold_returnsTrue() {
        val result = isDirectTransformGestureSignificant(
            zoomChange = 1.01f,
            rotationChange = 0.05f
        )

        assertTrue(result)
    }

    @Test
    fun isDirectTransformGestureSignificant_rotationChangeAtOrBelowThreshold_returnsFalse() {
        val result = isDirectTransformGestureSignificant(
            zoomChange = 1f,
            rotationChange = 0.1f
        )

        assertFalse(result)
    }

    @Test
    fun isDirectTransformGestureSignificant_rotationChangeAboveThreshold_returnsTrue() {
        val result = isDirectTransformGestureSignificant(
            zoomChange = 1.0005f,
            rotationChange = -0.11f
        )

        assertTrue(result)
    }

    @Test
    fun isDirectTransformGestureSignificant_bothValuesAtThreshold_returnsFalse() {
        val result = isDirectTransformGestureSignificant(
            zoomChange = 1.001f,
            rotationChange = 0.1f
        )

        assertFalse(result)
    }
}
