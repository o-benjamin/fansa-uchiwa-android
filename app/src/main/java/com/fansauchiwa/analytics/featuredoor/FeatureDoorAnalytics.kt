package com.fansauchiwa.analytics.featuredoor

import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent

/**
 * 毎日開く機能の需要調査（#270・一時的）で送るイベントを作る
 *
 * 判定：入口を押した人がホーム表示人数の8%以上、かつ「毎日」が40%以上なら作る候補、
 * 押した人が3%未満なら需要なしとする。
 *
 * 計測をやめるときに消すもの：
 * - このパッケージ（`analytics/featuredoor/`）と、対応するテスト（`test/.../analytics/featuredoor/`）
 * - [AnalyticsActions.TAP_FEATURE_DOOR] と [AnalyticsActions.ANSWER_FEATURE_DOOR]
 * - `strings.xml` の `feature_door_*`
 * - `HomeScreen.kt` の `FeatureDoorSection` の呼び出しと `onFeatureDoorEvent`
 */
object FeatureDoorAnalytics {
    /** 押された入口。値は [FeatureDoor.paramValue]（a/b/c） */
    const val PARAM_FEATURE = "feature"

    /** ダイアログの答え。値は [FeatureDoorAnswer.paramValue] */
    const val PARAM_ANSWER = "answer"

    /** 入口を押したとき */
    fun tapEvent(door: FeatureDoor): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.TAP_FEATURE_DOOR,
        params = mapOf(PARAM_FEATURE to door.paramValue)
    )

    /** ダイアログで答えたとき。答えずに閉じたときは送らない */
    fun answerEvent(door: FeatureDoor, answer: FeatureDoorAnswer): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.ANSWER_FEATURE_DOOR,
        params = mapOf(
            PARAM_FEATURE to door.paramValue,
            PARAM_ANSWER to answer.paramValue
        )
    )
}
