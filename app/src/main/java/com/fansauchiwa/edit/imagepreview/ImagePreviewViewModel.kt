package com.fansauchiwa.edit.imagepreview

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.IMAGE_URI_ARG
import com.fansauchiwa.data.repository.ImageProcessingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ImagePreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageProcessingRepository: ImageProcessingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImagePreviewUiState>(ImagePreviewUiState.Loading)
    val uiState: StateFlow<ImagePreviewUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<Unit>()
    val errorEvent: SharedFlow<Unit> = _errorEvent.asSharedFlow()

    // 背景透過処理の結果をキャッシュ
    private var transparentUri: Uri? = null

    init {
        loadImageUri()
    }

    fun showOriginal() {
        val currentState = _uiState.value
        if (currentState !is ImagePreviewUiState.Ready) return

        _uiState.value = ImagePreviewUiState.Ready.ShowingOriginal(currentState.originalUri)
    }

    fun showTransparent() {
        val currentState = _uiState.value
        if (currentState !is ImagePreviewUiState.Ready) return

        // すでに処理済みの場合はキャッシュを使用
        transparentUri?.let {
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                originalUri = currentState.originalUri,
                transparentUri = it
            )
            return
        }

        // 背景透過処理を実行
        viewModelScope.launch {
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Loading(currentState.originalUri)

            val result = imageProcessingRepository.removeBackground(currentState.originalUri)

            result.fold(
                onSuccess = { uri ->
                    transparentUri = uri
                    _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                        originalUri = currentState.originalUri,
                        transparentUri = uri
                    )
                },
                onFailure = {
                    _errorEvent.emit(Unit)
                    _uiState.value = ImagePreviewUiState.Ready.ShowingOriginal(currentState.originalUri)
                }
            )
        }
    }

    fun loadImageUri() {
        val imageUriString: String? = savedStateHandle.get<String>(IMAGE_URI_ARG)
        if (imageUriString != null) {
            _uiState.value = ImagePreviewUiState.Ready.ShowingOriginal(imageUriString.toUri())
        } else {
            _uiState.value =
                ImagePreviewUiState.LoadError(IllegalArgumentException("imageUri is null"))
        }
    }
}
