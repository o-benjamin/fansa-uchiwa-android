package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.TEMPLATE_ID_ARG
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.data.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var templateRepository: TemplateRepository

    private class FakeSettingsRepository(
        private var hasSeenEditCompletionTooltip: Boolean = false
    ) : SettingsRepository {
        private val hapticFeedbackEnabledStream = MutableSharedFlow<Boolean>(replay = 1)
        private val hasSeenEditCompletionTooltipStream = MutableSharedFlow<Boolean>(replay = 1)

        override fun getHapticFeedbackEnabledStream(): Flow<Boolean> = hapticFeedbackEnabledStream

        override suspend fun fetchHapticFeedbackEnabled() {
            hapticFeedbackEnabledStream.emit(true)
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            hapticFeedbackEnabledStream.emit(enabled)
        }

        override fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean> =
            hasSeenEditCompletionTooltipStream

        override suspend fun fetchHasSeenEditCompletionTooltip() {
            hasSeenEditCompletionTooltipStream.emit(hasSeenEditCompletionTooltip)
        }

        override suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean) {
            hasSeenEditCompletionTooltip = hasSeen
        }

        fun hasSeenEditCompletionTooltip(): Boolean = hasSeenEditCompletionTooltip
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        localImageRepository = mockk(relaxed = true)
        localDatabaseRepository = mockk(relaxed = true)
        masterpieceRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        settingsRepository = FakeSettingsRepository()
        templateRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        uchiwaId: String?,
        templateId: String? = null,
        hasSeenEditCompletionTooltip: Boolean = false
    ): EditViewModel {
        settingsRepository = FakeSettingsRepository(
            hasSeenEditCompletionTooltip = hasSeenEditCompletionTooltip
        )
        val savedStateHandle = SavedStateHandle().apply {
            if (uchiwaId != null) {
                set(UCHIWA_ID_ARG, uchiwaId)
            }
            if (templateId != null) {
                set(TEMPLATE_ID_ARG, templateId)
            }
        }
        return EditViewModel(
            localImageRepository = localImageRepository,
            localDatabaseRepository = localDatabaseRepository,
            masterpieceRepository = masterpieceRepository,
            analyticsRepository = analyticsRepository,
            settingsRepository = settingsRepository,
            templateRepository = templateRepository,
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

        val savedUchiwa = Uchiwa(
            id="test-id",
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
            localDatabaseRepository.saveUchiwa(any())
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

        val savedUchiwa = Uchiwa(
            id="test-id",
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
            localDatabaseRepository.saveUchiwa(any())
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

            val savedUchiwa = Uchiwa(
            id="test-id",
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
                localDatabaseRepository.saveUchiwa(any())
            }
        }

    @Test
    fun restoreExistingUchiwa_existingDataFound_stateRestoredCorrectly() = runTest {
        val uchiwaId = "existing-uchiwa-id"
        val textDecoration = Decoration.Text(
            id = "text-1",
            text = "既存テキスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val savedUchiwaColor = Color(0xFFFF0000)
        val savedBackgroundColor = Color(0xFF00FF00)

        val savedUchiwa = Uchiwa(
            id="test-id",
            decorations = listOf(textDecoration),
            uchiwaColor = savedUchiwaColor,
            backgroundColor = savedBackgroundColor
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(uchiwaId, state.uchiwaId)
        assertEquals(1, state.decorations.size)
        assertEquals("text-1", state.decorations[0].id)
        assertEquals(savedUchiwaColor, state.uchiwaColor)
        assertEquals(savedBackgroundColor, state.backgroundColor)

        coVerify(exactly = 0) {
            localDatabaseRepository.saveUchiwa(any())
        }
    }

    @Test
    fun applyNewUchiwaState_templateIdSpecified_templateDataAppliedToState() = runTest {
        val uchiwaId = "new-uchiwa-id"
        val templateId = "template_1"
        val templateUchiwaColor = Color(0xFFFF69B4)
        val templateBackgroundColor = Color(0xFFFFFFFF)

        val templateTextDecoration = Decoration.Text(
            text = "推し",
            id = "template_1_text_1",
            font = FontFamilies.DELA_GOTHIC_ONE
        )
        val templateStickerDecoration = Decoration.Sticker(
            label = "heart",
            id = "template_1_sticker_1"
        )

        val templateSavedUchiwa = SavedUchiwa(
            decorations = listOf(templateTextDecoration, templateStickerDecoration),
            uchiwaColor = templateUchiwaColor,
            backgroundColor = templateBackgroundColor
        )

        val template = Template(
            id = templateId,
            previewImageResId = 0,
            savedUchiwa = templateSavedUchiwa
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        coEvery { templateRepository.getTemplateById(templateId) } returns template
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId, templateId = templateId)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(uchiwaId, state.uchiwaId)
        assertEquals(2, state.decorations.size)
        assertTrue(state.decorations.any { it.id == "template_1_text_1" })
        assertTrue(state.decorations.any { it.id == "template_1_sticker_1" })
        assertEquals(templateUchiwaColor, state.uchiwaColor)
        assertEquals(templateBackgroundColor, state.backgroundColor)

        coVerify(exactly = 0) {
            localDatabaseRepository.saveUchiwa(any())
        }
    }

    @Test
    fun updateFont_existingTextDecoration_fontPropertyUpdated() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val textDecorationId = "text-1"
        val textDecoration = Decoration.Text(
            id = textDecorationId,
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val savedUchiwa = Uchiwa(
            id="test-id",
            decorations = listOf(textDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        viewModel.updateFont(textDecorationId, FontFamilies.ZEN_MARU_GOTHIC)
        advanceUntilIdle()

        val updatedDecoration = viewModel.uiState.value.decorations
            .filterIsInstance<Decoration.Text>()
            .find { it.id == textDecorationId }

        assertEquals(FontFamilies.ZEN_MARU_GOTHIC, updatedDecoration?.font)
    }

    @Test
    fun updateFont_callsSaveSnapshot_undoStackHasHistory() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val textDecorationId = "text-1"
        val textDecoration = Decoration.Text(
            id = textDecorationId,
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val savedUchiwa = Uchiwa(
            id="test-id",
            decorations = listOf(textDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        // Undo不可であることを確認
        assertFalse(viewModel.uiState.value.canUndo)

        viewModel.updateFont(textDecorationId, FontFamilies.DELA_GOTHIC_ONE)
        advanceUntilIdle()

        // saveSnapshotが呼ばれたのでUndoが可能になっていること
        assertTrue(viewModel.uiState.value.canUndo)

        // Undoすると元のフォントに戻ること
        viewModel.undo()
        advanceUntilIdle()

        val restoredDecoration = viewModel.uiState.value.decorations
            .filterIsInstance<Decoration.Text>()
            .find { it.id == textDecorationId }

        assertEquals(FontFamilies.HACHI_MARU_POP, restoredDecoration?.font)
    }

    @Test
    fun updateFont_logsAnalyticsEvent_eventSentWithFontFamily() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val textDecorationId = "text-1"
        val textDecoration = Decoration.Text(
            id = textDecorationId,
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val savedUchiwa = Uchiwa(
            id="test-id",
            decorations = listOf(textDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        viewModel.updateFont(textDecorationId, FontFamilies.ZEN_MARU_GOTHIC)
        advanceUntilIdle()

        coVerify {
            analyticsRepository.logEvent(
                match {
                    it.name == AnalyticsActions.SELECT_EDIT_TEXT_FONT &&
                            it.params["font_family"] == FontFamilies.ZEN_MARU_GOTHIC.name
                }
            )
        }
    }

    @Test
    fun updateDecorationGraphic_updatesPersistedScaleAndRotation() = runTest {
        val uchiwaId = "test-uchiwa-id"
        val textDecorationId = "text-1"
        val textDecoration = Decoration.Text(
            id = textDecorationId,
            text = "テスト",
            offset = Offset(12f, -8f),
            rotation = 15f,
            scale = 1.2f,
            font = FontFamilies.HACHI_MARU_POP
        )
        val savedUchiwa = Uchiwa(
            id = "test-id",
            decorations = listOf(textDecoration),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        viewModel.updateDecorationGraphic(
            id = textDecorationId,
            offset = Offset(8f, 10f),
            scale = 0.3f,
            rotation = 25f
        )
        advanceUntilIdle()

        val updatedDecoration = viewModel.uiState.value.decorations
            .filterIsInstance<Decoration.Text>()
            .first()

        assertEquals(20f, updatedDecoration.offset.x, 0.001f)
        assertEquals(2f, updatedDecoration.offset.y, 0.001f)
        assertEquals(1.5f, updatedDecoration.scale, 0.001f)
        assertEquals(40f, updatedDecoration.rotation, 0.001f)
    }

    @Test
    fun applyNewUchiwaState_noTemplateId_defaultBlankState() = runTest {
        val uchiwaId = "new-uchiwa-id"

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val defaultState = EditUiState()

        assertEquals(uchiwaId, state.uchiwaId)
        assertTrue(state.decorations.isEmpty())
        assertEquals(defaultState.uchiwaColor, state.uchiwaColor)
        assertEquals(defaultState.backgroundColor, state.backgroundColor)

        coVerify(exactly = 0) {
            localDatabaseRepository.saveUchiwa(any())
        }
    }

    @Test
    fun initialLoad_whenTooltipNotSeen_showsCompletionTooltipAndPersistsShownFlag() = runTest {
        val uchiwaId = "new-uchiwa-id"
        every { localImageRepository.getAllImages() } returns emptyList()
        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCompletionTooltip)
        assertTrue(settingsRepository.hasSeenEditCompletionTooltip())
    }

    @Test
    fun onTooltipDismissed_afterTooltipShown_hidesTooltipAndPersistsFlag() = runTest {
        val uchiwaId = "new-uchiwa-id"
        every { localImageRepository.getAllImages() } returns emptyList()
        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        val viewModel = createViewModel(uchiwaId = uchiwaId)
        advanceUntilIdle()

        viewModel.onTooltipDismissed()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCompletionTooltip)
        assertTrue(settingsRepository.hasSeenEditCompletionTooltip())
    }

    @Test
    fun initialLoad_whenTooltipAlreadySeen_doesNotShowCompletionTooltip() = runTest {
        val uchiwaId = "new-uchiwa-id"
        every { localImageRepository.getAllImages() } returns emptyList()
        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        val viewModel = createViewModel(
            uchiwaId = uchiwaId,
            hasSeenEditCompletionTooltip = true
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCompletionTooltip)
    }
}
