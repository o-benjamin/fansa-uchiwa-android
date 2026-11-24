package com.fansauchiwa.analytics

import com.fansauchiwa.update.UpdatePromptType

/**
 * 更新の案内（#428）の計測のパラメータ
 *
 * - [AnalyticsActions.VIEW_UPDATE_PROMPT]：案内を出したとき。柔軟な更新は Play の画面を出せたとき、
 *   ほかはダイアログを出したとき（閉じられないダイアログは、ホームの ViewModel ごと作り直されたときにもう一度送る。画面の回転では送らない）
 * - [AnalyticsActions.TAP_UPDATE_PROMPT]：更新を選んだとき。柔軟な更新は Play の画面で更新を選んだとき、ほかはダイアログの「更新する」
 *
 * どちらにも [PARAM_SOURCE] で案内の種類を付ける。`force_fallback` は `force_version_code` より古いが、
 * Play で直った版に即時更新できると確かめられず、閉じられるダイアログに下げた端末（`force` の効き目は `force` だけで見る）。
 * パラメータ名は GA4 にカスタムディメンションとして登録済みの `source`（[DailyPromptAnalyticsParams] と同じ名前）を使い、
 * 登録を待たずに初日から種類ごとに見られるようにした。イベント名で絞れば、お題の `source` とは混ざらない
 */
object UpdatePromptAnalyticsParams {
    const val PARAM_SOURCE = "source"

    const val SOURCE_FLEXIBLE = "flexible"
    const val SOURCE_MIN = "min"
    const val SOURCE_FORCE_FALLBACK = "force_fallback"
    const val SOURCE_FORCE = "force"

    /**
     * @return 案内を出さない [UpdatePromptType.NONE] のときは空
     */
    fun params(type: UpdatePromptType): Map<String, Any> = when (type) {
        UpdatePromptType.NONE -> emptyMap()
        UpdatePromptType.FLEXIBLE -> mapOf(PARAM_SOURCE to SOURCE_FLEXIBLE)
        UpdatePromptType.MIN -> mapOf(PARAM_SOURCE to SOURCE_MIN)
        UpdatePromptType.FORCE_FALLBACK -> mapOf(PARAM_SOURCE to SOURCE_FORCE_FALLBACK)
        UpdatePromptType.FORCE -> mapOf(PARAM_SOURCE to SOURCE_FORCE)
    }
}
