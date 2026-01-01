package com.example.fansauchiwa.edit

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.example.fansauchiwa.data.ColorParceler
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.ImageReference
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<Color, ColorParceler>
data class EditUiState(
    val uchiwaId: String = "",
    val decorations: List<Decoration> = emptyList(),
    val selectedDecorationId: String? = null,
    val editingTextId: String? = null,
    val userMessage: Int? = null,
    val images: List<ImageReference> = emptyList(),
    val allImages: List<ImageReference> = emptyList(),
    val isDeletingImage: Boolean = false,
    val selectedDeletingImages: List<String> = emptyList(),
    val savedPath: String? = null,
    val uchiwaColor: Color = Color(0xFB888888),
    val backgroundColor: Color = Color(0x00000000)
) : Parcelable
