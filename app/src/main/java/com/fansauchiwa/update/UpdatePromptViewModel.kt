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

    // 画面の回転などで UpdatePrompt だけが作り直されると LaunchedEffect がもう一度走る。Repository は FORCE を毎回返すので、ここで止めないと計測と広告の飛ばしが重なる
    private var hasCheckedForUpdate = false

    init {
        observeUpdateDownloaded()
    }

    fun fetchUpdateDownloaded() {
        viewModelScope.launch {
            appUpdateRepository.fetchUpdateDownloaded()
        }
    }

    /**
     * ホームを開いたときに呼ぶ。案内を出すのは起動して最初の1回だけ（閉じられないダイアログを除く。[AppUpdateRepository.takeUpdatePromptForLaunch]）。
     * 柔軟な更新は [UpdatePromptUiState.isFlexibleUpdatePending] を立てるだけで、Play の画面は [startFlexibleUpdate] で出す
     */
    fun checkForUpdate() {
        if (hasCheckedForUpdate) return
        hasCheckedForUpdate = true
        viewModelScope.launch {
            when (val type = appUpdateRepository.takeUpdatePromptForLaunch()) {
                UpdatePromptType.NONE -> Unit
                UpdatePromptType.FLEXIBLE -> _uiState.update { it.copy(isFlexibleUpdatePending = true) }
                UpdatePromptType.MIN, UpdatePromptType.FORCE_FALLBACK, UpdatePromptType.FORCE -> {
                    _uiState.update { it.copy(dialog = type) }
                    onPromptShown(type)
                }
            }
        }
    }

    /**
     * Play の柔軟な更新の画面を出す。[UpdatePromptUiState.isFlexibleUpdatePending] のとき、ホームが出ている間に呼ぶ
     */
    fun startFlexibleUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        if (!_uiState.value.isFlexibleUpdatePending) return
        _uiState.update { it.copy(isFlexibleUpdatePending = false) }
        viewModelScope.launch {
            if (appUpdateRepository.startFlexibleUpdate(launcher)) {
                onPromptShown(UpdatePromptType.FLEXIBLE)
            }
        }
    }

    /**
     * ダイアログの「更新する」。Play の即時更新の画面を出し、出せなければ Play ストアのページを開く
     */
    fun onUpdateTap(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val type = _uiState.value.dialog ?: return
        logEvent(AnalyticsActions.TAP_UPDATE_PROMPT, type)
        // 閉じられないダイアログは、Play の画面で更新をやめて戻ってきても出したままにする
        if (type != UpdatePromptType.FORCE) {
            _uiState.update { it.copy(dialog = null) }
        }
        viewModelScope.launch {
            if (!appUpdateRepository.startImmediateUpdate(launcher)) {
                appUpdateRepository.openStorePage()
            }
        }
    }

    /**
     * Play の即時更新の画面から戻ってきたとき。更新を選んだことは [onUpdateTap] で送っているので、失敗のログだけ残す
     */
    fun onImmediateUpdateResult(resultCode: Int) {
        appUpdateRepository.onImmediateUpdateResult(resultCode)
    }

    /**
     * 閉じられるダイアログの「あとで」と、ダイアログの外のタップ・戻る操作。閉じられないダイアログでは何もしない
     */
    fun dismissDialog() {
        if (_uiState.value.dialog == UpdatePromptType.FORCE) return
        _uiState.update { it.copy(dialog = null) }
    }

    /**
     * Play の柔軟な更新の画面から戻ってきたとき。更新を選んでいたら送る
     */
    fun onFlexibleUpdateResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            logEvent(AnalyticsActions.TAP_UPDATE_PROMPT, UpdatePromptType.FLEXIBLE)
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

    private fun observeUpdateDownloaded() {
        appUpdateRepository.getUpdateDownloadedStream()
            .onEach { isDownloaded -> _uiState.update { it.copy(isUpdateDownloaded = isDownloaded) } }
            .launchIn(viewModelScope)
    }

    private fun onPromptShown(type: UpdatePromptType) {
        // 案内を出した起動で、続けて全画面の広告まで出さない
        adMobRepository.skipNextInterstitialAd()
        logEvent(AnalyticsActions.VIEW_UPDATE_PROMPT, type)
    }

    private fun logEvent(name: String, type: UpdatePromptType) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(name = name, params = UpdatePromptAnalyticsParams.params(type))
            )
        }
    }
}
