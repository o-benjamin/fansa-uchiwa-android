package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import app.cash.turbine.test
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalDatabaseRepositoryImplTest {

    private lateinit var mockDao: FansaUchiwaDao
    private lateinit var repository: LocalDatabaseRepositoryImpl
    private val decorationConverters = Converters()

    @Before
    fun setUp() {
        mockDao = mockk<FansaUchiwaDao>()
        repository = LocalDatabaseRepositoryImpl(mockDao)
    }

    @Test
    fun saveUchiwa_domainModelIsSavedAsPersistenceModel() = runTest {
        var savedEntity: FansaUchiwaEntity? = null
        val uchiwa = Uchiwa(
            id = "uchiwa-1",
            decorations = listOf(
                Decoration.Image(id = "dec-1", imageId = "image-1")
            ),
            uchiwaColor = Color.Red,
            backgroundColor = Color.Blue
        )

        coEvery { mockDao.upsertUchiwaData(any()) } answers {
            savedEntity = firstArg()
        }

        repository.saveUchiwa(uchiwa)

        assertEquals(
            FansaUchiwaEntity(
                id = "uchiwa-1",
                decorations = decorationConverters.decorationsToJson(uchiwa.decorations),
                uchiwaColorValue = Color.Red.toColorLong(),
                backgroundColorValue = Color.Blue.toColorLong()
            ),
            savedEntity
        )
    }

    @Test
    fun getUchiwa_persistenceModelIsRestoredAsDomainModel() = runTest {
        val entity = FansaUchiwaEntity(
            id = "uchiwa-1",
            decorations = decorationConverters.decorationsToJson(
                listOf(Decoration.Image(id = "dec-1", imageId = "image-1"))
            ),
            uchiwaColorValue = Color.Red.toColorLong(),
            backgroundColorValue = Color.Blue.toColorLong()
        )

        coEvery { mockDao.getUchiwaById("uchiwa-1") } returns entity

        val result = repository.getUchiwa("uchiwa-1")

        assertEquals("uchiwa-1", result?.id)
        assertEquals(listOf(Decoration.Image(id = "dec-1", imageId = "image-1")), result?.decorations)
        assertEquals(Color.Red, result?.uchiwaColor)
        assertEquals(Color.Blue, result?.backgroundColor)
    }

    // region isImageUsedInAnyUchiwa

    @Test
    fun isImageUsedInAnyUchiwa_imageUsedInUchiwa_returnsTrue() = runTest {
        val targetImageId = "image-001"

        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-A",
                    decorations = decorationConverters.decorationsToJson(listOf(
                        Decoration.Image(id = "dec-1", imageId = targetImageId)
                    )),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                )
            )
        )

        val result = repository.isImageUsedInAnyUchiwa(targetImageId)

        assertTrue(result)
    }

    @Test
    fun isImageUsedInAnyUchiwa_imageNotUsed_returnsFalse() = runTest {
        val targetImageId = "image-001"

        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-A",
                    decorations = decorationConverters.decorationsToJson(listOf(
                        Decoration.Image(id = "dec-1", imageId = "image-999")
                    )),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                )
            )
        )

        val result = repository.isImageUsedInAnyUchiwa(targetImageId)

        assertFalse(result)
    }

    @Test
    fun isImageUsedInAnyUchiwa_multipleUchiwas_imageUsedInSecond_returnsTrue() = runTest {
        val targetImageId = "image-001"

        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-A",
                    decorations = decorationConverters.decorationsToJson(listOf(
                        Decoration.Image(id = "dec-1", imageId = "image-other")
                    )),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                ),
                FansaUchiwaEntity(
                    id = "uchiwa-B",
                    decorations = decorationConverters.decorationsToJson(listOf(
                        Decoration.Image(id = "dec-2", imageId = targetImageId)
                    )),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                )
            )
        )

        val result = repository.isImageUsedInAnyUchiwa(targetImageId)

        assertTrue(result)
    }

    @Test
    fun isImageUsedInAnyUchiwa_noUchiwasExist_returnsFalse() = runTest {
        every { mockDao.getAllUchiwasStream() } returns flowOf(emptyList())

        val result = repository.isImageUsedInAnyUchiwa("image-001")

        assertFalse(result)
    }

    @Test
    fun isImageUsedInAnyUchiwa_onlyNonImageDecorations_returnsFalse() = runTest {
        val targetImageId = "image-001"

        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-B",
                    decorations = decorationConverters.decorationsToJson(listOf(
                        Decoration.Sticker(id = "dec-sticker", label = "star")
                    )),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                )
            )
        )

        val result = repository.isImageUsedInAnyUchiwa(targetImageId)

        assertFalse(result)
    }

    @Test
    fun isImageUsedInAnyUchiwa_emptyDecorationsList_returnsFalse() = runTest {
        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-C",
                    decorations = decorationConverters.decorationsToJson(emptyList()),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L
                )
            )
        )

        val result = repository.isImageUsedInAnyUchiwa("image-001")

        assertFalse(result)
    }

    // endregion

    // region getAllUchiwasStream

    @Test
    fun getAllUchiwasStream_emitsEntitiesFromDao() = runTest {
        val entities = listOf(
            FansaUchiwaEntity(
                id = "uchiwa-1",
                decorations = decorationConverters.decorationsToJson(emptyList()),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            ),
            FansaUchiwaEntity(
                id = "uchiwa-2",
                decorations = decorationConverters.decorationsToJson(listOf(
                    Decoration.Sticker(id = "sticker-1", label = "heart")
                )),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L
            )
        )

        every { mockDao.getAllUchiwasStream() } returns flowOf(entities)

        repository.getAllUchiwasStream().test {
            val emitted = awaitItem()
            assertEquals(2, emitted.size)
            assertEquals("uchiwa-1", emitted[0].id)
            assertEquals("uchiwa-2", emitted[1].id)
            awaitComplete()
        }
    }

    // endregion
}
