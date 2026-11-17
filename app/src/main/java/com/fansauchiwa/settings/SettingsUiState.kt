package com.fansauchiwa.settings

import com.fansauchiwa.data.AppInfo

sealed interface SettingsUiState {
    val showLicenseDialog: Boolean

    /**
     * 画面のいちばん下に出すアプリの版と端末の ID（#411）。取得が終わるまでは null
     */
    val appInfo: AppInfo?

    data class Loading(
        override val showLicenseDialog: Boolean = false,
        override val appInfo: AppInfo? = null
    ) : SettingsUiState

    data class Success(
        val isHapticFeedbackEnabled: Boolean,
        override val showLicenseDialog: Boolean = false,
        override val appInfo: AppInfo? = null
    ) : SettingsUiState

    data class Error(
        val message: String,
        override val showLicenseDialog: Boolean = false,
        override val appInfo: AppInfo? = null
    ) : SettingsUiState
}
