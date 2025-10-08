package com.example.fansauchiwa.edit

import androidx.compose.runtime.mutableStateListOf
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
