package com.fansauchiwa.analytics

/**
 * [AnalyticsActions.EXPORT_UCHIWA_FONT] のパラメータ（#288）
 *
 * フォントの選択（select_edit_text_font）は、選んだあと消したフォントも数える。
 * ギャラリーへ保存できたうちわに残ったフォントを数え、使われないフォントを削る判断に使う。
 *
 * パラメータ名は select_edit_text_font と同じ font_family にしている。GA4 のカスタムディメンションは
 * パラメータ名で登録されるため、登録済みの font_family がこのイベントにもそのまま使える。
 */
object ExportedFontParams {
    /** フォントの enum 名（FontFamilies.name）。select_edit_text_font の font_family と同じ値 */
    const val PARAM_FONT_FAMILY = "font_family"
}
