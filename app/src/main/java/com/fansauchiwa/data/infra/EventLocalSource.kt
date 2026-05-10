package com.fansauchiwa.data.infra

import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventUchiwaCrossRef
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.data.source.FansaUchiwaDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventLocalSource @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : EventDataSource {
    override fun getEventsStream(): Flow<List<EventWithUchiwas>> = observeEvents()

    override suspend fun upsertEvent(event: EventEntity) {
        persistEvent(event)
    }

    override suspend fun deleteEvent(eventId: String) {
        deletePersistedEvent(eventId)
    }

    override suspend fun insertEventUchiwaCrossRef(crossRef: EventUchiwaCrossRef) {
        persistEventUchiwaCrossRef(crossRef)
    }

    override suspend fun replaceEventUchiwaCrossRefs(
        eventId: String,
        crossRefs: List<EventUchiwaCrossRef>
    ) {
        replacePersistedEventUchiwaCrossRefs(
            eventId = eventId,
            crossRefs = crossRefs
        )
    }

    override suspend fun updateEventThumbnail(eventId: String, thumbnailImagePath: String?) {
        updatePersistedEventThumbnail(
            eventId = eventId,
            thumbnailImagePath = thumbnailImagePath
        )
    }

    private fun observeEvents(): Flow<List<EventWithUchiwas>> =
        fansaUchiwaDao.getAllEventsWithUchiwasStream()

    private suspend fun persistEvent(event: EventEntity) {
        fansaUchiwaDao.upsertEvent(event)
    }

    private suspend fun deletePersistedEvent(eventId: String) {
        fansaUchiwaDao.deleteEventById(eventId)
    }

    private suspend fun persistEventUchiwaCrossRef(crossRef: EventUchiwaCrossRef) {
        fansaUchiwaDao.insertEventUchiwaCrossRef(crossRef)
    }

    private suspend fun replacePersistedEventUchiwaCrossRefs(
        eventId: String,
        crossRefs: List<EventUchiwaCrossRef>
    ) {
        fansaUchiwaDao.deleteEventUchiwaCrossRefsByEventId(eventId)

        val normalizedCrossRefs = crossRefs.distinct()
        if (normalizedCrossRefs.isEmpty()) {
            return
        }

        fansaUchiwaDao.insertEventUchiwaCrossRefs(normalizedCrossRefs)
    }

    private suspend fun updatePersistedEventThumbnail(
        eventId: String,
        thumbnailImagePath: String?
    ) {
        fansaUchiwaDao.updateEventThumbnail(eventId, thumbnailImagePath)
    }
}
