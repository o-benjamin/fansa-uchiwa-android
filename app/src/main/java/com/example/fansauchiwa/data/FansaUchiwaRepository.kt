package com.example.fansauchiwa.data

import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

interface FansaUchiwaRepository {
    suspend fun saveDecorations(decorations: List<Decoration>)
    suspend fun getDecorations(id: Int): List<Decoration>?
}

class FansaUchiwaRepositoryImpl @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : FansaUchiwaRepository {

    private val converters = Converters()

    override suspend fun saveDecorations(decorations: List<Decoration>) {
        val decorationsJson = converters.decorationsToJson(decorations)
        val fansaUchiwaEntity = FansaUchiwaEntity(decorations = decorationsJson)
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getDecorations(id: Int): List<Decoration>? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            converters.decorationsFromJson(it.decorations)
        }
    }
}
