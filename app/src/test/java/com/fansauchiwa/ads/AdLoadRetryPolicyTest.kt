package com.fansauchiwa.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdLoadRetryPolicyTest {

    @Test
    fun delayMillisFor_FirstAttempt_ReturnsInitialDelay() {
        assertEquals(2_000L, AdLoadRetryPolicy.delayMillisFor(0))
    }

    @Test
    fun delayMillisFor_EachAttempt_DoublesDelay() {
        val delays = (0 until AdLoadRetryPolicy.MAX_ATTEMPTS).map { AdLoadRetryPolicy.delayMillisFor(it) }

        assertEquals(listOf(2_000L, 4_000L, 8_000L, 16_000L, 32_000L), delays)
    }

    @Test
    fun delayMillisFor_LastAllowedAttempt_ReturnsDelay() {
        assertEquals(32_000L, AdLoadRetryPolicy.delayMillisFor(AdLoadRetryPolicy.MAX_ATTEMPTS - 1))
    }

    @Test
    fun delayMillisFor_AttemptReachesMax_ReturnsNull() {
        assertNull(AdLoadRetryPolicy.delayMillisFor(AdLoadRetryPolicy.MAX_ATTEMPTS))
    }

    @Test
    fun delayMillisFor_NegativeAttempt_ReturnsNull() {
        assertNull(AdLoadRetryPolicy.delayMillisFor(-1))
    }
}
