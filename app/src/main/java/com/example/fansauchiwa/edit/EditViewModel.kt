package com.example.fansauchiwa.edit

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.FansaUchiwaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UI_STATE_KEY = "ui_state"

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: FansaUchiwaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState: StateFlow<EditUiState> = savedStateHandle.getStateFlow(UI_STATE_KEY, EditUiState())

    init {
        loadAllImages()
    }

    fun updateDecoration(id: String, transform: (Decoration) -> Decoration) {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations.map { decoration ->
                if (decoration.id == id) transform(decoration) else decoration
            }
        )
    }

    private fun onDecorationsChanged() {
        viewModelScope.launch {
            // TODO: use savedStateHandle instead
            repository.saveDecorations(uiState.value.decorations)
        }
    }

    fun addDecoration(decoration: Decoration) {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations + decoration
        )
        onDecorationsChanged()

        if (decoration is Decoration.Image) {
            loadImage(decoration.id)
        }
    }

    fun deleteDecoration(id: String) {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations.filter { it.id != id }
        )
        onDecorationsChanged()
    }

    fun selectDecoration(id: String) {
        if (canEdit()) {
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                selectedDecorationId = id
            )
        }
    }

    fun unSelectDecoration() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            selectedDecorationId = null
        )
    }

    fun snackbarMessageShown() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            userMessage = null
        )
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
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                editingTextId = id
            )
        }
    }

    fun finishEditingText() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            editingTextId = null
        )
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
                val currentState = uiState.value
                val updatedImages = currentState.images.filter { it.id != imageId } + imageData
                savedStateHandle[UI_STATE_KEY] = currentState.copy(images = updatedImages)
            }
        }
    }

    fun loadAllImages() {
        viewModelScope.launch {
            val images = repository.getAllImages()
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(allImages = images)
        }
    }

    fun startImageDeletionMode() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(isDeletingImage = true)
    }

    fun toggleImageSelection(imageId: String) {
        val currentState = uiState.value
        val currentSelected = currentState.selectedDeletingImages
        val newSelected = if (currentSelected.contains(imageId)) {
            currentSelected - imageId
        } else {
            currentSelected + imageId
        }
        savedStateHandle[UI_STATE_KEY] = currentState.copy(selectedDeletingImages = newSelected)
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedDeletingImages
            if (selectedIds.isNotEmpty()) {
                repository.deleteImages(selectedIds)
                loadAllImages()
                cancelImageDeletionMode()
            }
        }
    }

    fun cancelImageDeletionMode() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            isDeletingImage = false,
            selectedDeletingImages = emptyList()
        )
    }

    private fun canEdit(): Boolean {
        (uiState.value.decorations.find { it.id == uiState.value.selectedDecorationId } as? Decoration.Text)?.let {
            if (it.text.isEmpty()) {
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(
                    userMessage = R.string.snackbar_input_too_short
                )
                return false
            }
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun saveUchiwaBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            // TODO: リポジトリの保存メソッドを実装する
            // repository.saveUchiwaBitmap(bitmap, "uchiwa_${System.currentTimeMillis()}.png")

            // 保存完了のメッセージを表示
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                userMessage = R.string.snackbar_saved
            )
        }
    }

    fun onBitmapSaved() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            shouldSaveBitmap = false
        )
    }

    fun resetEditUiState() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            selectedDecorationId = null,
            editingTextId = null,
            userMessage = null,
            isDeletingImage = false,
            selectedDeletingImages = emptyList(),
            shouldSaveBitmap = false
        )
    }
}

