package com.example.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
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
                uchiwaColor = it.uchiwaColor,
                backgroundColor = it.backgroundColor
            )
        }
    }

    override suspend fun deleteUchiwa(id: String) {
        fansaUchiwaDao.deleteUchiwaById(id)
    }
}

