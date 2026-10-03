package com.fansauchiwa.analytics

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

/**
 * ギャラリーへ保存できたうちわで使われているフォントを、export_uchiwa_font のイベントにする（#288）。
 *
 * Preview画面へ進む前にうちわはDBに保存されているので、分析のために状態を持ち回らず、DBから読む。
 * パラメータの意味は [ExportedFontParams] を参照。
 */
class ExportedFontAnalytics @Inject constructor(
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val crashReportingRepository: CrashReportingRepository
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return うちわのテキストが使っているフォントごとに1つずつのイベント（同じフォントは1つにまとめる）。
     *   テキストが無いとき・うちわが見つからないとき・読み込みに失敗したときは空
     *   （失敗しても保存を止めないよう、例外は記録して投げない）
     */
    suspend fun fontEvents(uchiwaId: String?): List<AnalyticsEvent> {
        if (uchiwaId == null) return emptyList()
        val uchiwa = try {
            localDatabaseRepository.getUchiwa(uchiwaId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReportingRepository.recordException(e)
            return emptyList()
        } ?: return emptyList()
        return uchiwa.decorations
            .filterIsInstance<Decoration.Text>()
            .map { it.font }
            .distinct()
            .map { font ->
                AnalyticsEvent(
                    AnalyticsActions.EXPORT_UCHIWA_FONT,
                    mapOf(ExportedFontParams.PARAM_FONT_FAMILY to font.name)
                )
            }
    }
}
