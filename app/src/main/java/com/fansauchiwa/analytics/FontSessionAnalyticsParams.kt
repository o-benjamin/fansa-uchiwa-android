package com.fansauchiwa.analytics

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
     * 今回の最終的なフォントが、直前に保存したうちわのテキスト装飾のどれかに使われているか。
     * 「直前に保存したうちわ」は、うちわ画像ファイルの更新日時が新しい順（ホーム画面と同じ並び。
     * 複製したうちわも含む）で、今回のうちわを除いた先頭。直前のうちわが無ければ "false"。
     * 同じPreview画面で2回保存しても、比べる相手は直前のうちわのまま（自分自身とは比べない）。
     *
     * v2.8.0 から定義を変更した（#267）。v2.7.x は「前回ギャラリー保存に成功したうちわの最終フォント
     * 1つと一致するか」だった（2回目の保存は自分自身と比べて "true" になっていた）。
     * 前後の値は意味が違うため、GA4 では app_version で分けて集計すること。
     *
     * true/falseではなく "true"/"false" の文字列で送る。
     * GA4 のカスタムディメンションの型をテキストに統一するため（既存の is_new 等と同じ、#249）。
     */
    const val FONT_SAME_AS_LAST = "font_same_as_last"
    const val EDIT_DURATION_BUCKET = "edit_duration_bucket"
}
