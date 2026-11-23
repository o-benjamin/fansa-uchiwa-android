package com.fansauchiwa.update

import com.fansauchiwa.data.PlayAppUpdateStatus
import com.fansauchiwa.data.UpdateVersionCodes
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePromptPolicyTest {

    private val current = 36L
    private val staleUpdate = PlayAppUpdateStatus(
        isFlexibleAllowed = true,
        stalenessDays = UpdatePromptPolicy.FLEXIBLE_MIN_STALENESS_DAYS
    )

    private fun decide(
        minVersionCode: Long = 0,
        forceVersionCode: Long = 0,
        playUpdate: PlayAppUpdateStatus? = null
    ): UpdatePromptType = UpdatePromptPolicy.decide(
        currentVersionCode = current,
        versionCodes = UpdateVersionCodes(minVersionCode, forceVersionCode),
        playUpdate = playUpdate
    )

    @Test
    fun decide_noThresholdsAndNoUpdate_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide())
    }

    @Test
    fun decide_belowForceAndUpdateAvailable_returnsForce() {
        assertEquals(
            UpdatePromptType.FORCE,
            decide(minVersionCode = current + 1, forceVersionCode = current + 1, playUpdate = staleUpdate)
        )
    }

    @Test
    fun decide_belowForceButPlayHasNoUpdate_returnsMin() {
        assertEquals(UpdatePromptType.MIN, decide(forceVersionCode = current + 1, playUpdate = null))
    }

    @Test
    fun decide_belowForceAndFlexibleNotAllowed_returnsForce() {
        assertEquals(
            UpdatePromptType.FORCE,
            decide(
                forceVersionCode = current + 1,
                playUpdate = PlayAppUpdateStatus(isFlexibleAllowed = false, stalenessDays = null)
            )
        )
    }

    @Test
    fun decide_forceEqualsCurrent_isNotForced() {
        assertEquals(UpdatePromptType.NONE, decide(forceVersionCode = current))
    }

    @Test
    fun decide_belowMin_returnsMinEvenWithoutPlayUpdate() {
        assertEquals(UpdatePromptType.MIN, decide(minVersionCode = current + 1, playUpdate = null))
    }

    @Test
    fun decide_belowMinAndStaleUpdate_returnsMinRatherThanFlexible() {
        assertEquals(UpdatePromptType.MIN, decide(minVersionCode = current + 1, playUpdate = staleUpdate))
    }

    @Test
    fun decide_minEqualsCurrent_isNotPrompted() {
        assertEquals(UpdatePromptType.NONE, decide(minVersionCode = current))
    }

    @Test
    fun decide_aboveThresholdsAndStaleUpdate_returnsFlexible() {
        assertEquals(
            UpdatePromptType.FLEXIBLE,
            decide(minVersionCode = current - 1, forceVersionCode = current - 1, playUpdate = staleUpdate)
        )
    }

    @Test
    fun decide_updateNewerThanStalenessThreshold_returnsNone() {
        assertEquals(
            UpdatePromptType.NONE,
            decide(
                playUpdate = staleUpdate.copy(
                    stalenessDays = UpdatePromptPolicy.FLEXIBLE_MIN_STALENESS_DAYS - 1
                )
            )
        )
    }

    @Test
    fun decide_stalenessUnknown_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(playUpdate = staleUpdate.copy(stalenessDays = null)))
    }

    @Test
    fun decide_staleButFlexibleNotAllowed_returnsNone() {
        assertEquals(UpdatePromptType.NONE, decide(playUpdate = staleUpdate.copy(isFlexibleAllowed = false)))
    }
}
