package com.fansauchiwa.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPromptUchiwaRepositoryImplTest {

    @Test
    fun isStartedFromPrompt_marked_returnsTrue() {
        val repository = DailyPromptUchiwaRepositoryImpl()

        repository.markStartedFromPrompt("uchiwa-1")

        assertTrue(repository.isStartedFromPrompt("uchiwa-1"))
    }

    @Test
    fun isStartedFromPrompt_notMarked_returnsFalse() {
        val repository = DailyPromptUchiwaRepositoryImpl()

        repository.markStartedFromPrompt("uchiwa-1")

        assertFalse(repository.isStartedFromPrompt("uchiwa-2"))
    }
}
