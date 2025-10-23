package com.example.fansauchiwa.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.FansaUchiwaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: FansaUchiwaRepository
) : ViewModel() {

    private val _decorations = mutableStateListOf<Decoration>()
    val decorations: List<Decoration> = _decorations

    var selectedDecoration by mutableStateOf<Decoration?>(null)
        private set

    private val undoStack = mutableListOf<List<Decoration>>()
    private val redoStack = mutableListOf<List<Decoration>>()

    private fun onDecorationsChanged() {
        viewModelScope.launch {
            // TODO: use savedStateHandle instead
            repository.saveDecorations(_decorations.toList())
        }
    }

    fun addDecoration(decoration: Decoration) {
        redoStack.clear()
        _decorations.add(decoration)
        undoStack.add(_decorations.toList())
        onDecorationsChanged()
    }

    fun selectDecoration(decoration: Decoration?) {
        selectedDecoration = decoration
    }

    fun updateDecorationPosition(decoration: Decoration, dragAmount: Offset) {
        val index = _decorations.indexOf(decoration)
        if (index != -1) {
            val currentDecoration = _decorations[index]
            val newDecoration = when (currentDecoration) {
                is Decoration.Sticker -> currentDecoration.copy(offset = currentDecoration.offset + dragAmount)
                is Decoration.Text -> currentDecoration.copy(offset = currentDecoration.offset + dragAmount)
            }
            _decorations[index] = newDecoration
            selectedDecoration = newDecoration
        }
    }

    fun rotateSelectedDecoration(newRotation: Float) {
        val currentSelection = selectedDecoration ?: return
        val index = _decorations.indexOf(currentSelection)
        if (index != -1) {
            val newDecoration = when (val decoration = _decorations[index]) {
                is Decoration.Sticker -> decoration.copy(rotation = newRotation)
                is Decoration.Text -> decoration // Assuming text can't be rotated for now
            }
            _decorations[index] = newDecoration
            selectedDecoration = newDecoration
        }
    }

    fun scaleSelectedDecoration(newScale: Float) {
        val currentSelection = selectedDecoration ?: return
        val index = _decorations.indexOf(currentSelection)
        if (index != -1) {
            val newDecoration = when (val decoration = _decorations[index]) {
                is Decoration.Sticker -> decoration.copy(scale = newScale)
                is Decoration.Text -> decoration.copy(scale = newScale)
            }
            _decorations[index] = newDecoration
            selectedDecoration = newDecoration
        }
    }


    fun onUndoClicked() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(undoStack.last())
            _decorations.clear()
            _decorations.addAll(undoStack.last())
            undoStack.removeAt(undoStack.lastIndex)
            onDecorationsChanged()
        }
    }

    fun onRedoClicked() {
        if (redoStack.isNotEmpty()) {
            _decorations.clear()
            _decorations.addAll(redoStack.last())
            redoStack.removeAt(redoStack.lastIndex)
            onDecorationsChanged()
        }
    }
}