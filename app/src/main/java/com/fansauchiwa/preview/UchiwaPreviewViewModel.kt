package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

private const val UI_STATE_KEY = "ui_state"

@HiltViewModel
class UchiwaPreviewViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository,
    private val adMobRepository: AdMobRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<UchiwaPreviewUiState> =
        savedStateHandle.getStateFlow(UI_STATE_KEY, UchiwaPreviewUiState())

    /** この画面のセッション中にリワード広告を既に視聴済みかどうか */
    private var hasEarnedReward = false

    init {
        adMobRepository.loadRewardedAd()
        // 広告のロード状態を監視
        viewModelScope.launch {
            adMobRepository.isLoadingRewardedAd.collect { isLoading ->
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(isLoadingAd = isLoading)
            }
        }
        // Navigation引数からimagePathを取得してUI Stateに設定
        val encodedImagePath = savedStateHandle.get<String>(IMAGE_PATH_ARG)
        if (encodedImagePath != null) {
            val decodedImagePath = URLDecoder.decode(encodedImagePath, "UTF-8")
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(imagePath = decodedImagePath)
        }
    }

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.PREVIEW_SCREEN)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(eventName, params)
            )
        }
    }

    /**
     * リワード広告を表示し、報酬獲得後にギャラリーに保存する
     * 広告のロードに失敗している場合は即座に保存を実行（UX低下を防ぐ）
     * この画面で既に広告を視聴済みの場合は広告をスキップして保存を実行
     */
    fun showRewardedAdAndSave(activity: Activity) {
        logEvent(AnalyticsActions.TAP_PREVIEW_EXPORT)

        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(isSaveButtonPressed = true)

        if (hasEarnedReward) {
            saveToGallery()
            return
        }

        adMobRepository.showRewardedAd(
            activity = activity,
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            waitForLoad = true,
            onUserEarnedReward = {
                hasEarnedReward = true
                saveToGallery()
            },
            onAdFailedOrSkipped = {
                saveToGallery()
            }
        )
    }

    private fun saveToGallery() {
        viewModelScope.launch {
            val imagePath = uiState.value.imagePath
            if (imagePath != null) {
                val success = masterpieceRepository.saveMasterpieceToGallery(imagePath)
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(
                    saveSuccess = success,
                    isSaveButtonPressed = false
                )
            }
        }
    }

    fun clearSaveStatus() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            saveSuccess = null
        )
    }

    /**
     * リワード広告を表示し、広告視聴後（または失敗時）に共有用パスをセットする
     * 広告のロードに失敗している場合は即座に共有を実行（UX低下を防ぐ）
     * この画面で既に広告を視聴済みの場合は広告をスキップして共有を実行
     */
    fun showRewardedAdAndShare(activity: Activity) {
        logEvent(AnalyticsActions.TAP_PREVIEW_SHARE)

        if (hasEarnedReward) {
            setShareImagePath()
            return
        }

        adMobRepository.showRewardedAd(
            activity = activity,
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            waitForLoad = true,
            onUserEarnedReward = {
                hasEarnedReward = true
            },
            onAdFailedOrSkipped = {
                // 広告が表示されなかった場合は onAdDismissed が来ないためここで共有を実行
                setShareImagePath()
            },
            onAdDismissed = {
                // 広告が閉じられた後（画面が前面に戻ってから）共有シートを起動
                setShareImagePath()
            }
        )
    }

    private fun setShareImagePath() {
        val imagePath = uiState.value.imagePath ?: return
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(shareImagePath = imagePath)
    }

    fun clearShareImage() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(shareImagePath = null)
    }
}
