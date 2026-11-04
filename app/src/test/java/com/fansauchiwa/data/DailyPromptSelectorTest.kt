package com.fansauchiwa.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyPromptSelectorTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 24)
    private val defaults = listOf(
        DailyPrompt("default_a", "A"),
        DailyPrompt("default_b", "B"),
        DailyPrompt("default_c", "C")
    )

    @Test
    fun select_scheduledHasToday_returnsScheduledPrompt() {
        val config = DailyPromptsConfig.Scheduled(
            mapOf(
                today.minusDays(1) to DailyPrompt("yesterday", "昨日"),
                today to DailyPrompt("today", "今日")
            )
        )

        assertEquals(DailyPrompt("today", "今日"), DailyPromptSelector.select(config, today, defaults))
    }

    @Test
    fun select_scheduledLacksToday_returnsDefaultForToday() {
        val config = DailyPromptsConfig.Scheduled(mapOf(today.plusDays(1) to DailyPrompt("tomorrow", "明日")))

        assertEquals(
            DailyPromptSelector.select(DailyPromptsConfig.Unset, today, defaults),
            DailyPromptSelector.select(config, today, defaults)
        )
    }

    @Test
    fun select_stopped_returnsNull() {
        assertNull(DailyPromptSelector.select(DailyPromptsConfig.Stopped, today, defaults))
    }

    @Test
    fun select_unset_rotatesDefaultsByDay() {
        val picked = (0L until 4L).map {
            DailyPromptSelector.select(DailyPromptsConfig.Unset, today.plusDays(it), defaults)
        }

        // 1日ずつ次の控えに進み、控えの数で一周する
        val start = defaults.indexOf(picked[0])
        assertEquals(
            (0 until 4).map { defaults[(start + it) % defaults.size] },
            picked
        )
    }

    @Test
    fun select_dateBeforeEpoch_wrapsToLastDefault() {
        // 1969-12-31 は通し番号 -1。3件で割った余り（mod）は 2 なので、最後の控えになる
        val picked = DailyPromptSelector.select(DailyPromptsConfig.Unset, LocalDate.of(1969, 12, 31), defaults)

        assertEquals(defaults[2], picked)
    }

    @Test
    fun select_defaultsEmpty_returnsNull() {
        assertNull(DailyPromptSelector.select(DailyPromptsConfig.Unset, today, emptyList()))
    }
}
