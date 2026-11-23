package com.fansauchiwa.update

data class UpdatePromptUiState(
    // 出している更新のダイアログ（MIN・FORCE_FALLBACK・FORCE）。null なら出さない。柔軟な更新は Play の画面なので、ここには入らない
    val dialog: UpdatePromptType? = null,
    // 柔軟な更新を出すと決まり、まだ Play の画面を出していない（ホームが出ているときに出すため）
    val isFlexibleUpdatePending: Boolean = false,
    // 柔軟な更新のダウンロードが終わり、再起動すれば入る
    val isUpdateDownloaded: Boolean = false
)
