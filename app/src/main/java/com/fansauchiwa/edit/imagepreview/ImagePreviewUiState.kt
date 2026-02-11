package com.fansauchiwa.edit.imagepreview

import android.net.Uri

sealed interface ImagePreviewUiState {
    // 処理中(初期状態)
    data object Loading : ImagePreviewUiState

    // 処理成功(加工後の画像のUriを持つ)
    data class Success(val processedUri: Uri) : ImagePreviewUiState

    // エラー発生
    data class Error(val error: Throwable) : ImagePreviewUiState
}

