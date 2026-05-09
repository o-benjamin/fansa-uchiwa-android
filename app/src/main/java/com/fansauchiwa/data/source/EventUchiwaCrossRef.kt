package com.fansauchiwa.data.source

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_uchiwa_cross_refs",
    primaryKeys = ["eventId", "uchiwaId"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FansaUchiwaEntity::class,
            parentColumns = ["id"],
            childColumns = ["uchiwaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["eventId"]), Index(value = ["uchiwaId"])]
)
data class EventUchiwaCrossRef(
    val eventId: String,
    val uchiwaId: String
)
