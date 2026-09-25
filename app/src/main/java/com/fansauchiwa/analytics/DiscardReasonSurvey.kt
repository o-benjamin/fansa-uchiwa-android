package com.fansauchiwa.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 編集を破棄した理由を選択肢で1問だけ聞く調査（#265・一時的）。
 *
 * 編集画面の戻るダイアログで「破棄」を選んだ直後に聞き、答えを answer_discard_reason の discard_reason で送る。
 * 聞きすぎないよう、アプリを起動してから1回だけ聞く（2回目以降の破棄では聞かずにそのまま戻る）。
 * 聞いたかどうかはメモリにだけ持ち、設定（DataStore）・DB には入れない（`.agents/analytics.md`）。
 * そのため、アプリを起動し直すとまた1回聞く。
 *
 * 調査の終了条件は、v2.8.0 の製品版公開から数えて「回答が300件たまる、または4週間たつ」の早いほう。
 * 削除作業は #279。
 *
 * 計測をやめるときに消すもの：
 * - このクラス、[DiscardReason]、[DiscardReasonParams]、edit/DiscardReasonSurveyDialog.kt
 * - [AnalyticsActions.ANSWER_DISCARD_REASON]、strings.xml の discard_reason_*
 * - EditViewModel の discardReasonSurvey 引数と、consumeDiscardReasonAskChance・answerDiscardReason
 * - EditScreen の showDiscardReasonDialog と、「破棄」ボタンの分岐・DiscardReasonSurveyDialog の呼び出し
 *   （「破棄」を押したらすぐ onBack() する元の形に戻す）
 * - テスト：DiscardReasonSurveyTest、EditViewModelTest の「破棄した理由の調査（#265・一時的）」region、
 *   EditViewModelTest・DuplicateDecorationTest の `discardReasonSurvey =` 引数
 * - GA4：カスタムディメンション discard_reason をアーカイブする（登録していれば）
 */
@Singleton
class DiscardReasonSurvey @Inject constructor() {

    private var hasAsked = false

    /**
     * 今回の破棄で理由を聞けるかどうかを返し、聞ける場合はその1回を使ったものとして記録する。
     * そのため、true を返すのはアプリを起動してから最初の1回だけ。
     */
    fun tryConsumeAskChance(): Boolean {
        if (hasAsked) return false
        hasAsked = true
        return true
    }

    /** 答えを送るイベントを作る */
    fun answerEvent(reason: DiscardReason): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.ANSWER_DISCARD_REASON,
        params = mapOf(DiscardReasonParams.PARAM_DISCARD_REASON to reason.paramValue)
    )
}
