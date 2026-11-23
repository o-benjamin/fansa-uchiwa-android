package com.fansauchiwa.update

data class UpdatePromptUiState(
    // 出している更新のダイアログ（MIN か FORCE）。null なら出さない。柔軟な更新は Play の画面なので、ここには入らない
    val dialog: UpdatePromptType? = null,
    // 柔軟な更新のダウンロードが終わり、再起動すれば入る
    val isUpdateDownloaded: Boolean = false
)
