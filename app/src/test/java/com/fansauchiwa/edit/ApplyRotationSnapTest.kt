package com.fansauchiwa.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FLOAT_DELTA = 0.001f

class ApplyRotationSnapTest {

    // ---- スナップポイント（0, 90, 180, 270度）に閾値内で近い場合 ----

    @Test
    fun applyRotationSnap_exactlyAt0_isSnapped() {
        val result = applyRotationSnap(0f)
        assertTrue(result.isSnapped)
        assertEquals(0f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_exactlyAt90_isSnapped() {
        val result = applyRotationSnap(90f)
        assertTrue(result.isSnapped)
        assertEquals(90f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_exactlyAt180_isSnapped() {
        val result = applyRotationSnap(180f)
        assertTrue(result.isSnapped)
        assertEquals(180f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_exactlyAt270_isSnapped() {
        val result = applyRotationSnap(270f)
        assertTrue(result.isSnapped)
        assertEquals(270f, result.snappedRotation, FLOAT_DELTA)
    }

    // ---- 閾値内（±5度）での近傍値 ----

    @Test
    fun applyRotationSnap_withinThreshold_snapsTo90() {
        val result = applyRotationSnap(93f)
        assertTrue(result.isSnapped)
        assertEquals(90f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_withinThreshold_snapsTo180() {
        val result = applyRotationSnap(177f)
        assertTrue(result.isSnapped)
        assertEquals(180f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_withinThreshold_snapsTo0FromBelow360() {
        val result = applyRotationSnap(357f)
        assertTrue(result.isSnapped)
        assertEquals(360f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_withinThreshold_snapsTo270() {
        val result = applyRotationSnap(274f)
        assertTrue(result.isSnapped)
        assertEquals(270f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_exactlyAtThresholdBoundary_snapsTo90() {
        val result = applyRotationSnap(98f)
        assertTrue(result.isSnapped)
        assertEquals(90f, result.snappedRotation, FLOAT_DELTA)
    }

    // ---- 閾値外 ----

    @Test
    fun applyRotationSnap_outsideThreshold_notSnapped() {
        val result = applyRotationSnap(45f)
        assertFalse(result.isSnapped)
        assertEquals(45f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_justOutsideThreshold_notSnapped() {
        val result = applyRotationSnap(99f)
        assertFalse(result.isSnapped)
        assertEquals(99f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_135degrees_notSnapped() {
        val result = applyRotationSnap(135f)
        assertFalse(result.isSnapped)
        assertEquals(135f, result.snappedRotation, FLOAT_DELTA)
    }

    // ---- 負の角度 ----

    @Test
    fun applyRotationSnap_negative90_snapsToNegative90() {
        val result = applyRotationSnap(-90f)
        assertTrue(result.isSnapped)
        assertEquals(-90f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_negative87_snapsToNegative90() {
        val result = applyRotationSnap(-87f)
        assertTrue(result.isSnapped)
        assertEquals(-90f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_negative45_notSnapped() {
        val result = applyRotationSnap(-45f)
        assertFalse(result.isSnapped)
        assertEquals(-45f, result.snappedRotation, FLOAT_DELTA)
    }

    // ---- 360度超 ----

    @Test
    fun applyRotationSnap_360_snapsTo360() {
        val result = applyRotationSnap(360f)
        assertTrue(result.isSnapped)
        assertEquals(360f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_450_snapsTo450() {
        val result = applyRotationSnap(450f)
        assertTrue(result.isSnapped)
        assertEquals(450f, result.snappedRotation, FLOAT_DELTA)
    }

    @Test
    fun applyRotationSnap_405_notSnapped() {
        val result = applyRotationSnap(405f)
        assertFalse(result.isSnapped)
        assertEquals(405f, result.snappedRotation, FLOAT_DELTA)
    }
}

