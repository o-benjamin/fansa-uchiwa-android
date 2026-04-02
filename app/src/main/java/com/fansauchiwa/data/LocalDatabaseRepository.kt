package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface LocalDatabaseRepository {
    suspend fun saveUchiwa(uchiwa: Uchiwa)
    suspend fun getUchiwa(id: String): Uchiwa?
    suspend fun deleteUchiwa(id: String)
    fun getAllUchiwasStream(): Flow<List<Uchiwa>>
    suspend fun isImageUsedInAnyUchiwa(imageId: String): Boolean
}

class LocalDatabaseRepositoryImpl @Inject constructor(
    private val fansaUchiwaDao: FansaUchiwaDao
) : LocalDatabaseRepository {

    override suspend fun saveUchiwa(uchiwa: Uchiwa) {
        val fansaUchiwaEntity = FansaUchiwaEntity(
            id = uchiwa.id,
            decorations = uchiwa.decorations,
            uchiwaColorValue = uchiwa.uchiwaColor.toColorLong(),
            backgroundColorValue = uchiwa.backgroundColor.toColorLong()
        )
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getUchiwa(id: String): Uchiwa? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            Uchiwa(
                id = it.id,
                decorations = it.decorations,
                uchiwaColor = Color(it.uchiwaColorValue.toULong()),
                backgroundColor = Color(it.backgroundColorValue.toULong())
            )
        }
    }

    override suspend fun deleteUchiwa(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }

    override fun getAllUchiwasStream(): Flow<List<Uchiwa>> {
        return fansaUchiwaDao.getAllUchiwasStream().map { entities ->
            entities.map { entity ->
                Uchiwa(
                    id = entity.id,
                    decorations = entity.decorations,
                    uchiwaColor = Color(entity.uchiwaColorValue.toULong()),
                    backgroundColor = Color(entity.backgroundColorValue.toULong())
                )
            }
        }
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
