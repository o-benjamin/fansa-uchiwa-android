package com.fansauchiwa.timeline

import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.repository.EventRepository
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EventTimelineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUuidProvider : UuidProvider {
        override fun generate(): String = "generated-event-id"
    }

    private class FakeEventRepository : EventRepository {
        private val eventsFlow = MutableSharedFlow<List<EventWithUchiwas>>(replay = 1)
        val savedEvents = mutableListOf<EventEntity>()
        val deletedEventIds = mutableListOf<String>()
        val linkedPairs = mutableListOf<Pair<String, String>>()

        override fun getEventsStream(): Flow<List<EventWithUchiwas>> = eventsFlow

        override suspend fun fetchEvents() {
            eventsFlow.emit(savedEvents.map { EventWithUchiwas(event = it, uchiwas = emptyList()) })
        }

        override suspend fun upsertEvent(event: EventEntity) {
            savedEvents.removeAll { it.id == event.id }
            savedEvents.add(event)
            fetchEvents()
        }

        override suspend fun deleteEvent(eventId: String) {
            deletedEventIds.add(eventId)
            savedEvents.removeAll { it.id == eventId }
            fetchEvents()
        }

        override suspend fun linkUchiwaToEvent(eventId: String, uchiwaId: String) {
            linkedPairs.add(eventId to uchiwaId)
            fetchEvents()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_uchiwaIdあり_SelectionModeになる() = runTest {
        val repository = FakeEventRepository()

        val viewModel = EventTimelineViewModel(
            eventRepository = repository,
            uuidProvider = FakeUuidProvider(),
            savedStateHandle = SavedStateHandle(mapOf(UCHIWA_ID_ARG to "uchiwa-1"))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EventTimelineUiState.Success)
        assertTrue((state as EventTimelineUiState.Success).isSelectionMode)
    }

    @Test
    fun saveEvent_新規作成かつSelectionMode_イベント保存後に紐付ける() = runTest {
        val repository = FakeEventRepository()
        val viewModel = EventTimelineViewModel(
            eventRepository = repository,
            uuidProvider = FakeUuidProvider(),
            savedStateHandle = SavedStateHandle(mapOf(UCHIWA_ID_ARG to "uchiwa-1"))
        )

        viewModel.saveEvent(
            eventId = null,
            name = "アリーナ公演",
            eventDate = LocalDate.of(2026, 8, 1),
            linkCurrentUchiwa = true
        )
        advanceUntilIdle()

        assertEquals(1, repository.savedEvents.size)
        assertEquals("generated-event-id", repository.savedEvents.first().id)
        assertEquals(listOf("generated-event-id" to "uchiwa-1"), repository.linkedPairs)
    }

    @Test
    fun deleteEvent_既存イベント_Repositoryに削除を委譲する() = runTest {
        val repository = FakeEventRepository().apply {
            savedEvents += EventEntity(
                id = "event-1",
                name = "フェス",
                eventDateEpochDay = LocalDate.of(2026, 9, 1).toEpochDay()
            )
        }
        val viewModel = EventTimelineViewModel(
            eventRepository = repository,
            uuidProvider = FakeUuidProvider(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.deleteEvent("event-1")
        advanceUntilIdle()

        assertEquals(listOf("event-1"), repository.deletedEventIds)
    }
}
