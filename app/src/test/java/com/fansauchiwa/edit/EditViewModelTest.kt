package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.repository.AnalyticsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var localImageRepository: LocalImageRepository
    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        localImageRepository = mockk(relaxed = true)
        localDatabaseRepository = mockk(relaxed = true)
        masterpieceRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(uchiwaId: String?): EditViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (uchiwaId != null) {
                set(UCHIWA_ID_ARG, uchiwaId)
            }
        }
        return EditViewModel(
            localImageRepository = localImageRepository,
            localDatabaseRepository = localDatabaseRepository,
            masterpieceRepository = masterpieceRepository,
            analyticsRepository = analyticsRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `loadExistingDecorations removes missing images and keeps valid ones`() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val validImageId = "valid-image-id"
        val missingImageId = "missing-image-id"
        val textDecorationId = "text-decoration-id"

        val textDecoration = Decoration.Text(
            id = textDecorationId,
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val validImageDecoration = Decoration.Image(
            id = "img-decoration-1",
            imageId = validImageId
        )
        val missingImageDecoration = Decoration.Image(
            id = "img-decoration-2",
            imageId = missingImageId
        )

        val savedUchiwa = SavedUchiwa(
            decorations = listOf(textDecoration, validImageDecoration, missingImageDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        val validImageReference = ImageReference(
            id = validImageId,
            path = "/path/to/valid/image.png"
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.loadImage(validImageId) } returns validImageReference
        every { localImageRepository.loadImage(missingImageId) } returns null
        every { localImageRepository.getAllImages() } returns listOf(validImageReference)

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        // decorationsから存在しない画像が除外されていること
        assertEquals(2, state.decorations.size)
        assertTrue(state.decorations.any { it.id == textDecorationId })
        assertTrue(state.decorations.any { it.id == "img-decoration-1" })
        assertFalse(state.decorations.any { it.id == "img-decoration-2" })

        // imagesには存在する画像のみが格納されていること
        assertTrue(state.images.any { it.id == validImageId })
        assertFalse(state.images.any { it.id == missingImageId })

        // DBを上書きするためのsaveUchiwaが除外後のリストを伴って呼び出されたこと
        coVerify {
            localDatabaseRepository.saveUchiwa(
                id = uchiwaId,
                decorations = listOf(textDecoration, validImageDecoration),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )
        }
    }

    @Test
    fun `loadExistingDecorations does not overwrite DB when all images exist`() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val imageId = "existing-image-id"

        val imageDecoration = Decoration.Image(
            id = "img-decoration-1",
            imageId = imageId
        )

        val savedUchiwa = SavedUchiwa(
            decorations = listOf(imageDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        val imageReference = ImageReference(
            id = imageId,
            path = "/path/to/image.png"
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.loadImage(imageId) } returns imageReference
        every { localImageRepository.getAllImages() } returns listOf(imageReference)

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        // デコレーションがそのまま維持されていること
        assertEquals(1, state.decorations.size)
        assertTrue(state.decorations.any { it.id == "img-decoration-1" })

        // 画像が存在するのでsaveUchiwaが呼ばれないこと
        coVerify(exactly = 0) {
            localDatabaseRepository.saveUchiwa(
                id = any(),
                decorations = any(),
                uchiwaColor = any(),
                backgroundColor = any()
            )
        }
    }

    @Test
    fun `loadExistingDecorations removes all image decorations when none exist on storage`() =
        runTest {
            val uchiwaId = "test-uchiwa-id"
            val missingImageId1 = "missing-1"
            val missingImageId2 = "missing-2"
            val textDecorationId = "text-decoration-id"

            val textDecoration = Decoration.Text(
                id = textDecorationId,
                text = "テスト",
                font = FontFamilies.HACHI_MARU_POP
            )
            val missingImageDecoration1 = Decoration.Image(
                id = "img-decoration-1",
                imageId = missingImageId1
            )
            val missingImageDecoration2 = Decoration.Image(
                id = "img-decoration-2",
                imageId = missingImageId2
            )

            val savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    textDecoration,
                    missingImageDecoration1,
                    missingImageDecoration2
                ),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            )

            coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
            every { localImageRepository.loadImage(missingImageId1) } returns null
            every { localImageRepository.loadImage(missingImageId2) } returns null
            every { localImageRepository.getAllImages() } returns emptyList()

            val viewModel = createViewModel(uchiwaId = uchiwaId)
            advanceUntilIdle()

            val state = viewModel.uiState.value

            // テキストデコレーションのみ残っていること
            assertEquals(1, state.decorations.size)
            assertTrue(state.decorations.any { it.id == textDecorationId })

            // imagesが空であること
            assertTrue(state.images.isEmpty())

            // DBが上書きされていること
            coVerify {
                localDatabaseRepository.saveUchiwa(
                    id = uchiwaId,
                    decorations = listOf(textDecoration),
                    uchiwaColor = Color.Black,
                    backgroundColor = Color.White
                )
            }
        }
}


