package com.example.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    }

    fun selectDecoration(decoration: Decoration?) {
        _uiState.update {
            it.copy(
                selectedDecoration = decoration
            )
        }
    }

    fun updateDecorationGraphic(
        decoration: Decoration,
        offset: Offset,
        scale: Float,
        rotation: Float
    ) {
        val index = _uiState.value.decorations.indexOf(decoration)
        if (index != -1) {
            val currentDecoration = _uiState.value.decorations[index]
            val newDecoration = when (currentDecoration) {
                is Decoration.Sticker -> currentDecoration.copy(
                    offset = currentDecoration.offset + offset,
                    scale = currentDecoration.scale + scale,
                    rotation = currentDecoration.rotation + rotation
                )

                is Decoration.Text -> currentDecoration.copy(
                    offset = currentDecoration.offset + offset,
                    scale = currentDecoration.scale + scale,
                    rotation = currentDecoration.rotation + rotation,

                    )
            }
            _uiState.update {
                it.copy(
                    decorations = it.decorations.toMutableList().apply {
                        this[index] = newDecoration
                    },
                    selectedDecoration = newDecoration

                )
            }
        }
    }

    fun onDecorationDoubleClick(decoration: Decoration) {
        _uiState.update { currentState ->
            val index = currentState.decorations.indexOf(decoration)
            if (index == -1) {
                return@update currentState
            }
            val currentDecoration = currentState.decorations[index] as? Decoration.Text
                ?: return@update currentState
            val newState = currentDecoration.copy(isEditingText = true)
            currentState.copy(
                decorations = currentState.decorations.toMutableList().apply {
                    this[index] = newState
                },
                selectedDecoration = newState
            )
        }
    }

    fun updateText(newText: String, decoration: Decoration) {
        _uiState.update { currentState ->
            val index = currentState.decorations.indexOf(decoration)
            if (index == -1) {
                return@update currentState
            }

            val currentDecoration = currentState.decorations[index] as? Decoration.Text
                ?: return@update currentState

            val newDecoration = currentDecoration.copy(text = newText)
            currentState.copy(
                decorations = currentState.decorations.toMutableList().apply {
                    this[index] = newDecoration
                },
                selectedDecoration = newDecoration
            )
        }
    }
}