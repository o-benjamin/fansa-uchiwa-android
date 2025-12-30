package com.example.fansauchiwa.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun saveToGallery() {
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

