package com.fansauchiwa.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 今日のファンサお題（#358）。同じ日なら全員が同じお題を見る
 *
 * Remote Config の `daily_prompts` から作り（[DailyPromptsParser]）、その日の分がなければ同梱の控え（[DailyPromptDefaults]）から選ぶ
 * （[DailyPromptSelector]）。
 *
 * @param id 計測に使う名前（`view_daily_prompt` などの `prompt_id`）。同梱の控えは `default_` で始まる
 * @param text お題の文言。[DailyPromptsParser.MAX_TEXT_LENGTH] 文字まで
 */
@Parcelize
data class DailyPrompt(
    val id: String,
    val text: String
) : Parcelable
