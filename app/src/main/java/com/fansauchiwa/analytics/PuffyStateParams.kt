package com.fansauchiwa.analytics

/**
 * [AnalyticsActions.TAP_PREVIEW_EXPORT] に付ける、ぷくぷくの状態のパラメータ（#268）
 *
 * ぷくぷくをうちわ全体のトグル1つにしたあと、保存されたうちわのうちどれだけがぷくぷくかを見る。
 */
object PuffyStateParams {
    const val PARAM_PUFFY_STATE = "puffy_state"

    /** 文字・ステッカー・フチがすべてぷくぷく */
    const val PUFFY_STATE_ON = "on"

    /** ぷくぷくが1つもない */
    const val PUFFY_STATE_OFF = "off"

    /** 一部だけぷくぷく（前のバージョンで部品ごとに切り替えて保存したうちわ） */
    const val PUFFY_STATE_MIXED = "mixed"
}
