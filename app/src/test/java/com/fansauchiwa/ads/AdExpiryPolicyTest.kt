package com.fansauchiwa.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdExpiryPolicyTest {

    private val loadedAt = 10_000L

    @Test
    fun isExpired_JustLoaded_ReturnsFalse() {
        assertFalse(AdExpiryPolicy.isExpired(loadedAtMillis = loadedAt, nowMillis = loadedAt))
    }

    @Test
    fun isExpired_JustBeforeOneHour_ReturnsFalse() {
        val now = loadedAt + AdExpiryPolicy.MAX_AGE_MILLIS - 1

        assertFalse(AdExpiryPolicy.isExpired(loadedAtMillis = loadedAt, nowMillis = now))
    }

    @Test
    fun isExpired_ExactlyOneHour_ReturnsTrue() {
        val now = loadedAt + AdExpiryPolicy.MAX_AGE_MILLIS

        assertTrue(AdExpiryPolicy.isExpired(loadedAtMillis = loadedAt, nowMillis = now))
    }

    @Test
    fun isExpired_MoreThanOneHour_ReturnsTrue() {
        val now = loadedAt + AdExpiryPolicy.MAX_AGE_MILLIS * 3

        assertTrue(AdExpiryPolicy.isExpired(loadedAtMillis = loadedAt, nowMillis = now))
    }

    @Test
    fun isExpired_NowBeforeLoadedAt_ReturnsFalse() {
        assertFalse(AdExpiryPolicy.isExpired(loadedAtMillis = loadedAt, nowMillis = loadedAt - 1))
    }
}
