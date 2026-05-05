package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class DecorationPreviewTransformTest {

    @Test
    fun resolveDecorationOffset_selectedDecoration_appliesOffsetDiff() {
        val resolvedOffset = resolveDecorationOffset(
            decorationId = "selected-id",
            selectedDecorationId = "selected-id",
            baseOffset = Offset(10f, 20f),
            offsetDiff = Offset(3f, -4f)
        )

        assertEquals(Offset(13f, 16f), resolvedOffset)
    }

    @Test
    fun resolveDecorationScale_selectedDecoration_multipliesScaleDiff() {
        val resolvedScale = resolveDecorationScale(
            decorationId = "selected-id",
            selectedDecorationId = "selected-id",
            baseScale = 2f,
            scaleDiff = 1.5f
        )

        assertEquals(3f, resolvedScale, FLOAT_DELTA)
    }

    @Test
    fun resolveDecorationRotation_nonSelectedDecoration_keepsBaseRotation() {
        val resolvedRotation = resolveDecorationRotation(
            decorationId = "other-id",
            selectedDecorationId = "selected-id",
            baseRotation = 30f,
            rotationDiff = 15f
        )

        assertEquals(30f, resolvedRotation, FLOAT_DELTA)
    }

    @Test
    fun resolveDecorationZIndex_selectedDecoration_returnsSelectedZIndex() {
        val resolvedZIndex = resolveDecorationZIndex(
            decorationId = "selected-id",
            selectedDecorationId = "selected-id"
        )

        assertEquals(SELECTED_DECORATION_Z_INDEX, resolvedZIndex, FLOAT_DELTA)
    }

    @Test
    fun resolveDecorationZIndex_nonSelectedDecoration_returnsDefaultZIndex() {
        val resolvedZIndex = resolveDecorationZIndex(
            decorationId = "other-id",
            selectedDecorationId = "selected-id"
        )

        assertEquals(DEFAULT_DECORATION_Z_INDEX, resolvedZIndex, FLOAT_DELTA)
    }

    @Test
    fun resolveDecorationZIndex_nullSelectedDecoration_returnsDefaultZIndex() {
        val resolvedZIndex = resolveDecorationZIndex(
            decorationId = "any-id",
            selectedDecorationId = null
        )

        assertEquals(DEFAULT_DECORATION_Z_INDEX, resolvedZIndex, FLOAT_DELTA)
    }

    @Test
    fun calculateCommittedScaleDiff_identityScale_returnsZero() {
        val scaleDelta = calculateCommittedScaleDiff(
            baseScale = 2f,
            scaleDiff = 1f
        )

        assertEquals(0f, scaleDelta, FLOAT_DELTA)
    }

    @Test
    fun calculateCommittedScaleDiff_scaledUp_returnsAdditiveDelta() {
        val scaleDelta = calculateCommittedScaleDiff(
            baseScale = 2f,
            scaleDiff = 1.5f
        )

        assertEquals(1f, scaleDelta, FLOAT_DELTA)
    }

    @Test
    fun calculateScaleFactor_zeroBaseScale_returnsIdentityFactor() {
        val scaleFactor = calculateScaleFactor(
            baseScale = 0f,
            targetScale = 2f
        )

        assertEquals(1f, scaleFactor, FLOAT_DELTA)
    }
}
