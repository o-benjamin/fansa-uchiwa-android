package com.fansauchiwa.timeline

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import com.fansauchiwa.data.repository.EventRepository
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.ui.notification.UchiwaReminderNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
    private val masterpieceRepository: MasterpieceRepository,
    private val uuidProvider: UuidProvider,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uchiwaId: String? = savedStateHandle.get<String>(UCHIWA_ID_ARG)
    private var rawEvents: List<EventWithUchiwas> = emptyList()
    private var availableUchiwas: List<EventTimelineUchiwaUiModel> = emptyList()

    private val _uiState = MutableStateFlow<EventTimelineUiState>(EventTimelineUiState.Loading)
    val uiState: StateFlow<EventTimelineUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        fetchAvailableUchiwas()
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
                rawEvents = events
                publishSuccess()
            }
            .launchIn(viewModelScope)
    }

    private fun fetchAvailableUchiwas() {
        viewModelScope.launch {
            runCatching {
                availableUchiwas = masterpieceRepository.loadAllMasterpieces().map { imagePath ->
                    EventTimelineUchiwaUiModel(
                        id = extractUchiwaIdFromImagePath(imagePath),
                        imagePath = imagePath
                    )
                }
                publishSuccess()
            }.onFailure { error ->
                _uiState.value = EventTimelineUiState.Error(
                    error.message ?: "Unknown error"
                )
            }
        }
    }

    fun saveEvent(
        eventId: String?,
        name: String,
        eventDate: LocalDate,
        remindEnabled: Boolean,
        selectedUchiwaIds: Set<String>
    ) {
        viewModelScope.launch {
            runCatching {
                val resolvedEventId = eventId ?: uuidProvider.generate()
                eventRepository.upsertEvent(
                    EventEntity(
                        id = resolvedEventId,
                        name = name.trim(),
                        eventDateEpochDay = eventDate.toEpochDay(),
                        remindEnabled = remindEnabled
                    )
                )
                eventRepository.replaceEventUchiwas(resolvedEventId, selectedUchiwaIds.toList())
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

    fun updateEventThumbnail(eventId: String, thumbnailImagePath: String?) {
        viewModelScope.launch {
            runCatching {
                eventRepository.updateEventThumbnail(eventId, thumbnailImagePath)
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

    fun sendDebugReminder(event: EventTimelineEventUiModel) {
        UchiwaReminderNotifier.showReminder(
            context = context,
            eventId = event.id,
            eventName = event.name,
            daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), event.eventDate).toInt()
        )
    }

    private fun publishSuccess() {
        val availableUchiwasById = availableUchiwas.associateBy { it.id }
        _uiState.value = EventTimelineUiState.Success(
            events = rawEvents.map { eventWithUchiwas ->
                EventTimelineEventUiModel(
                    id = eventWithUchiwas.event.id,
                    name = eventWithUchiwas.event.name,
                    eventDate = LocalDate.ofEpochDay(eventWithUchiwas.event.eventDateEpochDay),
                    remindEnabled = eventWithUchiwas.event.remindEnabled,
                    linkedUchiwas = eventWithUchiwas.uchiwas.map { uchiwa ->
                        availableUchiwasById[uchiwa.id]
                            ?: EventTimelineUchiwaUiModel(
                                id = uchiwa.id,
                                imagePath = null
                            )
                    },
                    thumbnailImagePath = eventWithUchiwas.event.thumbnailImagePath
                )
            },
            availableUchiwas = availableUchiwas,
            isSelectionMode = uchiwaId != null,
            currentUchiwaId = uchiwaId
        )
    }

}
