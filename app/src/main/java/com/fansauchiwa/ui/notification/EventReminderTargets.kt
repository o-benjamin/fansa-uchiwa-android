package com.fansauchiwa.ui.notification

import com.fansauchiwa.data.source.EventWithUchiwas
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val EVENT_REMINDER_DAYS_THRESHOLD = 10

/**
 * リマインド通知の対象となるイベントを抽出する。
 * リマインドが有効で、うちわが紐付いており、開催日まで 0〜[EVENT_REMINDER_DAYS_THRESHOLD] 日のイベントが対象。
 */
fun selectReminderTargets(
    events: List<EventWithUchiwas>,
    today: LocalDate
): List<EventWithUchiwas> = events.filter { eventWithUchiwas ->
    val daysUntil = calculateDaysUntil(today, eventWithUchiwas.event.eventDateEpochDay)
    eventWithUchiwas.event.remindEnabled &&
        eventWithUchiwas.uchiwas.isNotEmpty() &&
        daysUntil in 0..EVENT_REMINDER_DAYS_THRESHOLD
}

fun calculateDaysUntil(today: LocalDate, eventDateEpochDay: Long): Int {
    return ChronoUnit.DAYS.between(
        today,
        LocalDate.ofEpochDay(eventDateEpochDay)
    ).toInt()
}
