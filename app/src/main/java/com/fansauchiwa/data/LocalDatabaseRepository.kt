package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val decorationConverters = Converters()

private fun Uchiwa.toEntity(): FansaUchiwaEntity {
    return FansaUchiwaEntity(
        id = id,
        decorations = decorationConverters.decorationsToJson(decorations),
        uchiwaColorValue = uchiwaColor.toColorLong(),
        backgroundColorValue = backgroundColor.toColorLong()
    )
}

private fun FansaUchiwaEntity.toUchiwa(): Uchiwa {
    return Uchiwa(
        id = id,
        decorations = decorationConverters.decorationsFromJson(decorations),
        uchiwaColor = Color(uchiwaColorValue.toULong()),
        backgroundColor = Color(backgroundColorValue.toULong())
    )
}

private fun FansaUchiwaEntity.toDecorations(): List<Decoration> {
    return decorationConverters.decorationsFromJson(decorations)
}

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
        fansaUchiwaDao.upsertUchiwaData(uchiwa.toEntity())
    }

    override suspend fun getUchiwa(id: String): Uchiwa? {
        return fansaUchiwaDao.getUchiwaById(id)?.toUchiwa()
    }

    override suspend fun deleteUchiwa(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }

    override fun getAllUchiwasStream(): Flow<List<Uchiwa>> {
        return fansaUchiwaDao.getAllUchiwasStream().map { entities -> entities.map(FansaUchiwaEntity::toUchiwa) }
    }

    override suspend fun isImageUsedInAnyUchiwa(imageId: String): Boolean {
        val allUchiwas = fansaUchiwaDao.getAllUchiwasStream().first()
        return allUchiwas.any { uchiwa ->
            uchiwa.toDecorations().any { decoration ->
                decoration is Decoration.Image && decoration.imageId == imageId
            }
        }
    }
}
