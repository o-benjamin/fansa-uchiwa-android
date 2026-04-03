package com.fansauchiwa.home

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.TemplateRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var localDatabaseRepository: LocalDatabaseRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var templateRepository: TemplateRepository
    private lateinit var uuidProvider: UuidProvider

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        masterpieceRepository = mockk(relaxed = true)
        localDatabaseRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        templateRepository = mockk(relaxed = true)
        uuidProvider = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            masterpieceRepository = masterpieceRepository,
            localDatabaseRepository = localDatabaseRepository,
            analyticsRepository = analyticsRepository,
            templateRepository = templateRepository,
            uuidProvider = uuidProvider
        )
    }

    // region duplicateSelectedMasterpieces

    @Test
    fun duplicateSelectedMasterpieces_callsDuplicateForEachSelectedPath() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val path2 = "/data/masterpiece/uchiwa2.png"
        val newId1 = "new-uuid-1"
        val newId2 = "new-uuid-2"

        every { uuidProvider.generate() } returnsMany listOf(newId1, newId2)
        coEvery {
            masterpieceRepository.duplicateMasterpiece(
                path1,
                newId1
            )
        } returns "/data/masterpiece/$newId1.png"
        coEvery {
            masterpieceRepository.duplicateMasterpiece(
                path2,
                newId2
            )
        } returns "/data/masterpiece/$newId2.png"
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.White,
            backgroundColor = Color.Black
        )
        coEvery { localDatabaseRepository.getUchiwa("uchiwa2") } returns Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.Red,
            backgroundColor = Color.Blue
        )
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns listOf(
            path1, path2, "/data/masterpiece/$newId1.png", "/data/masterpiece/$newId2.png"
        )

        val viewModel = createViewModel()

        // 選択モードに入り、パスを選択
        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)
        viewModel.togglePathSelection(path2)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: 各パスに対してduplicateMasterpieceが呼ばれたか
        coVerify(exactly = 1) { masterpieceRepository.duplicateMasterpiece(path1, newId1) }
        coVerify(exactly = 1) { masterpieceRepository.duplicateMasterpiece(path2, newId2) }
    }

    @Test
    fun duplicateSelectedMasterpieces_callsSaveUchiwaWithNewId() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val newId1 = "new-uuid-1"
        val savedUchiwa = Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.White,
            backgroundColor = Color.Black
        )

        every { uuidProvider.generate() } returns newId1
        coEvery {
            masterpieceRepository.duplicateMasterpiece(
                path1,
                newId1
            )
        } returns "/data/masterpiece/$newId1.png"
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns savedUchiwa
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns listOf(
            path1,
            "/data/masterpiece/$newId1.png"
        )

        val viewModel = createViewModel()

        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: 新しいIDでsaveUchiwaが呼ばれたか
        coVerify(exactly = 1) {
            localDatabaseRepository.saveUchiwa(any())
        }
    }

    @Test
    fun duplicateSelectedMasterpieces_exitsDeletingMode() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val newId1 = "new-uuid-1"

        every { uuidProvider.generate() } returns newId1
        coEvery {
            masterpieceRepository.duplicateMasterpiece(
                path1,
                newId1
            )
        } returns "/data/masterpiece/$newId1.png"
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.White,
            backgroundColor = Color.White
        )
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns emptyList()

        val viewModel = createViewModel()

        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: 選択モードが解除されていること
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertEquals(emptyList<String>(), viewModel.uiState.value.selectedPaths)
    }

    @Test
    fun duplicateSelectedMasterpieces_refreshesMasterpieceList() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val newId1 = "new-uuid-1"
        val newPath1 = "/data/masterpiece/$newId1.png"
        val expectedPaths = listOf(newPath1, path1)

        every { uuidProvider.generate() } returns newId1
        coEvery { masterpieceRepository.duplicateMasterpiece(path1, newId1) } returns newPath1
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.White,
            backgroundColor = Color.White
        )
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns expectedPaths

        val viewModel = createViewModel()

        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: masterpiecePathListが更新されていること
        assertEquals(expectedPaths, viewModel.uiState.value.masterpiecePathList)
    }

    @Test
    fun duplicateSelectedMasterpieces_noSelection_doesNothing() = runTest {
        // 準備
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns emptyList()

        val viewModel = createViewModel()

        // 選択なしで実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: duplicateMasterpieceが一度も呼ばれないこと
        coVerify(exactly = 0) { masterpieceRepository.duplicateMasterpiece(any(), any()) }
        coVerify(exactly = 0) { localDatabaseRepository.saveUchiwa(any()) }
    }

    @Test
    fun duplicateSelectedMasterpieces_uchiwaNotFound_skipsDatabase() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val newId1 = "new-uuid-1"

        every { uuidProvider.generate() } returns newId1
        coEvery {
            masterpieceRepository.duplicateMasterpiece(
                path1,
                newId1
            )
        } returns "/data/masterpiece/$newId1.png"
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns null
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns listOf(
            path1,
            "/data/masterpiece/$newId1.png"
        )

        val viewModel = createViewModel()

        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: ファイル複製は行われるが、DB保存はスキップされること
        coVerify(exactly = 1) { masterpieceRepository.duplicateMasterpiece(path1, newId1) }
        coVerify(exactly = 0) { localDatabaseRepository.saveUchiwa(any()) }
    }

    @Test
    fun duplicateSelectedMasterpieces_duplicateFileFails_skipsDatabaseSave() = runTest {
        // 準備
        val path1 = "/data/masterpiece/uchiwa1.png"
        val newId1 = "new-uuid-1"

        every { uuidProvider.generate() } returns newId1
        coEvery { masterpieceRepository.duplicateMasterpiece(path1, newId1) } returns null
        coEvery { localDatabaseRepository.getUchiwa("uchiwa1") } returns Uchiwa(
            id = "test-id",
            decorations = emptyList(),
            uchiwaColor = Color.White,
            backgroundColor = Color.White
        )
        coEvery { masterpieceRepository.loadAllMasterpieces() } returns listOf(path1)

        val viewModel = createViewModel()

        viewModel.enterSelectionMode()
        viewModel.togglePathSelection(path1)

        // 実行
        viewModel.duplicateSelectedMasterpieces()
        advanceUntilIdle()

        // 検証: ファイル複製が失敗した場合、DB保存もスキップされること
        coVerify(exactly = 1) { masterpieceRepository.duplicateMasterpiece(path1, newId1) }
        coVerify(exactly = 0) { localDatabaseRepository.saveUchiwa(any()) }
    }

    // endregion
}
