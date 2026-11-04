package com.fansauchiwa.data

import java.time.LocalDate

/**
 * Remote Config の `daily_prompts` を読んだ結果（[DailyPromptsParser.parse]）
 *
 * 値が空のときは控えを出し、`{"prompts": []}` のときは止める。ほかのパラメータと逆の決まりなので、状態を型で分けている
 */
sealed interface DailyPromptsConfig {

    /** 値が空（未設定）か、設定の誤りで読めない。同梱の控えを出す */
    data object Unset : DailyPromptsConfig

    /** `{"prompts": []}` を公開して、お題を止めている */
    data object Stopped : DailyPromptsConfig

    /**
     * 日付ごとのお題。今日の分がなければ同梱の控えを出す
     *
     * @param droppedPositions 決まりに合わずに捨てた項目の、`prompts` の中での位置（1始まり）。ログに出して原因を追うため
     */
    data class Scheduled(
        val prompts: Map<LocalDate, DailyPrompt>,
        val droppedPositions: List<Int> = emptyList()
    ) : DailyPromptsConfig
}
