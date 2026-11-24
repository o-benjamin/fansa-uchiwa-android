package com.fansauchiwa.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AppUpdateRepository
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
        coEvery { appUpdateRepository.takeUpdatePromptForLaunch() } returns prompt
        return UpdatePromptViewModel(appUpdateRepository, adMobRepository, analyticsRepository)
    }

    private fun event(name: String, source: String) =
        AnalyticsEvent(name = name, params = mapOf("source" to source))

    @Test
    fun checkForUpdate_min_showsDialogLogsAndSkipsInterstitial() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)

        viewModel.checkForUpdate()
        advanceUntilIdle()

        assertEquals(UpdatePromptType.MIN, viewModel.uiState.value.dialog)
        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.VIEW_UPDATE_PROMPT, "min"))
        }
    }

    @Test
    fun checkForUpdate_force_showsForcedDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)

        viewModel.checkForUpdate()
        advanceUntilIdle()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.VIEW_UPDATE_PROMPT, "force"))
        }
    }

    @Test
    fun checkForUpdate_forceFallback_showsDialogAndLogsFallback() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE_FALLBACK)

        viewModel.checkForUpdate()
        advanceUntilIdle()

        assertEquals(UpdatePromptType.FORCE_FALLBACK, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.VIEW_UPDATE_PROMPT, "force_fallback"))
        }
    }

    @Test
    fun checkForUpdate_flexible_marksPendingWithoutStartingOrLogging() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)

        viewModel.checkForUpdate()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFlexibleUpdatePending)
        assertNull(viewModel.uiState.value.dialog)
        coVerify(exactly = 0) { appUpdateRepository.startFlexibleUpdate(any()) }
        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun checkForUpdate_none_doesNothing() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.checkForUpdate()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        assertFalse(viewModel.uiState.value.isFlexibleUpdatePending)
        verify(exactly = 0) { adMobRepository.skipNextInterstitialAd() }
    }

    @Test
    fun checkForUpdate_calledTwiceWithForce_asksAndSkipsInterstitialOnce() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)

        viewModel.checkForUpdate()
        viewModel.checkForUpdate()
        advanceUntilIdle()

        coVerify(exactly = 1) { appUpdateRepository.takeUpdatePromptForLaunch() }
        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
    }

    @Test
    fun startFlexibleUpdate_pendingAndStarted_logsAndSkipsInterstitial() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startFlexibleUpdate(launcher) } returns true
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.startFlexibleUpdate(launcher)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFlexibleUpdatePending)
        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.VIEW_UPDATE_PROMPT, "flexible"))
        }
    }

    @Test
    fun startFlexibleUpdate_notStarted_logsNothingAndKeepsInterstitial() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startFlexibleUpdate(launcher) } returns false
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.startFlexibleUpdate(launcher)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFlexibleUpdatePending)
        verify(exactly = 0) { adMobRepository.skipNextInterstitialAd() }
        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun startFlexibleUpdate_calledTwice_startsOnce() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FLEXIBLE)
        coEvery { appUpdateRepository.startFlexibleUpdate(launcher) } returns true
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.startFlexibleUpdate(launcher)
        viewModel.startFlexibleUpdate(launcher)
        advanceUntilIdle()

        coVerify(exactly = 1) { appUpdateRepository.startFlexibleUpdate(launcher) }
    }

    @Test
    fun startFlexibleUpdate_notPending_doesNothing() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.startFlexibleUpdate(launcher)
        advanceUntilIdle()

        coVerify(exactly = 0) { appUpdateRepository.startFlexibleUpdate(any()) }
    }

    @Test
    fun onUpdateTap_min_closesDialogLogsAndStartsImmediateUpdate() = runTest {
        val viewModel = createViewModel(UpdatePromptType.MIN)
        coEvery { appUpdateRepository.startImmediateUpdate(launcher) } returns true
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE_PROMPT, "min")) }
        verify(exactly = 0) { appUpdateRepository.openStorePage() }
    }

    @Test
    fun onUpdateTap_force_keepsDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)
        coEvery { appUpdateRepository.startImmediateUpdate(launcher) } returns true
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE_PROMPT, "force")) }
    }

    @Test
    fun onUpdateTap_immediateUpdateUnavailable_opensStorePage() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE_FALLBACK)
        coEvery { appUpdateRepository.startImmediateUpdate(launcher) } returns false
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialog)
        verify(exactly = 1) { appUpdateRepository.openStorePage() }
    }

    @Test
    fun onUpdateTap_noDialog_doesNothing() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.onUpdateTap(launcher)
        advanceUntilIdle()

        coVerify(exactly = 0) { appUpdateRepository.startImmediateUpdate(any()) }
        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun dismissDialog_forceFallback_closesDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE_FALLBACK)
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.dismissDialog()

        assertNull(viewModel.uiState.value.dialog)
    }

    @Test
    fun dismissDialog_force_keepsDialog() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)
        viewModel.checkForUpdate()
        advanceUntilIdle()

        viewModel.dismissDialog()

        assertEquals(UpdatePromptType.FORCE, viewModel.uiState.value.dialog)
    }

    @Test
    fun onFlexibleUpdateResult_accepted_logsTap() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.onFlexibleUpdateResult(Activity.RESULT_OK)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            analyticsRepository.logEvent(event(AnalyticsActions.TAP_UPDATE_PROMPT, "flexible"))
        }
    }

    @Test
    fun onFlexibleUpdateResult_canceled_logsNoTap() = runTest {
        val viewModel = createViewModel(UpdatePromptType.NONE)

        viewModel.onFlexibleUpdateResult(Activity.RESULT_CANCELED)
        advanceUntilIdle()

        coVerify(exactly = 0) { analyticsRepository.logEvent(any()) }
    }

    @Test
    fun onImmediateUpdateResult_passesResultToRepository() = runTest {
        val viewModel = createViewModel(UpdatePromptType.FORCE)

        viewModel.onImmediateUpdateResult(Activity.RESULT_CANCELED)

        verify(exactly = 1) { appUpdateRepository.onImmediateUpdateResult(Activity.RESULT_CANCELED) }
    }

    @Test
    fun observeUpdateDownloaded_downloadedEmitted_setsIsUpdateDownloaded() = runTest {
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
