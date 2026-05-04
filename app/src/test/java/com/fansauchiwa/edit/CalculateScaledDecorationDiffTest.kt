package com.fansauchiwa.edit

import com.fansauchiwa.data.Decoration
import org.junit.Assert.assertEquals
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class CalculateScaledDecorationDiffTest {

    @Test
    fun calculateScaledDecorationDiff_textZoomAccumulatesFromCurrentPreviewScale_returnsUpdatedDiff() {
        val decoration = Decoration.Text(
            id = "text-id",
            font = FontFamilies.HACHI_MARU_POP,
            scale = 2f
        )

        val result = calculateScaledDecorationDiff(
            baseScale = decoration.scale,
            currentScaleDiff = 1f,
            zoomChange = 1.5f,
            scaleRange = decoration.scaleRange()
        )

        assertEquals(2.5f, result, FLOAT_DELTA)
    }

    @Test
    fun calculateScaledDecorationDiff_stickerZoomBeyondMax_clampsToStickerLimit() {
        val decoration = Decoration.Sticker(
            id = "sticker-id",
            label = "heart",
            scale = 2.8f
        )

        val result = calculateScaledDecorationDiff(
            baseScale = decoration.scale,
            currentScaleDiff = 0f,
            zoomChange = 1.5f,
            scaleRange = decoration.scaleRange()
        )

        assertEquals(0.2f, result, FLOAT_DELTA)
    }

    @Test
    fun calculateScaledDecorationDiff_imageZoomBeyondMin_clampsToImageLimit() {
        val decoration = Decoration.Image(
            id = "image-id",
            imageId = "image-ref",
            scale = 0.75f
        )

        val result = calculateScaledDecorationDiff(
            baseScale = decoration.scale,
            currentScaleDiff = 0f,
            zoomChange = 0.1f,
            scaleRange = decoration.scaleRange()
        )

        assertEquals(-0.25f, result, FLOAT_DELTA)
    }
}
