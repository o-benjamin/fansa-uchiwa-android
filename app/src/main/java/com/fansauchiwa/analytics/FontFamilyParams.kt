package com.fansauchiwa.analytics

/**
 * フォントを表すパラメータ。select_edit_text・select_edit_text_font・export_uchiwa_font（#288）・
 * select_edit_all_text（target=font のとき。#308）で共通。
 * 「全体」タブで文字すべてのフォントをまとめて選んだときは、select_edit_text_font も1回送る（フォントの並び順の集計に入れるため）。
 *
 * GA4 のカスタムディメンションはパラメータ名で登録され、font_family は登録済み。
 * 名前を変えると、上の4つのイベントでフォント別の集計ができなくなるため変えない。
 */
object FontFamilyParams {
    /** フォントの enum 名（FontFamilies.name） */
    const val PARAM_FONT_FAMILY = "font_family"
}
