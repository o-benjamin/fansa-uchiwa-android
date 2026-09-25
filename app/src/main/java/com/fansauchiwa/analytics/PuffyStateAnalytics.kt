package com.fansauchiwa.analytics

import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.edit.PuffyState
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

/**
 * tap_preview_export に付けるぷくぷくの状態（#268）を、保存済みのうちわから求める。
 *
 * Preview画面へ進む前にうちわはDBに保存されているので、分析のために状態を持ち回らず、DBから読む。
 * 計測をやめるときに消すものは [PuffyStateParams] の KDoc を参照。
 */
class PuffyStateAnalytics @Inject constructor(
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val crashReportingRepository: CrashReportingRepository
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return puffy_state のパラメータ。うちわが見つからないとき・読み込みに失敗したときは空
     *   （失敗しても保存を止めないよう、例外は記録して投げない）
     */
    suspend fun exportParams(uchiwaId: String?): Map<String, Any> {
        if (uchiwaId == null) return emptyMap()
        val uchiwa = try {
            localDatabaseRepository.getUchiwa(uchiwaId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReportingRepository.recordException(e)
            return emptyMap()
        } ?: return emptyMap()
        val state = PuffyState.of(uchiwa.decorations, uchiwa.isOverallBorderPuffyEnabled)
        return mapOf(PuffyStateParams.PARAM_PUFFY_STATE to state.toParamValue())
    }

    private fun PuffyState.toParamValue(): String = when (this) {
        PuffyState.ON -> PuffyStateParams.PUFFY_STATE_ON
        PuffyState.OFF -> PuffyStateParams.PUFFY_STATE_OFF
        PuffyState.MIXED -> PuffyStateParams.PUFFY_STATE_MIXED
    }
}
