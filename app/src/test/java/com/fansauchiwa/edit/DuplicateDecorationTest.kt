package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.TEMPLATE_ID_ARG
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.TemplateRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DuplicateDecorationTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var localImageRepository: LocalImageRepository
    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var templateRepository: TemplateRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        localImageRepository = mockk(relaxed = true)
        localDatabaseRepository = mockk(relaxed = true)
        masterpieceRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        templateRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        uchiwaId: String? = null,
        templateId: String? = null
    ): EditViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (uchiwaId != null) set(UCHIWA_ID_ARG, uchiwaId)
            if (templateId != null) set(TEMPLATE_ID_ARG, templateId)
        }
        return EditViewModel(
            localImageRepository = localImageRepository,
            localDatabaseRepository = localDatabaseRepository,
            masterpieceRepository = masterpieceRepository,
            analyticsRepository = analyticsRepository,
            templateRepository = templateRepository,
            savedStateHandle = savedStateHandle
        )
    }

    /** 初期デコレーションを含むViewModelをセットアップするヘルパー */
    private fun createViewModelWithDecorations(
        vararg decorations: Decoration
    ): EditViewModel {
        val uchiwaId = "test-uchiwa-id"
        val savedUchiwa = SavedUchiwa(
            decorations = decorations.toList(),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White
        )
        coEvery { localDatabaseRepository.getUchiwa(uchiwaId) } returns savedUchiwa
        every { localImageRepository.getAllImages() } returns emptyList()
        every { localImageRepository.loadImage(any()) } returns null

        return createViewModel(uchiwaId = uchiwaId)
    }

    // ----------------------------------------------------------------
    // テストケース 1:
    // 存在するIDを渡したとき、新しいUUIDを持つ同じプロパティのデコレーションが
    // offset +50f ずれた状態でリストに追加されること
    // ----------------------------------------------------------------

    @Test
    fun duplicateDecoration_existingTextDecoration_addsDuplicateWithOffsetShift() = runTest {
        val originalId = "text-1"
        val originalOffset = Offset(100f, 200f)
        val original = Decoration.Text(
            id = originalId,
            text = "テスト",
            font = FontFamilies.HACHI_MARU_POP,
            offset = originalOffset
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        viewModel.duplicateDecoration(originalId)

        val state = viewModel.uiState.value
        assertEquals(2, state.decorations.size)

        val duplicate = state.decorations.first { it.id != originalId }
        assertTrue(duplicate is Decoration.Text)
        val duplicateText = duplicate as Decoration.Text

        // 新しいUUIDが付与されていること
        assertNotEquals(originalId, duplicateText.id)

        // offset のみ +50f ずれていること
        assertEquals(originalOffset + Offset(50f, 50f), duplicateText.offset)

        // 他のプロパティは元と同じであること
        assertEquals(original.text, duplicateText.text)
        assertEquals(original.font, duplicateText.font)
        assertEquals(original.rotation, duplicateText.rotation)
        assertEquals(original.scale, duplicateText.scale)
        assertEquals(original.color, duplicateText.color)
        assertEquals(original.strokeColor, duplicateText.strokeColor)
        assertEquals(original.strokeWidth, duplicateText.strokeWidth)
        assertEquals(original.width, duplicateText.width)
    }

    @Test
    fun duplicateDecoration_existingStickerDecoration_addsDuplicateWithOffsetShift() = runTest {
        val originalId = "sticker-1"
        val originalOffset = Offset(0f, 0f)
        val original = Decoration.Sticker(
            id = originalId,
            label = "heart",
            offset = originalOffset
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        viewModel.duplicateDecoration(originalId)

        val state = viewModel.uiState.value
        assertEquals(2, state.decorations.size)

        val duplicate = state.decorations.first { it.id != originalId }
        assertTrue(duplicate is Decoration.Sticker)
        val duplicateSticker = duplicate as Decoration.Sticker

        assertNotEquals(originalId, duplicateSticker.id)
        assertEquals(originalOffset + Offset(50f, 50f), duplicateSticker.offset)
        assertEquals(original.label, duplicateSticker.label)
        assertEquals(original.rotation, duplicateSticker.rotation)
        assertEquals(original.scale, duplicateSticker.scale)
        assertEquals(original.color, duplicateSticker.color)
        assertEquals(original.strokeColor, duplicateSticker.strokeColor)
        assertEquals(original.strokeWidth, duplicateSticker.strokeWidth)
    }

    @Test
    fun duplicateDecoration_existingImageDecoration_addsDuplicateWithOffsetShift() = runTest {
        val originalId = "image-1"
        val originalOffset = Offset(30f, 40f)
        val original = Decoration.Image(
            id = originalId,
            imageId = "image-resource-1",
            offset = originalOffset
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        viewModel.duplicateDecoration(originalId)

        val state = viewModel.uiState.value
        assertEquals(2, state.decorations.size)

        val duplicate = state.decorations.first { it.id != originalId }
        assertTrue(duplicate is Decoration.Image)
        val duplicateImage = duplicate as Decoration.Image

        assertNotEquals(originalId, duplicateImage.id)
        assertEquals(originalOffset + Offset(50f, 50f), duplicateImage.offset)
        assertEquals(original.imageId, duplicateImage.imageId)
        assertEquals(original.rotation, duplicateImage.rotation)
        assertEquals(original.scale, duplicateImage.scale)
        assertEquals(original.color, duplicateImage.color)
        assertEquals(original.strokeColor, duplicateImage.strokeColor)
        assertEquals(original.strokeWidth, duplicateImage.strokeWidth)
    }

    // ----------------------------------------------------------------
    // テストケース 2:
    // 複製された新しいデコレーションのIDが selectedDecorationId にセットされること
    // ----------------------------------------------------------------

    @Test
    fun duplicateDecoration_existingDecoration_newIdIsSelected() = runTest {
        val originalId = "text-1"
        val original = Decoration.Text(
            id = originalId,
            text = "選択テスト",
            font = FontFamilies.DELA_GOTHIC_ONE
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        viewModel.duplicateDecoration(originalId)

        val state = viewModel.uiState.value
        val duplicateId = state.decorations.first { it.id != originalId }.id
        assertEquals(duplicateId, state.selectedDecorationId)
    }

    // ----------------------------------------------------------------
    // テストケース 3:
    // saveSnapshot() が呼ばれ、Undoスタックに追加される状態になること
    // ----------------------------------------------------------------

    @Test
    fun duplicateDecoration_existingDecoration_canUndoBecomesTrue() = runTest {
        val originalId = "text-1"
        val original = Decoration.Text(
            id = originalId,
            text = "Undoテスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        // 複製前はUndoできない状態
        assertFalse(viewModel.uiState.value.canUndo)

        viewModel.duplicateDecoration(originalId)

        // saveSnapshot() によりUndoスタックに積まれ、canUndo が true になること
        assertTrue(viewModel.uiState.value.canUndo)
    }

    @Test
    fun duplicateDecoration_afterDuplicate_undoRestoresOriginalState() = runTest {
        val originalId = "text-1"
        val original = Decoration.Text(
            id = originalId,
            text = "Undo後復元テスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        viewModel.duplicateDecoration(originalId)

        // 複製後はデコレーションが2件
        assertEquals(2, viewModel.uiState.value.decorations.size)

        viewModel.undo()

        // Undo後は元の1件に戻ること
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(1, stateAfterUndo.decorations.size)
        assertEquals(originalId, stateAfterUndo.decorations[0].id)
    }

    // ----------------------------------------------------------------
    // テストケース 4:
    // 存在しないIDを渡した場合は何も変化しないこと
    // ----------------------------------------------------------------

    @Test
    fun duplicateDecoration_nonExistentId_stateUnchanged() = runTest {
        val originalId = "text-1"
        val original = Decoration.Text(
            id = originalId,
            text = "変化なしテスト",
            font = FontFamilies.HACHI_MARU_POP
        )
        val viewModel = createViewModelWithDecorations(original)
        advanceUntilIdle()

        val stateBefore = viewModel.uiState.value

        viewModel.duplicateDecoration("non-existent-id")

        val stateAfter = viewModel.uiState.value

        // decorations に変化がないこと
        assertEquals(stateBefore.decorations.size, stateAfter.decorations.size)
        assertEquals(stateBefore.decorations[0].id, stateAfter.decorations[0].id)

        // selectedDecorationId に変化がないこと
        assertEquals(stateBefore.selectedDecorationId, stateAfter.selectedDecorationId)

        // Undoスタックに追加されていないこと（canUndo は false のまま）
        assertFalse(stateAfter.canUndo)
    }
}


