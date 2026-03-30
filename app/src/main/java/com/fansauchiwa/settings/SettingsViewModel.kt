package com.fansauchiwa.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.isHapticFeedbackEnabled.collect { enabled ->
                val currentState = _uiState.value
                if (currentState is SettingsUiState.Success) {
                    _uiState.value = currentState.copy(isHapticFeedbackEnabled = enabled)
                } else {
                    _uiState.value = SettingsUiState.Success(isHapticFeedbackEnabled = enabled)
                }
            }
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
        }
    }
}
