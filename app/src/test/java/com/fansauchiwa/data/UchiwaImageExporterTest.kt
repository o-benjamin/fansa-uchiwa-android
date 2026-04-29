package com.fansauchiwa.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UchiwaImageExporterTest {

    @Test
    fun createUchiwaImageRenderPlan_validInput_returnsExpectedScale() {
        val renderPlan = createUchiwaImageRenderPlan(
            layerWidth = 1191f,
            layerHeight = 842f,
            targetWidth = 2382,
            targetHeight = 1684
        )

        assertEquals(2382, renderPlan.targetWidth)
        assertEquals(1684, renderPlan.targetHeight)
        assertEquals(2f, renderPlan.scaleX, 0f)
        assertEquals(2f, renderPlan.scaleY, 0f)
    }

    @Test
    fun createUchiwaImageRenderPlan_targetMatchesLayer_returnsUnitScale() {
        val renderPlan = createUchiwaImageRenderPlan(
            layerWidth = 2382f,
            layerHeight = 1684f,
            targetWidth = 2382,
            targetHeight = 1684
        )

        assertEquals(1f, renderPlan.scaleX, 0f)
        assertEquals(1f, renderPlan.scaleY, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createUchiwaImageRenderPlan_zeroLayerWidth_throwsIllegalArgumentException() {
        createUchiwaImageRenderPlan(
            layerWidth = 0f,
            layerHeight = 1684f,
            targetWidth = 2382,
            targetHeight = 1684
        )
    }
}
