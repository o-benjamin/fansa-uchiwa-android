package com.fansauchiwa.data.source

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FansaUchiwaEntity::class], version = 1, exportSchema = false)
abstract class FansaUchiwaDatabase : RoomDatabase() {
    abstract fun uchiwaDao(): FansaUchiwaDao
}
