package com.fansauchiwa.ads

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdLoadWaitTest {

    @Test
    fun awaitLoadFinished_NotLoading_ReturnsTrueWithoutWaiting() = runTest {
        val isLoading = MutableStateFlow(false)

        val finished = AdLoadWait.awaitLoadFinished(isLoading)

        assertTrue(finished)
        assertEquals(0L, currentTime)
    }

    @Test
    fun awaitLoadFinished_LoadFinishesBeforeMaxWait_ReturnsTrueAtThatMoment() = runTest {
        val isLoading = MutableStateFlow(true)
        val result = async { AdLoadWait.awaitLoadFinished(isLoading) }

        advanceTimeBy(1_200L)
        isLoading.value = false
        runCurrent()

        assertTrue(result.isCompleted)
        assertTrue(result.await())
        assertEquals(1_200L, currentTime)
    }

    @Test
    fun awaitLoadFinished_StillLoadingAtMaxWait_ReturnsFalse() = runTest {
        val isLoading = MutableStateFlow(true)

        val finished = AdLoadWait.awaitLoadFinished(isLoading)

        assertFalse(finished)
        assertEquals(AdLoadWait.MAX_WAIT_MILLIS, currentTime)
    }

    @Test
    fun awaitLoadFinished_JustBeforeMaxWait_KeepsWaiting() = runTest {
        val isLoading = MutableStateFlow(true)
        val result = async { AdLoadWait.awaitLoadFinished(isLoading) }

        advanceTimeBy(AdLoadWait.MAX_WAIT_MILLIS - 1)

        assertFalse(result.isCompleted)
        result.cancel()
    }
}
