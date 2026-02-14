package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.MasterpieceRepository
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

    init {
        // Navigation引数からimagePathを取得してUI Stateに設定
        val encodedImagePath = savedStateHandle.get<String>(IMAGE_PATH_ARG)
        if (encodedImagePath != null) {
            val decodedImagePath = URLDecoder.decode(encodedImagePath, "UTF-8")
            savedStateHandle[UI_STATE_KEY] = UchiwaPreviewUiState(imagePath = decodedImagePath)
        }
    }

    val uiState: StateFlow<UchiwaPreviewUiState> =
        savedStateHandle.getStateFlow(UI_STATE_KEY, UchiwaPreviewUiState())

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
     */
    fun showRewardedAdAndSave(activity: Activity) {
        logEvent(com.fansauchiwa.data.analytics.AnalyticsActions.TAP_PREVIEW_EXPORT)
        adMobRepository.showRewardedAd(
            activity = activity,
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            onUserEarnedReward = {
                // 報酬獲得（広告を最後まで視聴）したら保存を実行
                saveToGallery()
            },
            onAdFailedOrSkipped = {
                // 広告が表示できなかった場合も保存を実行
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
                    isSaving = false,
                    saveSuccess = success
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
}

