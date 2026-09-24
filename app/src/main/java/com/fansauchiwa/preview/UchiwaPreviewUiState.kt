package com.fansauchiwa.preview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UchiwaPreviewUiState(
    val imagePath: String? = null,
    val saveSuccess: Boolean? = null,
    val isLoadingAd: Boolean = false,
    val isSaveButtonPressed: Boolean = false,
    val shareImagePath: String? = null,
    // フォントが「迷い」か「楽しみ」かを見分けるための計測（#242）。Edit画面から渡される。
    val fontSwitchCount: Int = 0,
    val finalFontName: String? = null,
    val editStartTimeMillis: Long = 0L
) : Parcelable

