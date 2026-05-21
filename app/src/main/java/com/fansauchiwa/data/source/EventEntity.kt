package com.fansauchiwa.data.source

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [Index(value = ["eventDateEpochDay"])]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val eventDateEpochDay: Long,
    @ColumnInfo(defaultValue = "1") val remindEnabled: Boolean,
    val thumbnailImagePath: String? = null
)
