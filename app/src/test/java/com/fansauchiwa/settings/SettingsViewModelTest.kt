package com.fansauchiwa.settings

import com.fansauchiwa.data.repository.SettingsRepository
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
import org.junit.Assert.assertFalse
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

        override fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean> = MutableSharedFlow()

        override suspend fun fetchHasSeenEditCompletionTooltip() = Unit

        override suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean) = Unit

        override fun getHasSeenApologyDialogStream(): Flow<Boolean> = MutableSharedFlow()

        override suspend fun fetchHasSeenApologyDialog() = Unit

        override suspend fun setHasSeenApologyDialog(hasSeen: Boolean) = Unit

        fun isHapticEnabled(): Boolean = hapticEnabled
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAction_toggleHapticFeedback_updatesRepository() = runTest {
        val fakeRepository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(fakeRepository)

        advanceUntilIdle()

        val stateAfterLoad = viewModel.uiState.value
        assertTrue(stateAfterLoad is SettingsUiState.Success)
        assertTrue((stateAfterLoad as SettingsUiState.Success).isHapticFeedbackEnabled)

        viewModel.onAction(SettingsAction.ToggleHapticFeedback(false))
        advanceUntilIdle()

        val stateAfterToggle = viewModel.uiState.value
        assertTrue(stateAfterToggle is SettingsUiState.Success)
        assertFalse((stateAfterToggle as SettingsUiState.Success).isHapticFeedbackEnabled)
        assertFalse(fakeRepository.isHapticEnabled())
    }
}
