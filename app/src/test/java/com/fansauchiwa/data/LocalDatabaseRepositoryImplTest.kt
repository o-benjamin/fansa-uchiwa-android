package com.fansauchiwa.data

import app.cash.turbine.test
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaEntity
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

    @Before
    fun setUp() {
        mockDao = mockk<FansaUchiwaDao>()
        repository = LocalDatabaseRepositoryImpl(mockDao)
    }

    // region isImageUsedInAnyUchiwa

    @Test
    fun isImageUsedInAnyUchiwa_imageUsedInUchiwa_returnsTrue() = runTest {
        val targetImageId = "image-001"

        every { mockDao.getAllUchiwasStream() } returns flowOf(
            listOf(
                FansaUchiwaEntity(
                    id = "uchiwa-A",
                    decorations = listOf(
                        Decoration.Image(id = "dec-1", imageId = targetImageId)
                    ),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
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
                    decorations = listOf(
                        Decoration.Image(id = "dec-1", imageId = "image-999")
                    ),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
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
                    decorations = listOf(
                        Decoration.Image(id = "dec-1", imageId = "image-other")
                    ),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
                ),
                FansaUchiwaEntity(
                    id = "uchiwa-B",
                    decorations = listOf(
                        Decoration.Image(id = "dec-2", imageId = targetImageId)
                    ),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
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
                    decorations = listOf(
                        Decoration.Sticker(id = "dec-sticker", label = "star")
                    ),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
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
                    decorations = emptyList(),
                    uchiwaColorValue = 0L,
                    backgroundColorValue = 0L,
                    overallBorderColorValue = 0L,
                    overallBorderWidth = 0f,
                    isOverallBorderPuffyEnabled = false
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
                decorations = emptyList(),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L,
                overallBorderColorValue = 0L,
                overallBorderWidth = 0f,
                isOverallBorderPuffyEnabled = false
            ),
            FansaUchiwaEntity(
                id = "uchiwa-2",
                decorations = listOf(
                    Decoration.Sticker(id = "sticker-1", label = "heart")
                ),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L,
                overallBorderColorValue = 0L,
                overallBorderWidth = 0f,
                isOverallBorderPuffyEnabled = false
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
