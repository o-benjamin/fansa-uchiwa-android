package com.fansauchiwa.edit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeDetectionUseCaseTest {

    @Test
    fun shouldDispatchShake_aboveThresholdAndOutsideDebounce_returnsTrue() {
        var now = 2_001L
        val useCase = ShakeDetectionUseCase(currentTimeMillis = { now })

        val result = useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 12f, z = 0f))

        assertTrue(result)
    }

    @Test
    fun shouldDispatchShake_belowThreshold_returnsFalse() {
        val useCase = ShakeDetectionUseCase(currentTimeMillis = { 2_001L })

        val result = useCase.shouldDispatchShake(AccelerationVector(x = 3f, y = 4f, z = 0f))

        assertFalse(result)
    }

    @Test
    fun shouldDispatchShake_atThreshold_returnsFalse() {
        val useCase = ShakeDetectionUseCase(currentTimeMillis = { 2_001L })

        val result = useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 0f, z = 0f))

        assertFalse(result)
    }

    @Test
    fun shouldDispatchShake_withinDebounce_returnsFalse() {
        var now = 2_001L
        val useCase = ShakeDetectionUseCase(currentTimeMillis = { now })

        assertTrue(useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 12f, z = 0f)))

        now = 2_500L
        val result = useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 12f, z = 0f))

        assertFalse(result)
    }

    @Test
    fun shouldDispatchShake_atDebounceBoundary_returnsFalse() {
        var now = 2_001L
        val useCase = ShakeDetectionUseCase(currentTimeMillis = { now })

        assertTrue(useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 12f, z = 0f)))

        now = 3_001L
        val result = useCase.shouldDispatchShake(AccelerationVector(x = 12f, y = 12f, z = 0f))

        assertFalse(result)
    }
}
