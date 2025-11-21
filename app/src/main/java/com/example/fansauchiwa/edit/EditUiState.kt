package com.example.fansauchiwa.edit

import com.example.fansauchiwa.data.Decoration

data class EditUiState(
    val decorations: List<Decoration> = emptyList(),
    val selectedDecoration: Decoration? = null,
    val editingTextId: String? = null
) {
//    val selectedDecoration: Decoration?
//        get() = decorations.find { it.id == selectedDecorationId }

    val editingText: Decoration.Text?
        get() = decorations.find { it.id == editingTextId } as? Decoration.Text
}
