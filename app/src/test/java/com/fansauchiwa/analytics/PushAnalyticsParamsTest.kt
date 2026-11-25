package com.fansauchiwa.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class PushAnalyticsParamsTest {

    @Test
    fun params_withCampaign_returnsSource() {
        assertEquals(
            mapOf("source" to "2026-12-new-templates"),
            PushAnalyticsParams.params("2026-12-new-templates")
        )
    }

    @Test
    fun params_withoutCampaign_returnsEmpty() {
        assertEquals(emptyMap<String, Any>(), PushAnalyticsParams.params(null))
    }

    @Test
    fun params_blankCampaign_returnsEmpty() {
        assertEquals(emptyMap<String, Any>(), PushAnalyticsParams.params("  "))
    }

    @Test
    fun params_campaignWithSurroundingSpaces_trimsIt() {
        assertEquals(mapOf("source" to "spring"), PushAnalyticsParams.params(" spring "))
    }

    @Test
    fun params_tooLongCampaign_truncatesTo100Chars() {
        val source = PushAnalyticsParams.params("a".repeat(150))["source"] as String

        assertEquals(100, source.length)
    }
}
