package com.fansauchiwa.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollapsibleBannerLimiterTest {

    @Test
    fun tryAcquire_FirstRequestForPlacement_ReturnsTrue() {
        val limiter = CollapsibleBannerLimiter()

        assertTrue(limiter.tryAcquire("home_screen"))
    }

    @Test
    fun tryAcquire_SecondRequestForSamePlacement_ReturnsFalse() {
        val limiter = CollapsibleBannerLimiter()
        limiter.tryAcquire("home_screen")

        assertFalse(limiter.tryAcquire("home_screen"))
    }

    @Test
    fun tryAcquire_OtherPlacementAfterFirst_ReturnsTrue() {
        val limiter = CollapsibleBannerLimiter()
        limiter.tryAcquire("home_screen")

        assertTrue(limiter.tryAcquire("event_timeline_screen"))
    }

    @Test
    fun tryAcquire_EmptyPlacement_ReturnsTrueOnlyOnce() {
        val limiter = CollapsibleBannerLimiter()

        assertTrue(limiter.tryAcquire(""))
        assertFalse(limiter.tryAcquire(""))
    }
}
