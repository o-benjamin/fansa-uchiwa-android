package com.fansauchiwa.edit.imagepreview

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.repository.ImageProcessingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagePreviewViewModel @Inject constructor(
    private val imageProcessingRepository: ImageProcessingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImagePreviewUiState>(ImagePreviewUiState.Loading)
    val uiState: StateFlow<ImagePreviewUiState> = _uiState.asStateFlow()

    fun processImage(uriString: String) {
        viewModelScope.launch {
            _uiState.value = ImagePreviewUiState.Loading

            val result = imageProcessingRepository.removeBackground(uriString.toUri())

            _uiState.value = result.fold(
                onSuccess = { uri -> ImagePreviewUiState.Success(uri) },
                onFailure = { exception -> ImagePreviewUiState.Error(exception) }
            )
        }
    }
}



