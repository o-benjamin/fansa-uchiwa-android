package com.example.fansauchiwa.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fansa_uchiwa_data")
data class FansaUchiwaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var decorations: String
)