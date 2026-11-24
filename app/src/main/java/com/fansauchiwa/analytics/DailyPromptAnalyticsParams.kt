package com.fansauchiwa.analytics

import com.fansauchiwa.data.DailyPrompt

/**
 * 今日のお題（#360）の計測のパラメータ
 *
 * - [AnalyticsActions.VIEW_DAILY_PROMPT]・[AnalyticsActions.TAP_DAILY_PROMPT] には [PARAM_PROMPT_ID] を付ける
 * - お題のカードの副ボタンから AI素材の入口を開いたときの [AnalyticsActions.TAP_AI_MATERIAL_ENTRY]（#340）にも
 *   [PARAM_PROMPT_ID] を付ける。どのお題の日に AI素材が使われたかを見るため
 * - アプリ内メッセージ（#428）のボタンから開いたときの [AnalyticsActions.TAP_DAILY_PROMPT]・[AnalyticsActions.TAP_AI_MATERIAL_ENTRY] には、
 *   [tapParams] で `source=in_app_message` も付ける。カードから押した数と分けるため（カードから押したときは付けない）
 * - お題から作ったうちわの [AnalyticsActions.TAP_PREVIEW_EXPORT]・[AnalyticsActions.TAP_PREVIEW_SHARE] には、
 *   [sourceParams] で `source=daily_prompt` を付ける。ほかのうちわには何も付けない（今までの集計は変わらない）
 *
 * お題から作ったかどうかは、共有文のタグにも使うので `DailyPromptUchiwaRepository` が持つ（分析のためだけの値ではないため）。
 *
 * パラメータ名 `source` は Firebase の定義済み `FirebaseAnalytics.Param.SOURCE`（流入元。campaign_details で使う）と同じ名前だが、
 * GA4 ではイベントのスコープのカスタムディメンション `source` として登録済みで、流入元のディメンションとは別に集計される。
 * 名前を変えると集計が2つの名前に割れるので変えない。ほかの計測で「どこから作ったうちわか」を送るときも同じ名前を使う。
 *
 * 計測をやめるときに消すもの：
 * - このファイル、[DailyPromptViewTracker]、AnalyticsActions の VIEW_DAILY_PROMPT・TAP_DAILY_PROMPT
 * - HomeViewModel の onDailyPromptShown、onDailyPromptCreate の logEvent。onDailyPromptCreateWithAiMaterial は
 *   AnalyticsActions の TAP_AI_MATERIAL_ENTRY（AI素材の計測）を残して prompt_id だけ外し、KDoc の tap_daily_prompt の説明を消す
 * - DailyPromptCard の onShown と LaunchedEffect、HomeScreen.kt の onDailyPromptShown の受け渡し
 *   （HomeTabContent・HomeTabHomeContent・プレビュー）、HomeScreenTest の引数
 * - UchiwaPreviewViewModel の logExportEvent と showRewardedAdAndShare、ShareAnalyticsParams の selectShareTargetParams
 *   で sourceParams を足しているところ（select_share_target の source。#362）
 * - テスト：DailyPromptViewTrackerTest、HomeViewModelTest のお題の計測（onDailyPromptCreateWithAiMaterial_… の
 *   prompt_id と tap_daily_prompt の確認を含む）、UchiwaPreviewSaveTest と
 *   UchiwaPreviewShareTest の source のテスト、ShareAnalyticsParamsTest の
 *   selectShareTargetParams_StartedFromDailyPrompt_AddsSourceDailyPrompt、ShareTargetReceiverTest の
 *   paramsFrom_ChosenFromDailyPromptUchiwa_ReturnsTargetEntryPointAndSource（期待から source を外す）、
 *   DailyPromptCardTest の onShown（setCard の引数と、onShown が1回呼ばれることの確認。お題の文言が出ることの確認は残す）
 * - GA4：カスタムディメンション `prompt_id` と、ほかのイベントで使っていなければ `source` をアーカイブする
 *   （更新の案内の [UpdatePromptAnalyticsParams] も `source` を使っている）
 */
object DailyPromptAnalyticsParams {
    const val PARAM_PROMPT_ID = "prompt_id"
    const val PARAM_SOURCE = "source"

    /** お題から作ったうちわ */
    const val SOURCE_DAILY_PROMPT = "daily_prompt"

    /** アプリ内メッセージのボタンから、お題・AI素材を開いた */
    const val SOURCE_IN_APP_MESSAGE = "in_app_message"

    /**
     * [AnalyticsActions.TAP_DAILY_PROMPT]・[AnalyticsActions.TAP_AI_MATERIAL_ENTRY] のパラメータ。
     * [source] はカード以外から開いたときだけ渡す（[SOURCE_IN_APP_MESSAGE]）
     */
    fun tapParams(prompt: DailyPrompt, source: String? = null): Map<String, Any> =
        buildMap {
            put(PARAM_PROMPT_ID, prompt.id)
            source?.let { put(PARAM_SOURCE, it) }
        }

    fun sourceParams(isStartedFromDailyPrompt: Boolean): Map<String, Any> =
        if (isStartedFromDailyPrompt) {
            mapOf(PARAM_SOURCE to SOURCE_DAILY_PROMPT)
        } else {
            emptyMap()
        }
}
