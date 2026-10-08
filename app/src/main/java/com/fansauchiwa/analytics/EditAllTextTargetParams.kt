package com.fansauchiwa.analytics

/**
 * select_edit_all_text（「全体」タブで、すべての文字をまとめて変えた。#308）の target の値。
 * 文字を1つずつ変える select_edit_text_* とはイベントを分け、まとめて変える操作を使ったかを
 * イベント名だけで数えられるようにしている。
 *
 * 既存の select_edit_text_* は「イベント名＝何を変えたか（color・font・weight）、target＝どの部分か（text・stroke_1）」だが、
 * こちらはイベントが1つなので target に部分と項目を合わせて入れる（stroke_1_color など）。
 * GA4 で target = stroke_1 と絞り込んでも、まとめて変えた分は出てこない
 */
object EditAllTextTargetParams {
    /** フォント。font_family（FontFamilyParams）も付ける */
    const val FONT = "font"
    const val TEXT_COLOR = "text_color"
    const val STROKE_1_COLOR = "stroke_1_color"
    const val STROKE_1_WEIGHT = "stroke_1_weight"
}
