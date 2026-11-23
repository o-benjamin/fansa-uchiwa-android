package com.fansauchiwa.analytics

import com.fansauchiwa.update.UpdatePromptType
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePromptAnalyticsParamsTest {

    @Test
    fun params_eachPromptType_returnsSource() {
        assertEquals(mapOf("source" to "flexible"), UpdatePromptAnalyticsParams.params(UpdatePromptType.FLEXIBLE))
        assertEquals(mapOf("source" to "min"), UpdatePromptAnalyticsParams.params(UpdatePromptType.MIN))
        assertEquals(
            mapOf("source" to "force_fallback"),
            UpdatePromptAnalyticsParams.params(UpdatePromptType.FORCE_FALLBACK)
        )
        assertEquals(mapOf("source" to "force"), UpdatePromptAnalyticsParams.params(UpdatePromptType.FORCE))
    }

    @Test
    fun params_none_returnsEmpty() {
        assertEquals(emptyMap<String, Any>(), UpdatePromptAnalyticsParams.params(UpdatePromptType.NONE))
    }
}
