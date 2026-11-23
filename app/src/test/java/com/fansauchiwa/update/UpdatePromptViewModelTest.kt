package com.fansauchiwa.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AppUpdateRepository
import com.google.android.play.core.install.model.AppUpdateType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class UpdatePromptViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()
    private val updateDownloaded = MutableSharedFlow<Boolean>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appUpdateRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        every { appUpdateRepository.getUpdateDownloadedStream() } returns updateDownloaded
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(prompt: UpdatePromptType): UpdatePromptViewModel {
        coEvery { appUpdateRepository.getUpdatePromptOnLaunch() } returns prompt
        return UpdatePromptViewModel(appUpdateRepository, adMobRepository, analyticsRepository)
    }

    private fun event(name: String, source: String) =
        AnalyticsEvent(name = name, params = mapOf("source" to source))

    @Test
    fun checkForUpdate_min_showsDialogLogsAndSkipsInterstitial() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)

        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        assertEquals(UpdatePromptType.MIN, viewModel.uiState.value.dialog)
        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.SHOW_UPDATE_PROMPT, "min"))
        }
    }

    @Test
    fun checkForUpdate_force_showsForcedDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)

        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.SHOW_UPDATE_PROMPT, "force"))
        }
    }

    @Test
    fun checkForUpdate_flexibleStarted_logsWithoutDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.FLEXIBLE) } returns true

        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.SHOW_UPDATE_PROMPT, "flexible"))
        }
    }

    @Test
    fun checkForUpdate_flexibleNotStarted_logsNothingAndKeepsInterstitial() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.FLEXIBLE) } returns false

        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        verify(exactly = 0) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun checkForUpdate_none_doesNothing() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        verify(exactly = 0) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 0) { appUpdateRepository.startUpdateFlow(any(), any()) }
    }

    @Test
    fun checkForUpdate_calledTwice_asksRepositoryOnce() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.checkForUpdate(launcher)
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        coVerify(exactly = 1) { appUpdateRepository.getUpdatePromptOnLaunch() }
    }

    @Test
    fun onUpdateTap_min_closesDialogLogsAndStartsImmediateUpdate() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.IMMEDIATE) } returns true
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE, "min")) }
        verify(exactly = 0) { appUpdateRepository.openStorePage() }
    }

    @Test
    fun onUpdateTap_force_keepsDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.IMMEDIATE) } returns true
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE, "force")) }
    }

    @Test
    fun onUpdateTap_immediateUpdateUnavailable_opensStorePage() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.IMMEDIATE) } returns false
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        verify(exactly = 1) { appUpdateRepository.openStorePage() }
    }

    @Test
    fun onUpdateTap_noDialog_doesNothing() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        coVerify(exactly = 0) { appUpdateRepository.startUpdateFlow(any(), any()) }
        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun dismissDialog_min_closesDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.dismissDialog()

        assertNull(viewModel.uiState.value.dialog)
    }

    @Test
    fun dismissDialog_force_keepsDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.dismissDialog()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
    }

    @Test
    fun onUpdateFlowResult_flexibleAccepted_logsTapUpdateOnce() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.FLEXIBLE) } returns true
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.onUpdateFlowResult(Activity.RESULT_OK)
        viewModel.onUpdateFlowResult(Activity.RESULT_OK)
        advanceUntilIdle()

        coVerify(exactly = 1) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE, "flexible")) }
    }

    @Test
    fun onUpdateFlowResult_flexibleCanceled_logsNoTap() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.FLEXIBLE) } returns true
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()

        viewModel.onUpdateFlowResult(Activity.RESULT_CANCELED)
        advanceUntilIdle()

        coVerify(exactly = 0) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE, "flexible")) }
    }

    @Test
    fun onUpdateFlowResult_afterImmediateUpdate_logsNoExtraTap() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)
        coEvery { appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.IMMEDIATE) } returns true
        viewModel.checkForUpdate(launcher)
        advanceUntilIdle()
        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        viewModel.onUpdateFlowResult(Activity.RESULT_OK)
        advanceUntilIdle()

        coVerify(exactly = 1) { analyticsRepository.logEvent(match { it.name == AnalyticsActions.TAP_UPDATE }) }
    }

    @Test
    fun updateDownloaded_emitted_updatesUiState() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isUpdateDownloaded)

        updateDownloaded.emit(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isUpdateDownloaded)
    }

    @Test
    fun completeUpdate_callsRepository() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.completeUpdate()
        advanceUntilIdle()

        coVerify(exactly = 1) { appUpdateRepository.completeUpdate() }
    }
}
