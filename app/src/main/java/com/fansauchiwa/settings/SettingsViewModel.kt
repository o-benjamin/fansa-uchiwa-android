package com.fansauchiwa.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.repository.AppInfoRepository
import com.fansauchiwa.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val appInfoRepository: AppInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeAppInfo()
        fetchSettings()
        fetchAppInfo()
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.fetchHapticFeedbackEnabled()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = e.message ?: "Unknown error",
                    showLicenseDialog = _uiState.value.showLicenseDialog,
                    appInfo = _uiState.value.appInfo
                )
            }
        }
    }

    private fun observeSettings() {
        settingsRepository.getHapticFeedbackEnabledStream()
            .onEach { enabled ->
                _uiState.value = SettingsUiState.Success(
                    isHapticFeedbackEnabled = enabled,
                    showLicenseDialog = _uiState.value.showLicenseDialog,
                    appInfo = _uiState.value.appInfo
                )
            }
            .launchIn(viewModelScope)
    }

    private fun fetchAppInfo() {
        viewModelScope.launch {
            try {
                appInfoRepository.fetchAppInfo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Repository は ID が取れなくても null を流す約束なので、ここに来るのは想定外の不具合だけ。
                // バージョンと ID の行が出ないだけなので、触覚フィードバックの設定まで Error にしない
                Log.w(TAG, "アプリのバージョンと ID の取得に失敗", e)
            }
        }
    }

    private fun observeAppInfo() {
        appInfoRepository.getAppInfoStream()
            .onEach { appInfo ->
                _uiState.update { currentState ->
                    currentState.copySharedState(appInfo = appInfo)
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
            settingsRepository.fetchHapticFeedbackEnabled()
        }
    }

    fun showLicenseDialog() {
        _uiState.update { currentState ->
            currentState.copySharedState(showLicenseDialog = true)
        }
    }

    fun dismissLicenseDialog() {
        _uiState.update { currentState ->
            currentState.copySharedState(showLicenseDialog = false)
        }
    }

    /**
     * 3つの状態に共通の項目（ライセンスの表示・バージョンと ID）だけを変える
     */
    private fun SettingsUiState.copySharedState(
        showLicenseDialog: Boolean = this.showLicenseDialog,
        appInfo: AppInfo? = this.appInfo
    ): SettingsUiState =
        when (this) {
            is SettingsUiState.Loading -> copy(showLicenseDialog = showLicenseDialog, appInfo = appInfo)
            is SettingsUiState.Success -> copy(showLicenseDialog = showLicenseDialog, appInfo = appInfo)
            is SettingsUiState.Error -> copy(showLicenseDialog = showLicenseDialog, appInfo = appInfo)
        }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}
