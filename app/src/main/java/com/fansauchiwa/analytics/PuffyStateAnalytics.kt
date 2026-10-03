package com.fansauchiwa.analytics

import com.fansauchiwa.edit.PuffyState
import javax.inject.Inject

/**
 * tap_preview_export に付けるぷくぷくの状態（#268）を、保存済みのうちわから求める。
 *
 * うちわは [AnalyticsUchiwaReader] でDBから読む。
 * 計測をやめるときに消すものは [PuffyStateParams] の KDoc を参照。
 */
class PuffyStateAnalytics @Inject constructor(
    private val uchiwaReader: AnalyticsUchiwaReader
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return puffy_state のパラメータ。うちわを読めなかったとき（[AnalyticsUchiwaReader.readOrNull] が null）は空
     */
    suspend fun exportParams(uchiwaId: String?): Map<String, Any> {
        val uchiwa = uchiwaReader.readOrNull(uchiwaId) ?: return emptyMap()
        val state = PuffyState.of(uchiwa.decorations, uchiwa.isOverallBorderPuffyEnabled)
        return mapOf(PuffyStateParams.PARAM_PUFFY_STATE to state.toParamValue())
    }

    private fun PuffyState.toParamValue(): String = when (this) {
        PuffyState.ON -> PuffyStateParams.PUFFY_STATE_ON
        PuffyState.OFF -> PuffyStateParams.PUFFY_STATE_OFF
        PuffyState.MIXED -> PuffyStateParams.PUFFY_STATE_MIXED
    }
}
