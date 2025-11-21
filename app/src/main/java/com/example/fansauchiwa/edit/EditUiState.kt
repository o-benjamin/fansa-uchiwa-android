package com.example.fansauchiwa.edit

import com.example.fansauchiwa.data.Decoration

data class EditUiState(
    val decorations: List<Decoration> = emptyList(),
    val selectedDecorationId: String? = null,
    val editingTextId: String? = null
)