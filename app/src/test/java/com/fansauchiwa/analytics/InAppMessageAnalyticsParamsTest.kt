package com.fansauchiwa.analytics

import com.fansauchiwa.data.InAppMessageTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class InAppMessageAnalyticsParamsTest {

    @Test
    fun params_eachTarget_sendsTargetValue() {
        assertEquals(mapOf("target" to "store"), InAppMessageAnalyticsParams.params(InAppMessageTarget.STORE))
        assertEquals(mapOf("target" to "web"), InAppMessageAnalyticsParams.params(InAppMessageTarget.WEB))
        assertEquals(mapOf("target" to "home"), InAppMessageAnalyticsParams.params(InAppMessageTarget.HOME))
        assertEquals(
            mapOf("target" to "daily_prompt"),
            InAppMessageAnalyticsParams.params(InAppMessageTarget.DAILY_PROMPT)
        )
        assertEquals(
            mapOf("target" to "ai_material"),
            InAppMessageAnalyticsParams.params(InAppMessageTarget.AI_MATERIAL)
        )
        assertEquals(mapOf("target" to "other"), InAppMessageAnalyticsParams.params(InAppMessageTarget.OTHER))
    }
}
