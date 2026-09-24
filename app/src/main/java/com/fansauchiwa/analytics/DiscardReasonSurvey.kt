package com.fansauchiwa.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 編集を破棄した理由を選択肢で1問だけ聞く調査（#265・一時的）。
 *
 * 編集画面の戻るダイアログで「破棄」を選んだ直後に聞き、答えを answer_discard_reason の reason で送る。
 * 聞きすぎないよう、アプリを起動してから1回だけ聞く（2回目以降の破棄では聞かずにそのまま戻る）。
 * 聞いたかどうかはメモリにだけ持ち、設定（DataStore）・DB には入れない（`.agents/analytics.md`）。
 * そのため、アプリを起動し直すとまた1回聞く。
 *
 * 調査の終了条件は「回答が300件たまる、または4週間たつ」の早いほう（#265）。
 * 計測をやめるときは、このクラスと [DiscardReason]・edit/DiscardReasonSurveyDialog.kt・
 * strings.xml の discard_reason_*・[AnalyticsActions.ANSWER_DISCARD_REASON] を消し、
 * EditViewModel と EditScreen からの呼び出しを消す（破棄したらそのまま戻る元の動きに戻る）。
 */
@Singleton
class DiscardReasonSurvey @Inject constructor() {

    private var hasAsked = false

    /**
     * 今回の破棄で理由を聞くかどうかを返す。true を返したら聞いたものとして数え、以降は false を返す。
     */
    fun shouldAsk(): Boolean {
        if (hasAsked) return false
        hasAsked = true
        return true
    }

    /** 答えを送るイベントを作る */
    fun answerEvent(reason: DiscardReason): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.ANSWER_DISCARD_REASON,
        params = mapOf(PARAM_REASON to reason.paramValue)
    )

    companion object {
        const val PARAM_REASON = "reason"
    }
}
