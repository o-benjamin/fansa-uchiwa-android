package com.example.fansauchiwa.edit

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.FansaUchiwaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: FansaUchiwaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        loadAllImages()
    }

    fun updateDecoration(id: String, transform: (Decoration) -> Decoration) {
        _uiState.update { currentState ->
            currentState.copy(
                decorations = currentState.decorations.map { decoration ->
                    if (decoration.id == id) transform(decoration) else decoration
                }
            )
        }
    }

    private fun onDecorationsChanged() {
        viewModelScope.launch {
            // TODO: use savedStateHandle instead
            repository.saveDecorations(_uiState.value.decorations)
        }
    }

    fun addDecoration(decoration: Decoration) {
        _uiState.update {
            it.copy(decorations = it.decorations + decoration)
        }
        onDecorationsChanged()

        if (decoration is Decoration.Image) {
            loadImage(decoration.id)
        }
    }

    fun deleteDecoration(id: String) {
        _uiState.update { currentState ->
            currentState.copy(
                decorations = currentState.decorations.filter { it.id != id }
            )
        }
        onDecorationsChanged()
    }

    fun selectDecoration(id: String) {
        if (canEdit()) {
            _uiState.update { state ->
                state.copy(
                    selectedDecorationId = id
                )
            }
        }
    }

    fun unSelectDecoration() {
        _uiState.update { state ->
            state.copy(
                selectedDecorationId = null
            )
        }
    }

    fun snackbarMessageShown() {
        _uiState.update { state ->
            state.copy(
                userMessage = null
            )
        }
    }

    fun updateDecorationGraphic(id: String, offset: Offset, scale: Float, rotation: Float) {
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Sticker -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )

                is Decoration.Text -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )

                is Decoration.Image -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )
            }
        }
    }

    fun startEditingText(id: String) {
        if (canEdit()) {
            _uiState.update { state ->
                state.copy(
                    editingTextId = id
                )
            }
        }
    }

    fun finishEditingText() {
        _uiState.update { state ->
            state.copy(
                editingTextId = null
            )
        }
    }

    fun updateText(id: String, newText: String) {
        updateDecoration(id) { decoration ->
            (decoration as? Decoration.Text)?.copy(text = newText) ?: decoration
        }
    }

    fun updateColor(id: String, newColor: Int) {
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Sticker -> decoration.copy(color = newColor)
                is Decoration.Text -> decoration.copy(color = newColor)
                is Decoration.Image -> decoration.copy(color = newColor)
            }
        }
        onDecorationsChanged()
    }

    fun updateStrokeColor(id: String, newColor: Int) {
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> decoration.copy(strokeColor = newColor)
                is Decoration.Sticker -> decoration.copy(strokeColor = newColor)
                is Decoration.Image -> decoration.copy(strokeColor = newColor)
            }
        }
    }

    fun updateWidth(id: String, newWidth: Int) {
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> decoration.copy(width = newWidth)
                else -> decoration
            }
        }
    }

    fun updateStrokeWidth(id: String, newWidth: Float) {
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> decoration.copy(strokeWidth = newWidth)
                // TODO: いずれはStickerの枠線の太さも変更したい
                else -> decoration
            }
        }
    }

    fun saveImage(uri: Uri, id: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveImage(uri, id)
            loadAllImages()
            onSaved()
        }
    }

    private fun loadImage(imageId: String) {
        viewModelScope.launch {
            val imageData = repository.loadImage(imageId)
            if (imageData != null) {
                _uiState.update { state ->
                    val updatedImages = state.images.filter { it.id != imageId } + imageData
                    state.copy(images = updatedImages)
                }
            }
        }
    }

    fun loadAllImages() {
        viewModelScope.launch {
            val images = repository.getAllImages()
            _uiState.update { state ->
                state.copy(allImages = images)
            }
        }
    }

    fun startImageDeletionMode() {
        _uiState.update { state ->
            state.copy(isDeletingImage = true)
        }
    }

    fun toggleImageSelection(imageId: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedDeletingImages
            val newSelected = if (currentSelected.contains(imageId)) {
                currentSelected - imageId
            } else {
                currentSelected + imageId
            }
            state.copy(selectedDeletingImages = newSelected)
        }
    }

    private fun canEdit(): Boolean {
        (_uiState.value.decorations.find { it.id == _uiState.value.selectedDecorationId } as? Decoration.Text)?.let {
            if (it.text.isEmpty()) {
                _uiState.update { state ->
                    state.copy(userMessage = R.string.snackbar_input_too_short)
                }
                return false
            }
        }
        return true
    }
}