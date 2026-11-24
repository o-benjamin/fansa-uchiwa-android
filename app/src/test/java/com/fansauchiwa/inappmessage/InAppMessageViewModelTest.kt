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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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

    private fun tapEvent(target: String) =
        AnalyticsEvent(name = AnalyticsActions.TAP_IN_APP_MESSAGE, params = mapOf("target" to target))

    @Test
    fun impression_skipsNextInterstitial() = runTest {
        createViewModel()
        advanceUntilIdle()

        impressions.emit(Unit)
        advanceUntilIdle()

        verify(exactly = 1) { adMobRepository.skipNextInterstitialAd() }
    }

    @Test
    fun click_store_opensStorePageAndLogs() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        clickedTargets.emit(InAppMessageTarget.STORE)
        advanceUntilIdle()

        verify(exactly = 1) { appUpdateRepository.openStorePage() }
        assertNull(viewModel.uiState.value.pendingDestination)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("store")) }
    }

    @Test
    fun click_dailyPrompt_setsPendingDestinationUntilOpened() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        clickedTargets.emit(InAppMessageTarget.DAILY_PROMPT)
        advanceUntilIdle()

        assertEquals(InAppMessageTarget.DAILY_PROMPT, viewModel.uiState.value.pendingDestination)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("daily_prompt")) }

        viewModel.onDestinationOpened()

        assertNull(viewModel.uiState.value.pendingDestination)
    }

    @Test
    fun click_aiMaterial_setsPendingDestination() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        clickedTargets.emit(InAppMessageTarget.AI_MATERIAL)
        advanceUntilIdle()

        assertEquals(InAppMessageTarget.AI_MATERIAL, viewModel.uiState.value.pendingDestination)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("ai_material")) }
    }

    @Test
    fun click_webHomeOrOther_onlyLogs() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        clickedTargets.emit(InAppMessageTarget.WEB)
        clickedTargets.emit(InAppMessageTarget.HOME)
        clickedTargets.emit(InAppMessageTarget.OTHER)
        advanceUntilIdle()

        // Web ページは Firebase In-App Messaging が開く
        verify(exactly = 0) { appUpdateRepository.openStorePage() }
        assertNull(viewModel.uiState.value.pendingDestination)
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("web")) }
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("home")) }
        coVerify(exactly = 1) { analyticsRepository.logEvent(tapEvent("other")) }
    }

    @Test
    fun homeResumedAndPaused_switchesSuppression() = runTest {
        val viewModel = createViewModel()

        viewModel.onHomePaused()
        viewModel.onHomeResumed()

        verifyOrder {
            inAppMessagingRepository.setMessagesSuppressed(true)
            inAppMessagingRepository.setMessagesSuppressed(false)
        }
    }
}
