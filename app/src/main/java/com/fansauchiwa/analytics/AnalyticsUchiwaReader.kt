package com.fansauchiwa.analytics

import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

/**
 * 分析のために、保存済みのうちわをDBから読む。
 *
 * Preview画面へ進む前にうちわはDBに保存されているので、分析のために状態を持ち回らず、ここで読み直す。
 * 分析の処理で本体の操作を止めたりアプリを落としたりしないよう、読み込みの例外は記録して null を返す。
 */
class AnalyticsUchiwaReader @Inject constructor(
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val crashReportingRepository: CrashReportingRepository
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return 保存済みのうちわ。IDが無いとき・うちわが見つからないとき・読み込みに失敗したときは null
     */
    suspend fun readOrNull(uchiwaId: String?): Uchiwa? {
        if (uchiwaId == null) return null
        return try {
            localDatabaseRepository.getUchiwa(uchiwaId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReportingRepository.recordException(e)
            null
        }
    }
}
