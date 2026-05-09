package com.fansauchiwa.data.infra

import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventUchiwaCrossRef
import com.fansauchiwa.data.source.EventWithUchiwas
import kotlinx.coroutines.flow.Flow

interface EventDataSource {
    fun getEventsStream(): Flow<List<EventWithUchiwas>>

    suspend fun upsertEvent(event: EventEntity)

    suspend fun deleteEvent(eventId: String)

    suspend fun insertEventUchiwaCrossRef(crossRef: EventUchiwaCrossRef)

    suspend fun replaceEventUchiwaCrossRefs(eventId: String, crossRefs: List<EventUchiwaCrossRef>)
}
