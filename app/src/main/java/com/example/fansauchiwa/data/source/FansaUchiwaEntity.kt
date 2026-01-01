package com.example.fansauchiwa.data.source

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fansauchiwa.data.Decoration

@Entity(tableName = "fansa_uchiwa_data")
data class FansaUchiwaEntity(
    @PrimaryKey val id: String,
    var decorations: List<Decoration>,
    var uchiwaColorValue: Long,
    var backgroundColorValue: Long
) {
    val uchiwaColor: Color
        get() = Color(uchiwaColorValue.toULong())
    val backgroundColor: Color
        get() = Color(backgroundColorValue.toULong())
}
