package com.fansauchiwa.data

import java.time.LocalDate

/**
 * 端末の今日の日付から、今日のお題を1件決める（#359）。同じ日なら全員が同じお題になる
 */
object DailyPromptSelector {

    /**
     * @param config Remote Config の `daily_prompts` を読んだ結果
     * @param today 端末の今日の日付
     * @param defaults 未設定・今日の分がないときに使う控え
     * @return 今日のお題。止めているとき（と控えが空のとき）は null
     */
    fun select(
        config: DailyPromptsConfig,
        today: LocalDate,
        defaults: List<DailyPrompt> = DailyPromptDefaults.PROMPTS
    ): DailyPrompt? =
        when (config) {
            DailyPromptsConfig.Stopped -> null
            DailyPromptsConfig.Unset -> pickDefault(today, defaults)
            is DailyPromptsConfig.Scheduled -> config.prompts[today] ?: pickDefault(today, defaults)
        }

    /**
     * 日付の通し番号で控えを順に回す。端末どうしで同じ日なら同じ1件になる。
     * `mod` は割る数と同じ符号の余りを返すので、1970年より前で通し番号が負でも範囲に収まる
     */
    private fun pickDefault(today: LocalDate, defaults: List<DailyPrompt>): DailyPrompt? {
        if (defaults.isEmpty()) return null
        return defaults[today.toEpochDay().mod(defaults.size)]
    }
}
