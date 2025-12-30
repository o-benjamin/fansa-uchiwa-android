package com.example.fansauchiwa.preview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UchiwaPreviewUiState(
    val imagePath: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean? = null
) : Parcelable

