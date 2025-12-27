package com.example.fansauchiwa.edit

import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.ImageBitmap

data class EditUiState(
    val decorations: List<Decoration> = emptyList(),
    val selectedDecorationId: String? = null,
    val editingTextId: String? = null,
    val userMessage: Int? = null,
    val images: List<ImageBitmap> = emptyList(),
    val allImages: List<ImageBitmap> = emptyList()
)