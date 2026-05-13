package com.fansauchiwa.timeline

import java.time.LocalDate

data class EventTimelineEventUiModel(
    val id: String,
    val name: String,
    val eventDate: LocalDate,
    val remindEnabled: Boolean,
    val linkedUchiwas: List<EventTimelineUchiwaUiModel>,
    val thumbnailImagePath: String? = null
)
