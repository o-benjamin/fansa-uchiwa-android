package com.example.fansauchiwa.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FansaUchiwaDao {
    @Upsert
    suspend fun upsertUchiwaData(uchiwaData: FansaUchiwaEntity)

    @Query("SELECT * FROM fansa_uchiwa_data WHERE id = :id")
    suspend fun getUchiwaById(id: Int): FansaUchiwaEntity?
}