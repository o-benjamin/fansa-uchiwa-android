package com.fansauchiwa.analytics.featuredoor

import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsScreens

/**
 * 毎日開く機能の需要調査（#270・一時的）で送るイベントを作る
 *
 * 期限：v2.8.0 の公開から2週間後に判定し、消す（#280）。
 *
 * 判定：
 * - 分母はホームを表示したユーザー数（`screen_view` の `screen_name` = [AnalyticsScreens.HOME_SCREEN]）
 * - [AnalyticsActions.TAP_FEATURE_DOOR] を送ったユーザーが分母の8%以上、かつ
 *   [AnalyticsActions.ANSWER_FEATURE_DOOR] で [FeatureDoorAnswer.DAILY] と答えた人が答えた人の40%以上なら作る候補
 * - 押したユーザーが分母の3%未満なら需要なし
 *
 * 計測をやめるときに消すもの（#280）：
 * - このパッケージ（`analytics/featuredoor/`）と `home/featuredoor/`（入口とダイアログ）
 * - テスト：`test/.../analytics/featuredoor/`、`androidTest/.../home/featuredoor/`
 * - [AnalyticsActions.TAP_FEATURE_DOOR] と [AnalyticsActions.ANSWER_FEATURE_DOOR]
 * - `strings.xml` の `feature_door_*`
 * - `HomeScreen.kt` の `FeatureDoorSection` の item、`onFeatureDoorEvent` 引数とその受け渡し・プレビューでの指定、関係する import
 * - GA4 のカスタムディメンション `feature_door`・`feature_door_answer`（アーカイブする）
 */
object FeatureDoorAnalytics {
    /** 押された入口。値は [FeatureDoor.paramValue]（countdown / daily_prompt / anniversary） */
    const val PARAM_FEATURE_DOOR = "feature_door"

    /** ダイアログの答え。値は [FeatureDoorAnswer.paramValue]（daily / sometimes / not_needed） */
    const val PARAM_FEATURE_DOOR_ANSWER = "feature_door_answer"

    /** 入口を押したとき */
    fun tapEvent(door: FeatureDoor): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.TAP_FEATURE_DOOR,
        params = mapOf(PARAM_FEATURE_DOOR to door.paramValue)
    )

    /** ダイアログで答えたとき。答えずに閉じたときは送らない */
    fun answerEvent(door: FeatureDoor, answer: FeatureDoorAnswer): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.ANSWER_FEATURE_DOOR,
        params = mapOf(
            PARAM_FEATURE_DOOR to door.paramValue,
            PARAM_FEATURE_DOOR_ANSWER to answer.paramValue
        )
    )
}
