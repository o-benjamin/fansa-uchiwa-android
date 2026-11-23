package com.fansauchiwa.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.UpdatePromptAnalyticsParams
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AppUpdateRepository
import com.google.android.play.core.install.model.AppUpdateType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 新しい版への更新の案内（#428）。ホームに置く [UpdatePrompt] が使う
 */
@HiltViewModel
class UpdatePromptViewModel @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
    private val adMobRepository: AdMobRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatePromptUiState())
    val uiState: StateFlow<UpdatePromptUiState> = _uiState.asStateFlow()

    // 編集画面からホームに戻るたびに、Play と Remote Config に聞き直さない
    private var hasCheckedForUpdate = false

    // Play の柔軟な更新の画面を出して、まだ結果が返ってきていない
    private var isWaitingFlexibleUpdateResult = false

    init {
        observeUpdateDownloaded()
    }

    fun observeUpdateDownloaded() {
        appUpdateRepository.getUpdateDownloadedStream()
            .onEach { isDownloaded -> _uiState.update { it.copy(isUpdateDownloaded = isDownloaded) } }
            .launchIn(viewModelScope)
    }

    fun fetchUpdateDownloaded() {
        viewModelScope.launch {
            appUpdateRepository.fetchUpdateDownloaded()
        }
    }

    /**
     * ホームを開いたときに呼ぶ。案内を出すのは起動して最初の1回だけ（閉じられないダイアログを除く。[AppUpdateRepository.getUpdatePromptOnLaunch]）
     */
    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        if (hasCheckedForUpdate) return
        hasCheckedForUpdate = true
        viewModelScope.launch {
            when (val type = appUpdateRepository.getUpdatePromptOnLaunch()) {
                UpdatePromptType.NONE -> Unit
                UpdatePromptType.FLEXIBLE -> {
                    if (appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.FLEXIBLE)) {
                        isWaitingFlexibleUpdateResult = true
                        onPromptShown(type)
                    }
                }

                UpdatePromptType.MIN, UpdatePromptType.FORCE -> {
                    _uiState.update { it.copy(dialog = type) }
                    onPromptShown(type)
                }
            }
        }
    }

    /**
     * ダイアログの「更新する」。Play の即時更新の画面を出し、出せなければ Play ストアのページを開く
     */
    fun onUpdateTap(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val type = _uiState.value.dialog ?: return
        logEvent(AnalyticsActions.TAP_UPDATE, type)
        // 閉じられないダイアログは、Play の画面で更新をやめて戻ってきても出したままにする
        if (type != UpdatePromptType.FORCE) {
            _uiState.update { it.copy(dialog = null) }
        }
        viewModelScope.launch {
            if (!appUpdateRepository.startUpdateFlow(launcher, AppUpdateType.IMMEDIATE)) {
                appUpdateRepository.openStorePage()
            }
        }
    }

    /**
     * 閉じられるダイアログの「あとで」と、ダイアログの外のタップ・戻る操作。閉じられないダイアログでは何もしない
     */
    fun dismissDialog() {
        if (_uiState.value.dialog == UpdatePromptType.FORCE) return
        _uiState.update { it.copy(dialog = null) }
    }

    /**
     * Play の更新の画面から戻ってきたとき。柔軟な更新で更新を選んだかを送る
     * （即時更新を選んだことは、ダイアログの「更新する」を押したときに送っている）
     */
    fun onUpdateFlowResult(resultCode: Int) {
        if (!isWaitingFlexibleUpdateResult) return
        isWaitingFlexibleUpdateResult = false
        if (resultCode == Activity.RESULT_OK) {
            logEvent(AnalyticsActions.TAP_UPDATE, UpdatePromptType.FLEXIBLE)
        }
    }

    /**
     * ダウンロードが終わった更新を入れる。アプリは再起動する
     */
    fun completeUpdate() {
        viewModelScope.launch {
            appUpdateRepository.completeUpdate()
        }
    }

    private fun onPromptShown(type: UpdatePromptType) {
        // 案内を出した起動で、続けて全画面の広告まで出さない
        adMobRepository.skipNextInterstitialAd()
        logEvent(AnalyticsActions.SHOW_UPDATE_PROMPT, type)
    }

    private fun logEvent(name: String, type: UpdatePromptType) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(name = name, params = UpdatePromptAnalyticsParams.params(type))
            )
        }
    }
}
