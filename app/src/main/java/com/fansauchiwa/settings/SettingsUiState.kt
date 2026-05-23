package com.fansauchiwa.settings

sealed interface SettingsUiState {
    val showLicenseDialog: Boolean

    data class Loading(
        override val showLicenseDialog: Boolean = false
    ) : SettingsUiState

    data class Success(
        val isHapticFeedbackEnabled: Boolean,
        override val showLicenseDialog: Boolean = false
    ) : SettingsUiState

    data class Error(
        val message: String,
        override val showLicenseDialog: Boolean = false
    ) : SettingsUiState
}
