package com.fansauchiwa.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fansa_uchiwa_data")
data class FansaUchiwaEntity(
    @PrimaryKey val id: String,
    val decorations: String,
    val uchiwaColorValue: Long,
    val backgroundColorValue: Long
)
