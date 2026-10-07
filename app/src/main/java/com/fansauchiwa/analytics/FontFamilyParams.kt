package com.fansauchiwa.analytics

/**
 * フォントを表すパラメータ。select_edit_text・select_edit_text_font・export_uchiwa_font（#288）で共通。
 *
 * GA4 のカスタムディメンションはパラメータ名で登録され、font_family は登録済み。
 * 名前を変えると、上の3つのイベントでフォント別の集計ができなくなるため変えない。
 */
object FontFamilyParams {
    /** フォントの enum 名（FontFamilies.name） */
    const val PARAM_FONT_FAMILY = "font_family"
}
