package com.fansauchiwa.data.infra

import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow

interface LocalDatabaseDataSource {
    suspend fun upsertUchiwaData(uchiwaData: FansaUchiwaEntity)
    suspend fun getUchiwaById(id: String): FansaUchiwaEntity?
    suspend fun deleteUchiwaById(id: String)
    fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>>
}
