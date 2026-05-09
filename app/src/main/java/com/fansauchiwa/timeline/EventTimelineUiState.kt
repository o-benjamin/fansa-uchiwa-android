package com.fansauchiwa.timeline

sealed interface EventTimelineUiState {
    data object Loading : EventTimelineUiState

    data class Success(
        val events: List<EventTimelineEventUiModel>,
        val availableUchiwas: List<EventTimelineUchiwaUiModel>,
        val isSelectionMode: Boolean,
        val currentUchiwaId: String?
    ) : EventTimelineUiState

    data class Error(val message: String) : EventTimelineUiState
}
