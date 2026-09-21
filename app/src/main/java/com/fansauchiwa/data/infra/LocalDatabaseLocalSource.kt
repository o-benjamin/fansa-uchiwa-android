package com.fansauchiwa.data.infra

import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDatabaseLocalSource @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : LocalDatabaseDataSource {
    override suspend fun upsertUchiwaData(uchiwaData: FansaUchiwaEntity) {
        fansaUchiwaDao.upsertUchiwaData(uchiwaData)
    }

    override suspend fun getUchiwaById(id: String): FansaUchiwaEntity? {
        return fansaUchiwaDao.getUchiwaById(id)
    }

    override suspend fun deleteUchiwaById(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }

    override fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>> {
        return fansaUchiwaDao.getAllUchiwasStream()
    }
}
