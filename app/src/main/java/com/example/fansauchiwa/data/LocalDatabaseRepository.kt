package com.example.fansauchiwa.data

import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

interface LocalDatabaseRepository {
    suspend fun saveDecorations(id: String, decorations: List<Decoration>)
    suspend fun getDecorations(id: String): List<Decoration>?
    suspend fun deleteDecorations(id: String)
}

class LocalDatabaseRepositoryImpl @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : LocalDatabaseRepository {

    private val converters = Converters()

    override suspend fun saveDecorations(id: String, decorations: List<Decoration>) {
        val decorationsJson = converters.decorationsToJson(decorations)
        val fansaUchiwaEntity = FansaUchiwaEntity(id = id, decorations = decorationsJson)
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getDecorations(id: String): List<Decoration>? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            converters.decorationsFromJson(it.decorations)
        }
    }

    override suspend fun deleteDecorations(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }
}

