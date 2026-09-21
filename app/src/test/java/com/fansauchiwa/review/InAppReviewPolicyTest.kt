package com.fansauchiwa.review

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InAppReviewPolicyTest {

    private val now = TimeUnit.DAYS.toMillis(1_000)
    private val installedLongAgo = now - TimeUnit.DAYS.toMillis(100)

    @Test
    fun shouldRequest_secondSaveAndNeverRequested_returnsTrue() {
        assertTrue(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 2,
                firstInstallTimeMillis = installedLongAgo,
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_firstSave_returnsFalse() {
        assertFalse(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 1,
                firstInstallTimeMillis = installedLongAgo,
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_zeroSaves_returnsFalse() {
        assertFalse(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 0,
                firstInstallTimeMillis = installedLongAgo,
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_installedLessThanThreeDaysAgo_returnsFalse() {
        assertFalse(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 5,
                firstInstallTimeMillis = now - TimeUnit.DAYS.toMillis(3) + 1,
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_installedExactlyThreeDaysAgo_returnsTrue() {
        assertTrue(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 2,
                firstInstallTimeMillis = now - TimeUnit.DAYS.toMillis(3),
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_installTimeUnknown_returnsFalse() {
        assertFalse(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 5,
                firstInstallTimeMillis = null,
                lastRequestedAtMillis = null,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_requestedWithinThirtyDays_returnsFalse() {
        assertFalse(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 5,
                firstInstallTimeMillis = installedLongAgo,
                lastRequestedAtMillis = now - TimeUnit.DAYS.toMillis(30) + 1,
                nowMillis = now
            )
        )
    }

    @Test
    fun shouldRequest_requestedExactlyThirtyDaysAgo_returnsTrue() {
        assertTrue(
            InAppReviewPolicy.shouldRequest(
                saveSuccessCount = 5,
                firstInstallTimeMillis = installedLongAgo,
                lastRequestedAtMillis = now - TimeUnit.DAYS.toMillis(30),
                nowMillis = now
            )
        )
    }
}
