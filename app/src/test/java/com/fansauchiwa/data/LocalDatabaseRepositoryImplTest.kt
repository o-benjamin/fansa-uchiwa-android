package com.fansauchiwa.data

import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalDatabaseRepositoryImplTest {

    private lateinit var fakeDao: FakeFansaUchiwaDao
    private lateinit var repository: LocalDatabaseRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeFansaUchiwaDao()
        repository = LocalDatabaseRepositoryImpl(fakeDao)
    }

    @Test
    fun isImageUsedInOtherUchiwas_imageUsedInOtherUchiwa_returnsTrue() = runBlocking {
        val targetImageId = "image-001"
        val currentUchiwaId = "uchiwa-A"

        fakeDao.upsertUchiwaData(
            FansaUchiwaEntity(
                id = currentUchiwaId,
                decorations = listOf(
                    Decoration.Image(id = "dec-1", imageId = targetImageId)
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )
        fakeDao.upsertUchiwaData(
            FansaUchiwaEntity(
                id = "uchiwa-B",
                decorations = listOf(
                    Decoration.Image(id = "dec-2", imageId = targetImageId)
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )

        val result = repository.isImageUsedInOtherUchiwas(targetImageId, currentUchiwaId)

        assertTrue(result)
    }

    @Test
    fun isImageUsedInOtherUchiwas_imageOnlyInCurrentUchiwa_returnsFalse() = runBlocking {
        val targetImageId = "image-001"
        val currentUchiwaId = "uchiwa-A"

        fakeDao.upsertUchiwaData(
            FansaUchiwaEntity(
                id = currentUchiwaId,
                decorations = listOf(
                    Decoration.Image(id = "dec-1", imageId = targetImageId)
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )

        val result = repository.isImageUsedInOtherUchiwas(targetImageId, currentUchiwaId)

        assertFalse(result)
    }

    @Test
    fun isImageUsedInOtherUchiwas_imageNotUsedAnywhere_returnsFalse() = runBlocking {
        val targetImageId = "image-001"
        val currentUchiwaId = "uchiwa-A"

        fakeDao.upsertUchiwaData(
            FansaUchiwaEntity(
                id = "uchiwa-B",
                decorations = listOf(
                    Decoration.Image(id = "dec-2", imageId = "image-999")
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )

        val result = repository.isImageUsedInOtherUchiwas(targetImageId, currentUchiwaId)

        assertFalse(result)
    }

    @Test
    fun isImageUsedInOtherUchiwas_noUchiwasExist_returnsFalse() = runBlocking {
        val result = repository.isImageUsedInOtherUchiwas("image-001", "uchiwa-A")

        assertFalse(result)
    }

    @Test
    fun isImageUsedInOtherUchiwas_otherUchiwaHasNonImageDecorations_returnsFalse() = runBlocking {
        val targetImageId = "image-001"
        val currentUchiwaId = "uchiwa-A"

        fakeDao.upsertUchiwaData(
            FansaUchiwaEntity(
                id = "uchiwa-B",
                decorations = listOf(
                    Decoration.Sticker(id = "dec-sticker", label = "star")
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )

        val result = repository.isImageUsedInOtherUchiwas(targetImageId, currentUchiwaId)

        assertFalse(result)
    }
}

private class FakeFansaUchiwaDao : FansaUchiwaDao {

    private val uchiwas = mutableMapOf<String, FansaUchiwaEntity>()
    private val flow = MutableStateFlow<List<FansaUchiwaEntity>>(emptyList())

    override suspend fun upsertUchiwaData(uchiwaData: FansaUchiwaEntity) {
        uchiwas[uchiwaData.id] = uchiwaData
        flow.value = uchiwas.values.toList()
    }

    override suspend fun getUchiwaById(id: String): FansaUchiwaEntity? {
        return uchiwas[id]
    }

    override suspend fun deleteUchiwaById(id: String) {
        uchiwas.remove(id)
        flow.value = uchiwas.values.toList()
    }

    override fun getAllUchiwasStream(): Flow<List<FansaUchiwaEntity>> {
        return flow
    }
}

