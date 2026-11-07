package com.fansauchiwa.analytics

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 今日のお題のカードが出たこと（view_daily_prompt、#360）を、1つのお題につき1回だけ送るための記録
 *
 * カードはスクロールで隠れて戻るたび・タブを切り替えるたびに出直すので、そのたびに送らないよう、送ったお題の id を覚えておく。
 * メモリにだけ持つので、アプリのプロセスが作り直されると同じお題をもう1回送る（人数で見るので、判定には影響しない）。
 *
 * 計測をやめるときに消すもの：[DailyPromptAnalyticsParams] の KDoc を参照
 */
@Singleton
class DailyPromptViewTracker @Inject constructor() {

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
