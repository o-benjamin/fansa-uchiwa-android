package com.fansauchiwa.analytics.featuredoor

import androidx.annotation.StringRes
import com.fansauchiwa.R

/**
 * 需要を確かめるためにホームに入口だけ置く「毎日開く機能」の候補（#270・一時的）
 *
 * 機能そのものは作っていない。押されたら準備中のダイアログを出す（フェイクドア）。
 *
 * @param paramValue GA4 の `feature_door` パラメータに送る値。レポートの集計に使うので変えない
 */
enum class FeatureDoor(
    val paramValue: String,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int
) {
    /** A 推し活カウントダウン */
    COUNTDOWN(
        paramValue = "countdown",
        titleResId = R.string.feature_door_countdown_title,
        descriptionResId = R.string.feature_door_countdown_description
    ),

    /** B 今日のファンサお題 */
    DAILY_PROMPT(
        paramValue = "daily_prompt",
        titleResId = R.string.feature_door_daily_prompt_title,
        descriptionResId = R.string.feature_door_daily_prompt_description
    ),

    /** C 推しの記念日帳 */
    ANNIVERSARY(
        paramValue = "anniversary",
        titleResId = R.string.feature_door_anniversary_title,
        descriptionResId = R.string.feature_door_anniversary_description
    )
}
