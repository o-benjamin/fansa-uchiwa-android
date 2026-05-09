package com.fansauchiwa.timeline

import com.fansauchiwa.data.source.EventWithUchiwas

sealed interface EventTimelineUiState {
    data object Loading : EventTimelineUiState

    data class Success(
        val events: List<EventWithUchiwas>,
        val isSelectionMode: Boolean
    ) : EventTimelineUiState

    data class Error(val message: String) : EventTimelineUiState
}
