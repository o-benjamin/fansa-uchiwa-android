package com.fansauchiwa.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fansauchiwa.data.Decoration

@Entity(tableName = "fansa_uchiwa_data")
data class FansaUchiwaEntity(
    @PrimaryKey val id: String,
    val decorations: List<Decoration>,
    val uchiwaColorValue: Long,
    val backgroundColorValue: Long,
    val overallBorderColorValue: Long,
    val overallBorderWidth: Float,
    val isOverallBorderPuffyEnabled: Boolean
)
