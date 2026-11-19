package com.fansauchiwa.settings

import android.util.Log
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.repository.AppInfoRepository
import com.fansauchiwa.data.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    class FakeSettingsRepository : SettingsRepository {
        private val hapticState = MutableSharedFlow<Boolean>(replay = 1)
        private var hapticEnabled = true

        override fun getHapticFeedbackEnabledStream(): Flow<Boolean> = hapticState

        override suspend fun fetchHapticFeedbackEnabled() {
            hapticState.emit(hapticEnabled)
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            hapticEnabled = enabled
        }

        override fun getHasSeenApologyDialogStream(): Flow<Boolean> = MutableSharedFlow()

        override suspend fun fetchHasSeenApologyDialog() = Unit

        override suspend fun setHasSeenApologyDialog(hasSeen: Boolean) = Unit

        fun isHapticEnabled(): Boolean = hapticEnabled
    }

    class FakeAppInfoRepository(private val appInfo: AppInfo) : AppInfoRepository {
        private val appInfoState = MutableSharedFlow<AppInfo>(replay = 1)

        override fun getAppInfoStream(): Flow<AppInfo> = appInfoState

        override suspend fun fetchAppInfo() {
            appInfoState.emit(appInfo)
        }
    }

    private fun createViewModel(
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        appInfo: AppInfo = AppInfo(
            versionName = "2.13.0",
            installationId = "test-installation-id",
            userId = "test-user-id"
        )
    ): SettingsViewModel = SettingsViewModel(settingsRepository, FakeAppInfoRepository(appInfo))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // 取得の失敗はログに残すだけなので、JVM のテストでは Log を空にしておく
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun toggleHapticFeedback_updatesRepository() = runTest {
        val fakeRepository = FakeSettingsRepository()
        val viewModel = createViewModel(settingsRepository = fakeRepository)

        advanceUntilIdle()

        val stateAfterLoad = viewModel.uiState.value
        assertTrue(stateAfterLoad is SettingsUiState.Success)
        assertTrue((stateAfterLoad as SettingsUiState.Success).isHapticFeedbackEnabled)

        viewModel.toggleHapticFeedback(false)
        advanceUntilIdle()

        val stateAfterToggle = viewModel.uiState.value
        assertTrue(stateAfterToggle is SettingsUiState.Success)
        assertFalse((stateAfterToggle as SettingsUiState.Success).isHapticFeedbackEnabled)
        assertFalse(fakeRepository.isHapticEnabled())
    }

    @Test
    fun showAndDismissLicenseDialog_updatesUiState() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertTrue(initialState is SettingsUiState.Success)
        assertFalse(initialState.showLicenseDialog)

        viewModel.showLicenseDialog()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showLicenseDialog)

        viewModel.dismissLicenseDialog()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showLicenseDialog)
    }

    @Test
    fun appInfo_installationIdAvailable_isSetInUiStateAndKeptAfterToggle() = runTest {
        val appInfo = AppInfo(versionName = "2.13.0", installationId = "test-installation-id", userId = "test-user-id")
        val viewModel = createViewModel(appInfo = appInfo)

        advanceUntilIdle()

        val stateAfterLoad = viewModel.uiState.value
        assertTrue(stateAfterLoad is SettingsUiState.Success)
        assertEquals(appInfo, stateAfterLoad.appInfo)

        // 触覚フィードバックの値が流れ直しても、版と ID は消えない
        viewModel.toggleHapticFeedback(false)
        advanceUntilIdle()

        assertEquals(appInfo, viewModel.uiState.value.appInfo)
    }

    @Test
    fun appInfo_installationIdUnavailable_keepsSuccessWithNullInstallationId() = runTest {
        val viewModel = createViewModel(appInfo = AppInfo(versionName = "2.13.0", installationId = null, userId = null))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SettingsUiState.Success)
        assertEquals("2.13.0", state.appInfo?.versionName)
        assertNull(state.appInfo?.installationId)
    }

    @Test
    fun appInfo_showAndDismissLicenseDialog_keepsAppInfo() = runTest {
        val appInfo = AppInfo(versionName = "2.13.0", installationId = "test-installation-id", userId = "test-user-id")
        val viewModel = createViewModel(appInfo = appInfo)

        advanceUntilIdle()
        viewModel.showLicenseDialog()
        viewModel.dismissLicenseDialog()
        advanceUntilIdle()

        assertEquals(appInfo, viewModel.uiState.value.appInfo)
    }

    @Test
    fun appInfo_fetchThrows_keepsSuccessWithoutAppInfo() = runTest {
        val throwingRepository = object : AppInfoRepository {
            override fun getAppInfoStream(): Flow<AppInfo> = MutableSharedFlow()

            override suspend fun fetchAppInfo() {
                throw IllegalStateException("fetch failed")
            }
        }
        val viewModel = SettingsViewModel(FakeSettingsRepository(), throwingRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SettingsUiState.Success)
        assertNull(state.appInfo)
    }
}
