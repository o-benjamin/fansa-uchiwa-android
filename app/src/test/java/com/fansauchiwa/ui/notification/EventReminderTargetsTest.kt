package com.fansauchiwa.ui.notification

import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.data.source.FansaUchiwaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EventReminderTargetsTest {

    private val today = LocalDate.of(2026, 9, 19)

    private val uchiwa = FansaUchiwaEntity(
        id = "uchiwa-1",
        decorations = emptyList(),
        uchiwaColorValue = 0L,
        backgroundColorValue = 0L,
        overallBorderColorValue = 0L,
        overallBorderWidth = 0f,
        isOverallBorderPuffyEnabled = false
    )

    private fun event(
        id: String,
        daysFromToday: Long,
        remindEnabled: Boolean = true,
        uchiwas: List<FansaUchiwaEntity> = listOf(uchiwa)
    ) = EventWithUchiwas(
        event = EventEntity(
            id = id,
            name = "event-$id",
            eventDateEpochDay = today.plusDays(daysFromToday).toEpochDay(),
            remindEnabled = remindEnabled
        ),
        uchiwas = uchiwas
    )

    @Test
    fun selectReminderTargets_EventToday_IsTarget() {
        val events = listOf(event(id = "a", daysFromToday = 0))

        assertEquals(listOf("a"), selectReminderTargets(events, today).map { it.event.id })
    }

    @Test
    fun selectReminderTargets_EventInTenDays_IsTarget() {
        val events = listOf(event(id = "a", daysFromToday = 10))

        assertEquals(listOf("a"), selectReminderTargets(events, today).map { it.event.id })
    }

    @Test
    fun selectReminderTargets_EventInElevenDays_IsNotTarget() {
        val events = listOf(event(id = "a", daysFromToday = 11))

        assertTrue(selectReminderTargets(events, today).isEmpty())
    }

    @Test
    fun selectReminderTargets_EventYesterday_IsNotTarget() {
        val events = listOf(event(id = "a", daysFromToday = -1))

        assertTrue(selectReminderTargets(events, today).isEmpty())
    }

    @Test
    fun selectReminderTargets_RemindDisabled_IsNotTarget() {
        val events = listOf(event(id = "a", daysFromToday = 3, remindEnabled = false))

        assertTrue(selectReminderTargets(events, today).isEmpty())
    }

    @Test
    fun selectReminderTargets_NoLinkedUchiwa_IsNotTarget() {
        val events = listOf(event(id = "a", daysFromToday = 3, uchiwas = emptyList()))

        assertTrue(selectReminderTargets(events, today).isEmpty())
    }

    @Test
    fun selectReminderTargets_EmptyEvents_ReturnsEmpty() {
        assertTrue(selectReminderTargets(emptyList(), today).isEmpty())
    }

    @Test
    fun selectReminderTargets_MixedEvents_ReturnsOnlyTargetsInOrder() {
        val events = listOf(
            event(id = "past", daysFromToday = -1),
            event(id = "soon", daysFromToday = 2),
            event(id = "disabled", daysFromToday = 2, remindEnabled = false),
            event(id = "far", daysFromToday = 30),
            event(id = "limit", daysFromToday = 10)
        )

        assertEquals(
            listOf("soon", "limit"),
            selectReminderTargets(events, today).map { it.event.id }
        )
    }

    @Test
    fun calculateDaysUntil_SameDay_ReturnsZero() {
        assertEquals(0, calculateDaysUntil(today, today.toEpochDay()))
    }

    @Test
    fun calculateDaysUntil_AcrossYearBoundary_ReturnsDayDifference() {
        val lastDayOfYear = LocalDate.of(2026, 12, 31)

        assertEquals(1, calculateDaysUntil(lastDayOfYear, LocalDate.of(2027, 1, 1).toEpochDay()))
    }

    @Test
    fun calculateDaysUntil_PastDate_ReturnsNegative() {
        assertEquals(-5, calculateDaysUntil(today, today.minusDays(5).toEpochDay()))
    }
}
