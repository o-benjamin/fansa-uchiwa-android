package com.example.fansauchiwa.edit

import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.ImageReference

data class EditUiState(
    val decorations: List<Decoration> = emptyList(),
    val selectedDecorationId: String? = null,
    val editingTextId: String? = null,
    val userMessage: Int? = null,
    val images: List<ImageReference> = emptyList(),
    val allImages: List<ImageReference> = emptyList(),
    val isDeletingImage: Boolean = false
)