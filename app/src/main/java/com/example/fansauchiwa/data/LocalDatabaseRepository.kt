package com.example.fansauchiwa.data

import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

data class SavedUchiwa(
    val decorations: List<Decoration>,
    val uchiwaColorResId: Int,
    val backgroundColorResId: Int
)

interface LocalDatabaseRepository {
    suspend fun saveUchiwa(
        id: String,
        decorations: List<Decoration>,
        uchiwaColorResId: Int,
        backgroundColorResId: Int
    )

    suspend fun getUchiwa(id: String): SavedUchiwa?
    suspend fun deleteUchiwa(id: String)
}

class LocalDatabaseRepositoryImpl @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : LocalDatabaseRepository {

    private val converters = Converters()

    override suspend fun saveUchiwa(
        id: String,
        decorations: List<Decoration>,
        uchiwaColorResId: Int,
        backgroundColorResId: Int
    ) {
        val decorationsJson = converters.decorationsToJson(decorations)
        val fansaUchiwaEntity = FansaUchiwaEntity(
            id = id,
            decorations = decorationsJson,
            uchiwaColorResId = uchiwaColorResId,
            backgroundColorResId = backgroundColorResId
        )
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getUchiwa(id: String): SavedUchiwa? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            SavedUchiwa(
                decorations = converters.decorationsFromJson(it.decorations),
                uchiwaColorResId = it.uchiwaColorResId,
                backgroundColorResId = it.backgroundColorResId
            )
        }
    }

    override suspend fun deleteUchiwa(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }
}

