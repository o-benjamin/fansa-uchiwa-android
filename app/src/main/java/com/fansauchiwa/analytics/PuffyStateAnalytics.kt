package com.fansauchiwa.analytics

import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.edit.PuffyState
import javax.inject.Inject

/**
 * tap_preview_export に付けるぷくぷくの状態（#268）を、保存済みのうちわから求める。
 *
 * Preview画面へ進む前にうちわはDBに保存されているので、分析のために状態を持ち回らず、DBから読む。
 */
class PuffyStateAnalytics @Inject constructor(
    private val localDatabaseRepository: LocalDatabaseRepository
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return puffy_state のパラメータ。うちわが見つからなければ空
     */
    suspend fun exportParams(uchiwaId: String?): Map<String, Any> {
        val uchiwa = uchiwaId?.let { localDatabaseRepository.getUchiwa(it) } ?: return emptyMap()
        val state = PuffyState.of(uchiwa.decorations, uchiwa.isOverallBorderPuffyEnabled)
        return mapOf(PuffyStateParams.PARAM_PUFFY_STATE to state.toParamValue())
    }

    private fun PuffyState.toParamValue(): String = when (this) {
        PuffyState.ON -> PuffyStateParams.PUFFY_STATE_ON
        PuffyState.OFF -> PuffyStateParams.PUFFY_STATE_OFF
        PuffyState.MIXED -> PuffyStateParams.PUFFY_STATE_MIXED
    }
}
