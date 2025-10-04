package com.example.fansauchiwa.edit

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.fansauchiwa.data.Decoration

class EditViewModel : ViewModel() {

    private val _decorations = mutableStateListOf<Decoration>()
    val decorations: List<Decoration> = _decorations

    private val redoStack = mutableListOf<Decoration>()

    fun addDecoration(decoration: Decoration) {
        redoStack.clear()
        _decorations.add(decoration)
    }

    fun onUndoClicked() {
        if (_decorations.isNotEmpty()) {
            redoStack.add(_decorations.last())
            _decorations.removeAt(_decorations.lastIndex)
        }
    }

    fun onRedoClicked() {
        if (redoStack.isNotEmpty()) {
            _decorations.add(redoStack.last())
            redoStack.removeAt(redoStack.lastIndex)
        }
    }
}
