package com.fansauchiwa.analytics

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 今日のお題のカードが出たこと（view_daily_prompt、#360）を、1つのお題につき1回だけ送るための記録
 *
 * カードはスクロールで隠れて戻るたび・タブを切り替えるたびに出直すので、そのたびに送らないよう、送ったお題の id を覚えておく。
 * メモリにだけ持つので、アプリのプロセスが作り直されると同じお題をもう1回送る（GA4 ではお題ごとのユーザー数で見るので、件数が1つ増えても集計の結論は変わらない）。
 *
 * 計測をやめるときに消すもの：[DailyPromptAnalyticsParams] の KDoc を参照
 */
@Singleton
class DailyPromptViewTracker @Inject constructor() {

    // HomeViewModel から呼ばれる @Singleton。呼ぶスレッドを決めていないので、並行に触っても壊れない集合にする
    private val loggedPromptIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** はじめて出たお題なら送るイベントを返し、前に送ったお題なら null を返す */
    fun viewEventIfFirstShown(promptId: String): AnalyticsEvent? {
        if (!loggedPromptIds.add(promptId)) return null
        return AnalyticsEvent(
            AnalyticsActions.VIEW_DAILY_PROMPT,
            mapOf(DailyPromptAnalyticsParams.PARAM_PROMPT_ID to promptId)
        )
    }
}
