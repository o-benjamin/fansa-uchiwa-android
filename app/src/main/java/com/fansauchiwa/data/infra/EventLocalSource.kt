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
    override fun getEventsStream(): Flow<List<EventWithUchiwas>> =
        fansaUchiwaDao.getAllEventsWithUchiwasStream()

    override suspend fun upsertEvent(event: EventEntity) {
        fansaUchiwaDao.upsertEvent(event)
    }

    override suspend fun deleteEvent(eventId: String) {
        fansaUchiwaDao.deleteEventById(eventId)
    }

    override suspend fun insertEventUchiwaCrossRef(crossRef: EventUchiwaCrossRef) {
        fansaUchiwaDao.insertEventUchiwaCrossRef(crossRef)
    }
}
