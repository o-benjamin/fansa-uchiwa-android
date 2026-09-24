package com.fansauchiwa.preview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UchiwaPreviewUiState(
    val imagePath: String? = null,
    val saveSuccess: Boolean? = null,
    val isLoadingAd: Boolean = false,
    val isSaveButtonPressed: Boolean = false,
    val shareImagePath: String? = null
) : Parcelable

