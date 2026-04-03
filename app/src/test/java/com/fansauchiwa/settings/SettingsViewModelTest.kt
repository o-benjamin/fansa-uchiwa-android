package com.fansauchiwa.settings

import com.fansauchiwa.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
        val hapticState = MutableStateFlow(true)
        override val isHapticFeedbackEnabled = hapticState
        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            hapticState.value = enabled
        }
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
    fun toggleHapticFeedback_updatesRepository() = runTest {
        val fakeRepository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(fakeRepository)

        advanceUntilIdle()

        // リポジトリからの値が反映され、ローディング状態が解除されるか
        val stateAfterLoad = viewModel.uiState.first()
        assertTrue(stateAfterLoad.isHapticFeedbackEnabled)
        assertFalse(stateAfterLoad.isLoading)

        // トグル処理の呼び出し
        viewModel.toggleHapticFeedback(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.first().isHapticFeedbackEnabled)
        assertFalse(fakeRepository.hapticState.value)
    }
}


