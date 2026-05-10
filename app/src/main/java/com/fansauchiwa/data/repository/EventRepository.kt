package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.EventDataSource
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventUchiwaCrossRef
import com.fansauchiwa.data.source.EventWithUchiwas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface EventRepository {
    fun getEventsStream(): Flow<List<EventWithUchiwas>>

    suspend fun fetchEvents()

    suspend fun upsertEvent(event: EventEntity)

    suspend fun deleteEvent(eventId: String)

    suspend fun linkUchiwaToEvent(eventId: String, uchiwaId: String)

    suspend fun replaceEventUchiwas(eventId: String, uchiwaIds: List<String>)

    suspend fun updateEventThumbnail(eventId: String, thumbnailImagePath: String?)
}

class EventRepositoryImpl @Inject constructor(
    private val eventDataSource: EventDataSource
) : EventRepository {

    private val _eventsStream = MutableSharedFlow<List<EventWithUchiwas>>(replay = 1)

    override fun getEventsStream(): Flow<List<EventWithUchiwas>> = _eventsStream.asSharedFlow()

    override suspend fun fetchEvents() {
        val events = eventDataSource.getEventsStream().first()
        _eventsStream.emit(events)
    }

    override suspend fun upsertEvent(event: EventEntity) {
        eventDataSource.upsertEvent(event)
        fetchEvents()
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDataSource.deleteEvent(eventId)
        fetchEvents()
    }

    override suspend fun linkUchiwaToEvent(eventId: String, uchiwaId: String) {
        eventDataSource.insertEventUchiwaCrossRef(
            EventUchiwaCrossRef(
                eventId = eventId,
                uchiwaId = uchiwaId
            )
        )
        fetchEvents()
    }

    override suspend fun replaceEventUchiwas(eventId: String, uchiwaIds: List<String>) {
        eventDataSource.replaceEventUchiwaCrossRefs(
            eventId = eventId,
            crossRefs = uchiwaIds.distinct().map { uchiwaId ->
                EventUchiwaCrossRef(
                    eventId = eventId,
                    uchiwaId = uchiwaId
                )
            }
        )
        fetchEvents()
    }

    override suspend fun updateEventThumbnail(eventId: String, thumbnailImagePath: String?) {
        eventDataSource.updateEventThumbnail(eventId, thumbnailImagePath)
        fetchEvents()
    }
}
