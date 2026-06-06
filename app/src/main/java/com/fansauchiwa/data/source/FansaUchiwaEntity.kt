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
    // 既存DBの移行直後に未設定レコードも読み出せるよう、デフォルト値を持たせる。
    val overallBorderColorValue: Long = 0L,
    val overallBorderWidth: Float = 0f
)
