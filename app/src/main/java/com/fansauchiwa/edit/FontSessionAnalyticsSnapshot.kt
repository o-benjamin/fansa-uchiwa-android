package com.fansauchiwa.edit

/**
 * 編集セッション中のフォント計測データのスナップショット（#242）。
 *
 * Preview画面へ渡すために [EditViewModel.consumeFontSessionForPreview] が返す。
 *
 * @param fontSwitchCount このセッション中に select_edit_text_font（フォント切り替え）が起きた回数
 * @param finalFontName 最終的に選ばれたフォントの名前（テキスト装飾が無い場合は null）
 * @param sessionStartTimeMillis 編集画面を開いた（このセッションが始まった）時刻
 */
data class FontSessionAnalyticsSnapshot(
    val fontSwitchCount: Int,
    val finalFontName: String?,
    val sessionStartTimeMillis: Long
)
