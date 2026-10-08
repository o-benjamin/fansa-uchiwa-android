package com.fansauchiwa.analytics

/**
 * select_edit_all_text（「全体」タブで、すべての文字をまとめて変えた。#308）の target の値。
 * 文字を1つずつ変える select_edit_text_* とはイベントを分け、まとめて変える操作を使ったかを
 * イベント名だけで数えられるようにしている
 */
object EditAllTextTargetParams {
    /** フォント。font_family（FontFamilyParams）も付ける */
    const val FONT = "font"
    const val TEXT_COLOR = "text_color"
    const val STROKE_1_COLOR = "stroke_1_color"
    const val STROKE_1_WEIGHT = "stroke_1_weight"
}
