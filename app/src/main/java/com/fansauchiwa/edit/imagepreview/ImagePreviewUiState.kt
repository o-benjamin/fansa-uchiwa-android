package com.fansauchiwa.edit.imagepreview

import android.net.Uri

sealed interface ImagePreviewUiState {
    data object Loading : ImagePreviewUiState

    sealed interface Error : ImagePreviewUiState {
        val error: Throwable
    }

    data class LoadError(
        override val error: Throwable
    ) : Error

    sealed interface Success : ImagePreviewUiState {
        val originalUri: Uri
        val displayUri: Uri
        val isOriginalSelected: Boolean
        val isTransparentProcessing: Boolean
        val isManualCorrectionMode: Boolean
    }

    sealed interface Ready : Success {
        override val displayUri: Uri
            get() = originalUri

        override val isOriginalSelected: Boolean
            get() = true

        override val isTransparentProcessing: Boolean
            get() = false

        override val isManualCorrectionMode: Boolean
            get() = false

        data class ShowingOriginal(
            override val originalUri: Uri
        ) : Ready

        sealed interface ShowingTransparent : Ready {
            val transparentUri: Uri?

            override val displayUri: Uri
                get() = transparentUri ?: originalUri

            override val isOriginalSelected: Boolean
                get() = false

            data class Loading(
                override val originalUri: Uri
            ) : ShowingTransparent {
                override val transparentUri: Uri? = null
                override val isTransparentProcessing: Boolean = true
            }

            data class Success(
                override val originalUri: Uri,
                override val transparentUri: Uri
            ) : ShowingTransparent

            data class ManualCorrection(
                override val originalUri: Uri,
                override val transparentUri: Uri
            ) : ShowingTransparent {
                override val isManualCorrectionMode: Boolean = true
            }
        }
    }
}
