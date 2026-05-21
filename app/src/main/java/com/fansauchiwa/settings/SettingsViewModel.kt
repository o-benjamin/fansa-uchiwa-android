package com.fansauchiwa.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        fetchSettings()
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.fetchHapticFeedbackEnabled()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeSettings() {
        settingsRepository.getHapticFeedbackEnabledStream()
            .onEach { enabled ->
                _uiState.value = SettingsUiState.Success(isHapticFeedbackEnabled = enabled)
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ToggleHapticFeedback -> toggleHapticFeedback(action.enabled)
        }
    }

    private fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
            settingsRepository.fetchHapticFeedbackEnabled()
        }
    }
}
