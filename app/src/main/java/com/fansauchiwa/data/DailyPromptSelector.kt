package com.fansauchiwa.data

import java.time.LocalDate

/**
 * 端末の今日の日付から、今日のお題を1件決める（#359）。同じ日なら全員が同じお題になる
 */
object DailyPromptSelector {

    /**
     * @param scheduled Remote Config の日付ごとのお題（[DailyPromptsParser.parse] の結果）。
     *   null は Remote Config が使えない（未設定・読めない）ことを、空の Map はお題を止めていることを表す
     * @param today 端末の今日の日付
     * @param defaults 今日の分がないときに使う控え
     * @return 今日のお題。止めているとき（と控えが空のとき）は null
     */
    fun select(
        scheduled: Map<LocalDate, DailyPrompt>?,
        today: LocalDate,
        defaults: List<DailyPrompt> = DailyPromptDefaults.PROMPTS
    ): DailyPrompt? {
        if (scheduled != null && scheduled.isEmpty()) return null
        return scheduled?.get(today) ?: pickDefault(today, defaults)
    }

    /** 日付の通し番号で控えを順に回す。端末どうしで同じ日なら同じ1件になる */
    private fun pickDefault(today: LocalDate, defaults: List<DailyPrompt>): DailyPrompt? {
        if (defaults.isEmpty()) return null
        return defaults[Math.floorMod(today.toEpochDay(), defaults.size.toLong()).toInt()]
    }
}
