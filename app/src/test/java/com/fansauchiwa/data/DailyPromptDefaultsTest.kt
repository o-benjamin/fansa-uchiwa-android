package com.fansauchiwa.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPromptDefaultsTest {

    @Test
    fun prompts_all_passRemoteConfigTextRules() {
        val rejected = DailyPromptDefaults.PROMPTS.filterNot { DailyPromptsParser.isAllowedText(it.text) }

        assertEquals(emptyList<DailyPrompt>(), rejected)
    }

    @Test
    fun prompts_ids_areUniqueAndPrefixed() {
        val ids = DailyPromptDefaults.PROMPTS.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.startsWith("default_") })
    }

    @Test
    fun prompts_texts_areUnique() {
        val texts = DailyPromptDefaults.PROMPTS.map { it.text }

        assertEquals(texts.size, texts.toSet().size)
    }

    @Test
    fun prompts_count_coversAboutAMonth() {
        // 取得できない日が続いても、約1か月は同じお題が出ない
        assertTrue(DailyPromptDefaults.PROMPTS.size >= 28)
    }
}
