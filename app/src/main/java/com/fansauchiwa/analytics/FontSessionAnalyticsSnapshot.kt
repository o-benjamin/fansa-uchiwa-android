package com.fansauchiwa.analytics

import com.fansauchiwa.edit.FontFamilies

/**
 * 保存してPreview画面へ進んだ時点の、編集セッションのフォント計測データ（#242）。
 *
 * [FontSessionTracker.finishSessionForPreview] が作り、tap_preview_export を送るまで
 * [FontSessionTracker] の中に持っておく（画面間の受け渡しにナビゲーション引数は使わない）。
 *
 * @param fontSwitchCount このセッション中に select_edit_text_font（フォント切り替え）が起きた回数
 * @param finalFont 最終的に選ばれたフォント（テキスト装飾が無い場合は null）
 * @param editStartTimeMillis 編集画面を開いた（このセッションが始まった）時刻
 */
internal data class FontSessionAnalyticsSnapshot(
    val fontSwitchCount: Int,
    val finalFont: FontFamilies?,
    val editStartTimeMillis: Long
)
