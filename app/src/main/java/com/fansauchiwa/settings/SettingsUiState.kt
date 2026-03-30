package com.fansauchiwa.settings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val isHapticFeedbackEnabled: Boolean) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}
