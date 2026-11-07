package com.fansauchiwa.analytics

/**
 * [AnalyticsActions.TAP_PREVIEW_SHARE]・[AnalyticsActions.SELECT_SHARE_TARGET] のパラメータ
 *
 * [PARAM_ENTRY_POINT] は、共有ボタンからの共有と、保存完了のダイアログからの共有を見分けるために付ける（#244）。
 * イベント名は変えないので、tap_preview_share の回数はこれまでどおり数えられる。
 */
object ShareAnalyticsParams {
    const val PARAM_ENTRY_POINT = "entry_point"

    /**
     * 共有の画面で選ばれたアプリのパッケージ名（#362。例 `com.twitter.android`）
     *
     * GA4 のカスタムディメンション `target` は編集画面のイベント（文字・ステッカーのどちらを変えたか）でも使っているので、
     * イベント名で分けて読む。
     */
    const val PARAM_TARGET = "target"

    /** プレビュー画面の共有ボタン（従来からある導線） */
    const val ENTRY_POINT_PREVIEW_BUTTON = "preview_button"

    /** 保存完了のダイアログの「SNSでシェア」 */
    const val ENTRY_POINT_AFTER_SAVE = "after_save"

    /**
     * [AnalyticsActions.SELECT_SHARE_TARGET] のパラメータ。選ばれたアプリが分からないときは null（送らない）
     *
     * @param chosenPackage 選ばれたアプリのパッケージ名
     * @param entryPoint tap_preview_share に付けたのと同じ導線（ENTRY_POINT_*）。分からないときは付けない
     * @param isStartedFromDailyPrompt お題から作ったうちわか（`source=daily_prompt` を付けるか。#360）
     */
    fun selectShareTargetParams(
        chosenPackage: String?,
        entryPoint: String?,
        isStartedFromDailyPrompt: Boolean
    ): Map<String, Any>? {
        if (chosenPackage.isNullOrBlank()) return null
        return buildMap {
            put(PARAM_TARGET, chosenPackage)
            if (entryPoint != null) put(PARAM_ENTRY_POINT, entryPoint)
            putAll(DailyPromptAnalyticsParams.sourceParams(isStartedFromDailyPrompt))
        }
    }
}
