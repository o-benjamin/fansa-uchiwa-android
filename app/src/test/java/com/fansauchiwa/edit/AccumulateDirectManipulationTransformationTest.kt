package com.fansauchiwa.edit

import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class AccumulateDirectManipulationTransformationTest {

    @Test
    fun accumulateDirectManipulationTransformation_appliesZoomToCurrentScale() {
        val result = accumulateDirectManipulationTransformation(
            initialScale = 2f,
            currentScaleDiff = 1f,
            currentRotationDiff = 15f,
            zoomChange = 1.1f,
            rotationChange = 12f,
            minScale = 0.5f,
            maxScale = 6f
        )

        assertEquals(1.3f, result.scaleDiff, FLOAT_DELTA)
        assertEquals(27f, result.rotationDiff, FLOAT_DELTA)
    }

    @Test
    fun accumulateDirectManipulationTransformation_clampsScaleToUpperBound() {
        val result = accumulateDirectManipulationTransformation(
            initialScale = 5f,
            currentScaleDiff = 0.6f,
            currentRotationDiff = 0f,
            zoomChange = 1.2f,
            rotationChange = 0f,
            minScale = 0.5f,
            maxScale = 6f
        )

        assertEquals(1f, result.scaleDiff, FLOAT_DELTA)
    }

    @Test
    fun accumulateDirectManipulationTransformation_clampsScaleToLowerBound() {
        val result = accumulateDirectManipulationTransformation(
            initialScale = 1f,
            currentScaleDiff = 0f,
            currentRotationDiff = 0f,
            zoomChange = 0.1f,
            rotationChange = -8f,
            minScale = 0.5f,
            maxScale = 6f
        )

        // minScale (0.5f) - initialScale (1f) = -0.5f
        assertEquals(-0.5f, result.scaleDiff, FLOAT_DELTA)
        assertEquals(-8f, result.rotationDiff, FLOAT_DELTA)
    }
}
