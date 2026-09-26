package com.fansauchiwa.analytics

/**
 * [AnalyticsActions.TAP_PREVIEW_EXPORT] に付ける、ぷくぷくの状態のパラメータ（#268）
 *
 * ぷくぷくをうちわ全体のトグル1つにしたあと、保存されたうちわのうちどれだけがぷくぷくかを見る。
 *
 * 一時的な計測。#268 の答え合わせ（公開4週間後に、保存のうち puffy_state=on の割合を見る）が
 * 済んだら消してよい。
 * 計測をやめるときは、このクラスと [PuffyStateAnalytics]・それぞれのテストを消し、
 * UchiwaPreviewViewModel の puffyStateAnalytics（コンストラクタ引数と logExportEvent での呼び出し）を消す。
 * edit/PuffyState.kt は編集画面のトグルでも使うので消さない。
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
