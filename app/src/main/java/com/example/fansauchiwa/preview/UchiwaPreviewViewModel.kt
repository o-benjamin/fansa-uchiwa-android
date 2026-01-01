package com.example.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.data.AdMobRepository
import com.example.fansauchiwa.data.MasterpieceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

private const val UI_STATE_KEY = "ui_state"
const val IMAGE_PATH_ARG = "imagePath"

@HiltViewModel
class UchiwaPreviewViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository,
    private val adMobRepository: AdMobRepository,
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

    /**
     * リワード広告を表示し、報酬獲得後にギャラリーに保存する
     * 広告のロードに失敗している場合は即座に保存を実行（UX低下を防ぐ）
     */
    fun showRewardedAdAndSave(activity: Activity) {
        adMobRepository.showRewardedAd(
            activity = activity,
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

