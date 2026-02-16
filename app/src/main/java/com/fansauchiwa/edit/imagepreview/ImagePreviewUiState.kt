package com.fansauchiwa.edit.imagepreview

import android.net.Uri

sealed interface ImagePreviewUiState {
    /**
     * 初期ロード中
     */
    data object Loading : ImagePreviewUiState

    /**
     * 画像URIのロード失敗
     */
    data class LoadError(val error: Throwable) : ImagePreviewUiState

    /**
     * 画像URIロード完了
     */
    sealed interface Ready : ImagePreviewUiState {
        val originalUri: Uri

        /**
         * オリジナル画像を表示
         */
        data class ShowingOriginal(override val originalUri: Uri) : Ready

        /**
         * 背景透過画像を表示
         */
        sealed interface ShowingTransparent : Ready {
            /**
             * 背景透過処理中
             */
            data class Loading(override val originalUri: Uri) : ShowingTransparent

            /**
             * 背景透過処理成功
             */
            data class Success(
                override val originalUri: Uri,
                val transparentUri: Uri
            ) : ShowingTransparent

            /**
             * 手動修正モード
             */
            data class ManualCorrection(
                override val originalUri: Uri,
                val transparentUri: Uri
            ) : ShowingTransparent
        }
    }
}
