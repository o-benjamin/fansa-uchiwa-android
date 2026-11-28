package com.fansauchiwa.inappmessage

import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.data.InAppMessageTarget
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AppUpdateRepository
import com.fansauchiwa.data.repository.InAppMessagingRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InAppMessageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var inAppMessagingRepository: InAppMessagingRepository
    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private val impressions = MutableSharedFlow<Unit>()
    private val clickedTargets = MutableSharedFlow<InAppMessageTarget>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        inAppMessagingRepository = mockk(relaxed = true)
        appUpdateRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        every { inAppMessagingRepository.getImpressionStream() } returns impressions
        every { inAppMessagingRepository.getClickedTargetStream() } returns clickedTargets
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = InAppMessageViewModel(
        inAppMessagingRepository,
        appUpdateRepository,
        adMobRepository,
        analyticsRepository
    )

    // InAppMessageHost が Activity の前にいる間だけ observeMessages を回すのと同じに、テストの間だけ回す。今の画面はホーム
    private fun TestScope.createObservingViewModel(): InAppMessageViewModel {
        val viewModel = createViewModel()
        viewModel.onHomeShownChanged(true)
        backgroundScope.launch { viewModel.observeMessages() }
        return viewModel
    }

    private fun tapEvent(target: String) =
        AnalyticsEvent(name = AnalyticsActions.TAP_IN_APP_MESSAGE, params = mapOf("target" to target))

    @Test
    fun impression_skipsNextInterstitial() = runTest {
        createObservingViewModel()
        runCurrent()

        impressions.emit(Unit)
        runCurrent()

        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
    }

    @Test
    fun click_store_opensStorePageAndLogs() = runTest {
        val viewModel = createObservingViewModel()
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.STORE)
        runCurrent()

        verify(exactly = 1) { appUpdateRepository.openStorePage() }
        assertNull(viewModel.uiState.value.pendingTarget)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("store")) }
    }

    @Test
    fun click_dailyPrompt_setsPendingTargetUntilHandled() = runTest {
        val viewModel = createObservingViewModel()
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.DAILY_PROMPT)
        runCurrent()

        assertEquals(InAppMessageTarget.DAILY_PROMPT, viewModel.uiState.value.pendingTarget)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("daily_prompt")) }

        viewModel.onPendingTargetHandled()

        assertNull(viewModel.uiState.value.pendingTarget)
    }

    @Test
    fun click_aiMaterial_setsPendingTarget() = runTest {
        val viewModel = createObservingViewModel()
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.AI_MATERIAL)
        runCurrent()

        assertEquals(InAppMessageTarget.AI_MATERIAL, viewModel.uiState.value.pendingTarget)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("ai_material")) }
    }

    @Test
    fun click_webHomeOrOther_onlyLogs() = runTest {
        val viewModel = createObservingViewModel()
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.WEB)
        clickedTargets.emit(InAppMessageTarget.HOME)
        clickedTargets.emit(InAppMessageTarget.OTHER)
        runCurrent()

        // Web ページは Firebase In-App Messaging が開く
        verify(exactly = 0) { appUpdateRepository.openStorePage() }
        assertNull(viewModel.uiState.value.pendingTarget)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("web")) }
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("home")) }
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("other")) }
    }

    @Test
    fun observeMessages_stopped_ignoresClicksUntilObservedAgain() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.observeMessages() }
        runCurrent()
        job.cancel()
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.STORE)
        runCurrent()

        verify(exactly = 0) { appUpdateRepository.openStorePage() }

        // 前に戻ったら付け直す
        backgroundScope.launch { viewModel.observeMessages() }
        runCurrent()
        clickedTargets.emit(InAppMessageTarget.STORE)
        runCurrent()

        verify(exactly = 1) { appUpdateRepository.openStorePage() }
        verify(exactly = 2) { inAppMessagingRepository.getClickedTargetStream() }
    }

    @Test
    fun click_dailyPromptOutsideHome_doesNotSetPendingTarget() = runTest {
        val viewModel = createObservingViewModel()
        viewModel.onHomeShownChanged(false)
        runCurrent()

        clickedTargets.emit(InAppMessageTarget.DAILY_PROMPT)
        runCurrent()

        // ホームに戻ったときに急に開かない。押した数は数える
        assertNull(viewModel.uiState.value.pendingTarget)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("daily_prompt")) }
    }

    @Test
    fun homeHidden_dropsPendingTarget() = runTest {
        val viewModel = createObservingViewModel()
        runCurrent()
        clickedTargets.emit(InAppMessageTarget.AI_MATERIAL)
        runCurrent()

        viewModel.onHomeShownChanged(false)

        assertNull(viewModel.uiState.value.pendingTarget)
    }

    @Test
    fun homeShownChanged_suppressesOutsideHome() = runTest {
        val viewModel = createViewModel()

        viewModel.onHomeShownChanged(false)
        viewModel.onHomeShownChanged(true)

        verifyOrder {
            inAppMessagingRepository.setMessagesSuppressed(true)
            inAppMessagingRepository.setMessagesSuppressed(false)
        }
    }
}
