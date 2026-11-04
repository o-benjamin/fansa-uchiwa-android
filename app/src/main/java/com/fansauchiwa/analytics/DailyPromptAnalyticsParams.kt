package com.fansauchiwa.analytics

/**
 * 今日のお題（#360）の計測のパラメータ
 *
 * - [AnalyticsActions.VIEW_DAILY_PROMPT]・[AnalyticsActions.TAP_DAILY_PROMPT] には [PARAM_PROMPT_ID] を付ける
 * - お題から作ったうちわの [AnalyticsActions.TAP_PREVIEW_EXPORT]・[AnalyticsActions.TAP_PREVIEW_SHARE] には、
 *   [sourceParams] で `source=daily_prompt` を付ける。ほかのうちわには何も付けない（今までの集計は変わらない）
 *
 * お題から作ったかどうかは、共有文のタグにも使うので `DailyPromptUchiwaRepository` が持つ（分析のためだけの値ではないため）。
 *
 * 計測をやめるときに消すもの：
 * - このファイルと、AnalyticsActions の VIEW_DAILY_PROMPT・TAP_DAILY_PROMPT
 * - HomeViewModel の onDailyPromptShown（と DailyPromptCard の LaunchedEffect）、onDailyPromptCreate の logEvent
 * - UchiwaPreviewViewModel の logExportEvent と showRewardedAdAndShare で sourceParams を足しているところ
 * - テスト：HomeViewModelTest のお題の計測、UchiwaPreviewSaveTest と UchiwaPreviewShareTest の source のテスト
 */
object DailyPromptAnalyticsParams {
    const val PARAM_PROMPT_ID = "prompt_id"
    const val PARAM_SOURCE = "source"

    /** お題から作ったうちわ */
    const val SOURCE_DAILY_PROMPT = "daily_prompt"

    fun sourceParams(isStartedFromDailyPrompt: Boolean): Map<String, Any> =
        if (isStartedFromDailyPrompt) {
            mapOf(PARAM_SOURCE to SOURCE_DAILY_PROMPT)
        } else {
            emptyMap()
        }
}
