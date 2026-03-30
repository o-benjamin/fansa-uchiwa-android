package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface LocalDatabaseRepository {
    suspend fun saveUchiwa(
        id: String,
        decorations: List<Decoration>,
        uchiwaColor: Color,
        backgroundColor: Color
    )

    suspend fun getUchiwa(id: String): SavedUchiwa?
    suspend fun deleteUchiwa(id: String)
    fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>>
    suspend fun isImageUsedInAnyUchiwa(imageId: String): Boolean
}

class LocalDatabaseRepositoryImpl @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : LocalDatabaseRepository {

    override suspend fun saveUchiwa(
        id: String,
        decorations: List<Decoration>,
        uchiwaColor: Color,
        backgroundColor: Color
    ) {
        val fansaUchiwaEntity = FansaUchiwaEntity(
            id = id,
            decorations = decorations,
            uchiwaColorValue = uchiwaColor.toColorLong(),
            backgroundColorValue = backgroundColor.toColorLong()
        )
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getUchiwa(id: String): SavedUchiwa? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            SavedUchiwa(
                decorations = it.decorations,
                uchiwaColor = Color(it.uchiwaColorValue.toULong()),
                backgroundColor = Color(it.backgroundColorValue.toULong())
            )
        }
    }

    override suspend fun deleteUchiwa(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }

    override fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>> {
        return fansaUchiwaDao.getAllUchiwasStream()
    }

    override suspend fun isImageUsedInAnyUchiwa(imageId: String): Boolean {
        val allUchiwas = fansaUchiwaDao.getAllUchiwasStream().first()
        return allUchiwas.any { uchiwa ->
            uchiwa.decorations.any { decoration ->
                decoration is Decoration.Image && decoration.imageId == imageId
            }
        }
    }
}

