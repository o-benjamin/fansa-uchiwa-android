package com.fansauchiwa.edit

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.EDIT_INPUT_ARG
import com.fansauchiwa.EditScreenInputArg
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.EditDecorationRepository
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
    private lateinit var editDecorationRepository: EditDecorationRepository
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
        editDecorationRepository = mockk(relaxed = true)
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
        templateMainColor: DecorationColors? = null,
        lastName: String? = null,
        firstName1: String? = null,
        firstName2: String? = null,
        honorific: String? = null,
        hasSeenEditCompletionTooltip: Boolean = false
    ): EditViewModel {
        settingsRepository = FakeSettingsRepository(
            hasSeenEditCompletionTooltip = hasSeenEditCompletionTooltip
        )
        val savedStateHandle = SavedStateHandle().apply {
            if (uchiwaId != null) {
                set(
                    EDIT_INPUT_ARG,
                    EditScreenInputArg(
                        uchiwaId = uchiwaId,
                        templateId = templateId,
                        templateMainColor = templateMainColor,
                        lastName = lastName,
                        firstName1 = firstName1,
                        firstName2 = firstName2,
                        honorific = honorific
                    ).toRouteArgument()
                )
            }
        }
        return EditViewModel(
            localImageRepository = localImageRepository,
            localDatabaseRepository = localDatabaseRepository,
            masterpieceRepository = masterpieceRepository,
            analyticsRepository = analyticsRepository,
            editDecorationRepository = editDecorationRepository,
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
    fun applyNewUchiwaState_templateIdAndMainColorSpecified_appliesTemplateMainColor() = runTest {
        val uchiwaId = "new-uchiwa-id"
        val templateId = "template_1"
        val selectedMainColor = DecorationColors.BLUE
        val templateUchiwaColor = Color(0xFFFF69B4)
        val templateBackgroundColor = Color(0xFFFFFFFF)

        val templateTextDecoration = Decoration.Text(
            text = "推し",
            id = "template_1_text_1",
            color = DecorationColors.PINK.value,
            strokeColor = DecorationColors.WHITE.value,
            strokeWidth = 18f,
            secondBorderColor = DecorationColors.BLACK.value,
            secondBorderWidth = 18f,
            font = FontFamilies.DELA_GOTHIC_ONE
        )
        val templateStickerDecoration = Decoration.Sticker(
            label = "heart",
            id = "template_1_sticker_1",
            color = DecorationColors.PINK.value,
            strokeColor = DecorationColors.WHITE.value,
            strokeWidth = 4f,
            secondStrokeColor = DecorationColors.BLACK.value,
            secondStrokeWidth = 4f
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

        val viewModel = createViewModel(
            uchiwaId = uchiwaId,
            templateId = templateId,
            templateMainColor = selectedMainColor
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val textDecoration = state.decorations.filterIsInstance<Decoration.Text>().single()
        val stickerDecoration = state.decorations.filterIsInstance<Decoration.Sticker>().single()

        assertEquals(uchiwaId, state.uchiwaId)
        assertEquals(2, state.decorations.size)
        assertEquals("template_1_text_1", textDecoration.id)
        assertEquals(selectedMainColor.value, textDecoration.color)
        assertEquals(DecorationColors.WHITE.value, textDecoration.strokeColor)
        assertEquals(DecorationColors.BLACK.value, textDecoration.secondBorderColor)
        assertEquals(18f, textDecoration.secondBorderWidth)
        assertEquals("template_1_sticker_1", stickerDecoration.id)
        assertEquals(DecorationColors.PINK.value, stickerDecoration.color)
        assertEquals(DecorationColors.WHITE.value, stickerDecoration.strokeColor)
        assertEquals(DecorationColors.BLACK.value, stickerDecoration.secondStrokeColor)
        assertEquals(4f, stickerDecoration.secondStrokeWidth)
        assertEquals(templateUchiwaColor, state.uchiwaColor)
        assertEquals(templateBackgroundColor, state.backgroundColor)

        coVerify(exactly = 0) {
            localDatabaseRepository.saveUchiwa(any())
        }
    }

    @Test
    fun applyNewUchiwaState_namedTemplateSpecified_replacesPlaceholderWithNonEmptyNameParts() = runTest {
        val uchiwaId = "new-uchiwa-id"
        val templateId = "template_1"
        val lastNamePlaceholder = Decoration.Text(
            text = "みょうじ",
            id = "name-last",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val firstName1Placeholder = Decoration.Text(
            text = "名",
            id = "name-1",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val firstName2Placeholder = Decoration.Text(
            text = "前",
            id = "name-2",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val honorificPlaceholder = Decoration.Text(
            text = "くん",
            id = "name-honorific",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val fixedDecoration = Decoration.Text(
            text = "して！",
            id = "fixed-text",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val template = Template(
            id = templateId,
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    lastNamePlaceholder,
                    firstName1Placeholder,
                    firstName2Placeholder,
                    honorificPlaceholder,
                    fixedDecoration
                ),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            ),
            isNameInputPlaceholderEnabled = true
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        coEvery { templateRepository.getTemplateById(templateId) } returns template
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(
            uchiwaId = uchiwaId,
            templateId = templateId,
            lastName = "佐藤",
            firstName1 = "勝",
            firstName2 = "利",
            honorific = "くん"
        )
        advanceUntilIdle()

        val textDecorations = viewModel.uiState.value.decorations.filterIsInstance<Decoration.Text>()

        assertEquals(listOf("佐藤", "勝", "利", "くん", "して！"), textDecorations.map { it.text })
        assertEquals(
            listOf("name-last", "name-1", "name-2", "name-honorific", "fixed-text"),
            textDecorations.map { it.id }
        )
    }

    @Test
    fun applyNewUchiwaState_namedTemplateSpecified_removesPlaceholderTextsForBlankValues() = runTest {
        val uchiwaId = "new-uchiwa-id"
        val templateId = "template_1"
        val lastNamePlaceholder = Decoration.Text(
            text = "みょうじ",
            id = "name-last",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val firstName1Placeholder = Decoration.Text(
            text = "名",
            id = "name-1",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val firstName2Placeholder = Decoration.Text(
            text = "前",
            id = "name-2",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val honorificPlaceholder = Decoration.Text(
            text = "くん",
            id = "name-honorific",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val fixedDecoration = Decoration.Text(
            text = "プロポーズ",
            id = "fixed-text",
            font = FontFamilies.M_PLUS_ROUNDED_1C
        )
        val template = Template(
            id = templateId,
            previewImageResId = 0,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    lastNamePlaceholder,
                    firstName1Placeholder,
                    firstName2Placeholder,
                    honorificPlaceholder,
                    fixedDecoration
                ),
                uchiwaColor = Color.Black,
                backgroundColor = Color.White
            ),
            isNameInputPlaceholderEnabled = true
        )

        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns null
        coEvery { templateRepository.getTemplateById(templateId) } returns template
        every { localImageRepository.getAllImages() } returns emptyList()

        val viewModel = createViewModel(
            uchiwaId = uchiwaId,
            templateId = templateId,
            lastName = " ",
            firstName1 = "潤",
            firstName2 = "",
            honorific = "   "
        )
        advanceUntilIdle()

        val textDecorations = viewModel.uiState.value.decorations.filterIsInstance<Decoration.Text>()

        assertEquals(listOf("潤", "プロポーズ"), textDecorations.map { it.text })
        assertEquals(
            listOf("name-1", "fixed-text"),
            textDecorations.map { it.id }
        )
    }

    @Test
    fun addTextDecoration_selectsNewlyAddedTextDecoration() = runTest {
        val newDecoration = Decoration.Text(
            id = "text-1",
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        every { localImageRepository.getAllImages() } returns emptyList()
        every { editDecorationRepository.createText(FontFamilies.HACHI_MARU_POP) } returns newDecoration

        val viewModel = createViewModel(uchiwaId = null)
        advanceUntilIdle()

        viewModel.addTextDecoration(FontFamilies.HACHI_MARU_POP)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.decorations.contains(newDecoration))
        assertEquals(newDecoration.id, state.selectedDecorationId)
    }

    @Test
    fun addStickerDecoration_selectsNewlyAddedStickerDecoration() = runTest {
        val newDecoration = Decoration.Sticker(
            id = "sticker-1",
            label = "heart"
        )
        every { localImageRepository.getAllImages() } returns emptyList()
        every { editDecorationRepository.createSticker("heart") } returns newDecoration

        val viewModel = createViewModel(uchiwaId = null)
        advanceUntilIdle()

        viewModel.addStickerDecoration("heart")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.decorations.contains(newDecoration))
        assertEquals(newDecoration.id, state.selectedDecorationId)
    }

    @Test
    fun addImageDecoration_selectsNewlyAddedImageDecoration() = runTest {
        val imageId = "image-1"
        val newDecoration = Decoration.Image(
            id = "image-decoration-1",
            imageId = imageId
        )
        val imageReference = ImageReference(
            id = imageId,
            path = "/path/to/image.png"
        )
        every { localImageRepository.getAllImages() } returns emptyList()
        every { editDecorationRepository.createImage(imageId) } returns newDecoration
        every { localImageRepository.loadImage(imageId) } returns imageReference

        val viewModel = createViewModel(uchiwaId = null)
        advanceUntilIdle()

        viewModel.addImageDecoration(imageId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.decorations.contains(newDecoration))
        assertEquals(newDecoration.id, state.selectedDecorationId)
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

    @Test
    fun initialState_setsPukuPukuSupportFromCurrentSdk() = runTest {
        every { localImageRepository.getAllImages() } returns emptyList()
        val viewModel = createViewModel(uchiwaId = null)
        advanceUntilIdle()

        assertEquals(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            viewModel.uiState.value.isPukuPukuSupported
        )
    }

    @Test
    fun notifyPukuPukuUnsupported_setsUnsupportedSnackbarMessage() = runTest {
        every { localImageRepository.getAllImages() } returns emptyList()
        val viewModel = createViewModel(uchiwaId = null)
        advanceUntilIdle()

        viewModel.notifyPukuPukuUnsupported()

        assertEquals(
            R.string.snackbar_puku_puku_unsupported,
            viewModel.uiState.value.userMessage
        )
    }
}
