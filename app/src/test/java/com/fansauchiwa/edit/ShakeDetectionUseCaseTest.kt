package com.fansauchiwa.edit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeDetectionUseCaseTest {

    @Test
    fun shouldDetect_accelerationExceedsThresholdAfterDebounce_returnsTrue() {
        var currentTime = 1_500L
        val useCase = ShakeDetectionUseCase(timeProvider = { currentTime })

        val result = useCase.shouldDetect(AccelerationSample(x = 13f, y = 0f, z = 0f))

        assertTrue(result)
    }

    @Test
    fun shouldDetect_accelerationEqualsThreshold_returnsFalse() {
        val useCase = ShakeDetectionUseCase(timeProvider = { 1_500L })

        val result = useCase.shouldDetect(AccelerationSample(x = 12f, y = 0f, z = 0f))

        assertFalse(result)
    }

    @Test
    fun shouldDetect_secondShakeWithinDebounce_returnsFalse() {
        var currentTime = 1_500L
        val useCase = ShakeDetectionUseCase(timeProvider = { currentTime })

        assertTrue(useCase.shouldDetect(AccelerationSample(x = 13f, y = 0f, z = 0f)))

        currentTime = 2_000L
        val result = useCase.shouldDetect(AccelerationSample(x = 13f, y = 0f, z = 0f))

        assertFalse(result)
    }

    @Test
    fun shouldDetect_secondShakeAfterDebounce_returnsTrue() {
        var currentTime = 1_500L
        val useCase = ShakeDetectionUseCase(timeProvider = { currentTime })

        assertTrue(useCase.shouldDetect(AccelerationSample(x = 13f, y = 0f, z = 0f)))

        currentTime = 2_501L
        val result = useCase.shouldDetect(AccelerationSample(x = 13f, y = 0f, z = 0f))

        assertTrue(result)
    }

    @Test
    fun shouldDetect_accelerationBelowThreshold_returnsFalse() {
        val useCase = ShakeDetectionUseCase(timeProvider = { 1_500L })

        val result = useCase.shouldDetect(AccelerationSample(x = 5f, y = 5f, z = 5f))

        assertFalse(result)
    }
}
