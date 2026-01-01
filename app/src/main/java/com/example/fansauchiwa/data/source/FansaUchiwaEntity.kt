package com.example.fansauchiwa.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fansa_uchiwa_data")
data class FansaUchiwaEntity(
    @PrimaryKey val id: String,
    var decorations: String,
    var uchiwaColorResId: Int,
    var backgroundColorResId: Int
)