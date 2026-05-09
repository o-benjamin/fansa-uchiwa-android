package com.fansauchiwa.data.source

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EventWithUchiwas(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventUchiwaCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "uchiwaId"
        )
    )
    val uchiwas: List<FansaUchiwaEntity>
)
