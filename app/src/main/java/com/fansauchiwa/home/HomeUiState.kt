package com.fansauchiwa.home

import android.os.Parcelable
import com.fansauchiwa.data.Template
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class HomeUiState(
    val masterpiecePathList: List<String> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedPaths: List<String> = emptyList(),
    val templates: @RawValue List<Template> = emptyList()
) : Parcelable
