package com.example.fansauchiwa.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fansauchiwa.data.Converters

@Database(entities = [FansaUchiwaEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FansaUchiwaDatabase : RoomDatabase() {
    abstract fun uchiwaDao(): FansaUchiwaDao
}