package com.fansauchiwa.update

import com.fansauchiwa.data.AvailableAppUpdate
import com.fansauchiwa.data.UpdateVersionCodes
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePromptPolicyTest {

    private val current = 36L
    private val staleUpdate = AvailableAppUpdate(
        availableVersionCode = (current + 1).toInt(),
        isImmediateAllowed = true,
        isFlexibleAllowed = true,
        stalenessDays = UpdatePromptPolicy.FLEXIBLE_MIN_STALENESS_DAYS
    )

    private fun decide(
        minVersionCode: Long = 0,
        forceVersionCode: Long = 0,
        availableUpdate: AvailableAppUpdate? = null
    ): UpdatePromptType = UpdatePromptPolicy.decide(
        currentVersionCode = current,
        versionCodes = UpdateVersionCodes(minVersionCode, forceVersionCode),
        availableUpdate = availableUpdate
    )

    @Test
    fun decide_noThresholdsAndNoUpdate_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide())
    }

    @Test
    fun decide_belowForceAndUpdateAvailable_returnsForce() {
        assertEquals(
            UpdatePromptType.FORCE,
            decide(minVersionCode = current + 1, forceVersionCode = current + 1, availableUpdate = staleUpdate)
        )
    }

    @Test
    fun decide_belowForceButPlayHasNoUpdate_returnsForceFallback() {
        assertEquals(
            UpdatePromptType.FORCE_FALLBACK,
            decide(forceVersionCode = current + 1, availableUpdate = null)
        )
    }

    @Test
    fun decide_belowForceButImmediateNotAllowed_returnsForceFallback() {
        assertEquals(
            UpdatePromptType.FORCE_FALLBACK,
            decide(forceVersionCode = current + 1, availableUpdate = staleUpdate.copy(isImmediateAllowed = false))
        )
    }

    @Test
    fun decide_belowForceButAvailableVersionOlderThanForce_returnsForceFallback() {
        assertEquals(
            UpdatePromptType.FORCE_FALLBACK,
            decide(
                forceVersionCode = current + 2,
                availableUpdate = staleUpdate.copy(availableVersionCode = (current + 1).toInt())
            )
        )
    }

    @Test
    fun decide_belowForceAndFlexibleNotAllowed_returnsForce() {
        assertEquals(
            UpdatePromptType.FORCE,
            decide(
                forceVersionCode = current + 1,
                availableUpdate = staleUpdate.copy(isFlexibleAllowed = false, stalenessDays = null)
            )
        )
    }

    @Test
    fun decide_forceEqualsCurrent_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(forceVersionCode = current))
    }

    @Test
    fun decide_belowMin_returnsMinEvenWithoutAvailableUpdate() {
        assertEquals(UpdatePromptType.MIN, decide(minVersionCode = current + 1, availableUpdate = null))
    }

    @Test
    fun decide_belowMinAndStaleUpdate_returnsMinRatherThanFlexible() {
        assertEquals(UpdatePromptType.MIN, decide(minVersionCode = current + 1, availableUpdate = staleUpdate))
    }

    @Test
    fun decide_minEqualsCurrent_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(minVersionCode = current))
    }

    @Test
    fun decide_aboveThresholdsAndStaleUpdate_returnsFlexible() {
        assertEquals(
            UpdatePromptType.FLEXIBLE,
            decide(minVersionCode = current - 1, forceVersionCode = current - 1, availableUpdate = staleUpdate)
        )
    }

    @Test
    fun decide_stalenessBelowThreshold_returnsNone() {
        assertEquals(
            UpdatePromptType.NONE,
            decide(
                availableUpdate = staleUpdate.copy(
                    stalenessDays = UpdatePromptPolicy.FLEXIBLE_MIN_STALENESS_DAYS - 1
                )
            )
        )
    }

    @Test
    fun decide_stalenessUnknown_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(availableUpdate = staleUpdate.copy(stalenessDays = null)))
    }

    @Test
    fun decide_staleButFlexibleNotAllowed_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(availableUpdate = staleUpdate.copy(isFlexibleAllowed = false)))
    }
}
