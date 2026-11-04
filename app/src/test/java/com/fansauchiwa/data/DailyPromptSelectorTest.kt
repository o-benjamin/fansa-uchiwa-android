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
        val scheduled = mapOf(
            today.minusDays(1) to DailyPrompt("yesterday", "昨日"),
            today to DailyPrompt("today", "今日")
        )

        assertEquals(DailyPrompt("today", "今日"), DailyPromptSelector.select(scheduled, today, defaults))
    }

    @Test
    fun select_scheduledLacksToday_returnsDefaultForToday() {
        val scheduled = mapOf(today.plusDays(1) to DailyPrompt("tomorrow", "明日"))

        assertEquals(
            DailyPromptSelector.select(null, today, defaults),
            DailyPromptSelector.select(scheduled, today, defaults)
        )
    }

    @Test
    fun select_scheduledEmpty_returnsNull() {
        assertNull(DailyPromptSelector.select(emptyMap(), today, defaults))
    }

    @Test
    fun select_scheduledNull_rotatesDefaultsByDay() {
        val picked = (0L until 4L).map { DailyPromptSelector.select(null, today.plusDays(it), defaults) }

        // 1日ずつ次の控えに進み、控えの数で一周する
        val start = defaults.indexOf(picked[0])
        assertEquals(
            (0 until 4).map { defaults[(start + it) % defaults.size] },
            picked
        )
    }

    @Test
    fun select_sameDay_returnsSameDefault() {
        assertEquals(
            DailyPromptSelector.select(null, today, defaults),
            DailyPromptSelector.select(null, LocalDate.of(2026, 10, 24), defaults)
        )
    }

    @Test
    fun select_dateBeforeEpoch_returnsDefaultWithoutCrash() {
        // 端末の日付が 1970 年より前（通し番号が負）でも、範囲内の控えを選ぶ
        val picked = DailyPromptSelector.select(null, LocalDate.of(1969, 12, 31), defaults)

        assertEquals(defaults[2], picked)
    }

    @Test
    fun select_defaultsEmpty_returnsNull() {
        assertNull(DailyPromptSelector.select(null, today, emptyList()))
    }
}
