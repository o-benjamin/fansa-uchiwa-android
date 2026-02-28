package com.fansauchiwa.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FansaUchiwaDao {
    @Upsert
    suspend fun upsertUchiwaData(uchiwaData: FansaUchiwaEntity)

    @Query("SELECT * FROM fansa_uchiwa_data WHERE id = :id")
    suspend fun getUchiwaById(id: String): FansaUchiwaEntity?

    @Query("DELETE FROM fansa_uchiwa_data WHERE id = :id")
    suspend fun deleteUchiwaById(id: String)

    @Query("SELECT * FROM fansa_uchiwa_data")
    fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>>
}