package com.example.fansauchiwa.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.net.URLDecoder
import javax.inject.Inject

private const val UI_STATE_KEY = "ui_state"
const val IMAGE_PATH_ARG = "imagePath"

@HiltViewModel
class UchiwaPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
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
}

