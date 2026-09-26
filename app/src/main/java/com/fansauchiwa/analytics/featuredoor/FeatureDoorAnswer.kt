package com.fansauchiwa.analytics.featuredoor

import androidx.annotation.StringRes
import com.fansauchiwa.R

/**
 * 準備中のダイアログで聞く「この機能ができたら使いたいか」の答え（#270・一時的）
 *
 * @param paramValue GA4 の `feature_door_answer` パラメータに送る値。レポートの集計に使うので変えない
 */
enum class FeatureDoorAnswer(
    val paramValue: String,
    @StringRes val labelResId: Int
) {
    DAILY(paramValue = "daily", labelResId = R.string.feature_door_answer_daily),
    SOMETIMES(paramValue = "sometimes", labelResId = R.string.feature_door_answer_sometimes),
    NOT_NEEDED(paramValue = "not_needed", labelResId = R.string.feature_door_answer_not_needed)
}
