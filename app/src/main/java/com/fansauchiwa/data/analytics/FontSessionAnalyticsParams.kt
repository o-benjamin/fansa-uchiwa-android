package com.fansauchiwa.data.analytics

/**
 * フォント選びが「迷い」か「楽しみ」かを見分けるためのパラメータ（#242）
 *
 * FONT_SWITCH_BUCKET と EDIT_DURATION_BUCKET は [AnalyticsActions.TAP_PREVIEW_EXPORT] と
 * [AnalyticsActions.TAP_EDIT_BACK_DIALOG]（action=[AnalyticsBackDialogActions.ACTION_DELETE]）の
 * 両方に付ける。保存側にだけ付けると保存まで到達した人しか記録されず、
 * 諦めた人のデータが丸ごと欠けるため（選択バイアス）。
 * FINAL_FONT_RANK_BUCKET と FONT_SAME_AS_LAST は TAP_PREVIEW_EXPORT のみに付ける。
 */
object FontSessionAnalyticsParams {
    const val FONT_SWITCH_BUCKET = "font_switch_bucket"
    const val FINAL_FONT_RANK_BUCKET = "final_font_rank_bucket"

    /**
     * true/falseではなく "true"/"false" の文字列で送る。
     * GA4 のカスタムディメンションの型をテキストに統一するため（既存の is_new 等と同じ、#249）。
     */
    const val FONT_SAME_AS_LAST = "font_same_as_last"
    const val EDIT_DURATION_BUCKET = "edit_duration_bucket"
}
