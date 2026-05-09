package com.fansauchiwa.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.repository.EventRepository
import com.fansauchiwa.data.source.EventEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class EventTimelineViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val uuidProvider: UuidProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uchiwaId: String? = savedStateHandle.get<String>(UCHIWA_ID_ARG)

    private val _uiState = MutableStateFlow<EventTimelineUiState>(EventTimelineUiState.Loading)
    val uiState: StateFlow<EventTimelineUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            runCatching {
                eventRepository.fetchEvents()
            }.onFailure { error ->
                _uiState.value = EventTimelineUiState.Error(
                    error.message ?: "Unknown error"
                )
            }
        }
    }

    private fun observeEvents() {
        eventRepository.getEventsStream()
            .onEach { events ->
                _uiState.value = EventTimelineUiState.Success(
                    events = events,
                    isSelectionMode = uchiwaId != null
                )
            }
            .launchIn(viewModelScope)
    }

    fun saveEvent(
        eventId: String?,
        name: String,
        eventDate: LocalDate,
        linkCurrentUchiwa: Boolean
    ) {
        viewModelScope.launch {
            runCatching {
                val resolvedEventId = eventId ?: uuidProvider.generate()
                eventRepository.upsertEvent(
                    EventEntity(
                        id = resolvedEventId,
                        name = name.trim(),
                        eventDateEpochDay = eventDate.toEpochDay()
                    )
                )
                if (linkCurrentUchiwa && uchiwaId != null) {
                    eventRepository.linkUchiwaToEvent(resolvedEventId, uchiwaId)
                }
            }.onFailure { error ->
                _uiState.value = EventTimelineUiState.Error(
                    error.message ?: "Unknown error"
                )
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            runCatching {
                eventRepository.deleteEvent(eventId)
            }.onFailure { error ->
                _uiState.value = EventTimelineUiState.Error(
                    error.message ?: "Unknown error"
                )
            }
        }
    }

    fun linkUchiwaToEvent(eventId: String) {
        val currentUchiwaId = uchiwaId ?: return
        viewModelScope.launch {
            runCatching {
                eventRepository.linkUchiwaToEvent(eventId, currentUchiwaId)
            }.onFailure { error ->
                _uiState.value = EventTimelineUiState.Error(
                    error.message ?: "Unknown error"
                )
            }
        }
    }
}
