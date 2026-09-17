package com.fansauchiwa.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.infra.LocalDatabaseDataSource
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
    private val localDatabaseDataSource: LocalDatabaseDataSource
) : LocalDatabaseRepository {

    override suspend fun saveUchiwa(uchiwa: Uchiwa) {
        val fansaUchiwaEntity = FansaUchiwaEntity(
            id = uchiwa.id,
            decorations = uchiwa.decorations,
            uchiwaColorValue = uchiwa.uchiwaColor.toColorLong(),
            backgroundColorValue = uchiwa.backgroundColor.toColorLong(),
            overallBorderColorValue = uchiwa.overallBorderColor.toColorLong(),
            overallBorderWidth = uchiwa.overallBorderWidth,
            isOverallBorderPuffyEnabled = uchiwa.isOverallBorderPuffyEnabled
        )
        return localDatabaseDataSource.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getUchiwa(id: String): Uchiwa? {
        val uchiwaData = localDatabaseDataSource.getUchiwaById(id)
        return uchiwaData?.let {
            Uchiwa(
                id = it.id,
                decorations = it.decorations,
                uchiwaColor = Color(it.uchiwaColorValue.toULong()),
                backgroundColor = Color(it.backgroundColorValue.toULong()),
                overallBorderColor = Color(it.overallBorderColorValue.toULong()),
                overallBorderWidth = it.overallBorderWidth,
                isOverallBorderPuffyEnabled = it.isOverallBorderPuffyEnabled
            )
        }
    }

    override suspend fun deleteUchiwa(id: String) {
        localDatabaseDataSource.deleteUchiwaById(id)
    }

    override fun getAllUchiwasStream(): Flow<List<Uchiwa>> {
        return localDatabaseDataSource.getAllUchiwasStream().map { entities ->
            entities.map { entity ->
                Uchiwa(
                    id = entity.id,
                    decorations = entity.decorations,
                    uchiwaColor = Color(entity.uchiwaColorValue.toULong()),
                    backgroundColor = Color(entity.backgroundColorValue.toULong()),
                    overallBorderColor = Color(entity.overallBorderColorValue.toULong()),
                    overallBorderWidth = entity.overallBorderWidth,
                    isOverallBorderPuffyEnabled = entity.isOverallBorderPuffyEnabled
                )
            }
        }
    }

    override suspend fun isImageUsedInAnyUchiwa(imageId: String): Boolean {
        val allUchiwas = localDatabaseDataSource.getAllUchiwasStream().first()
        return allUchiwas.any { uchiwa ->
            uchiwa.decorations.any { decoration ->
                decoration is Decoration.Image && decoration.imageId == imageId
            }
        }
    }
}
