package com.fansauchiwa.ads

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 表示を求められたときにロード中だった広告を、ロードが終わるまで待つ
 * 上限の時間がたったら待つのをやめる（ロードそのものは続く）
 */
object AdLoadWait {
    const val MAX_WAIT_MILLIS = 5_000L

    /**
     * [isLoading] が false になる（ロードが成功・失敗のどちらかで終わる）か、[maxWaitMillis] たつまで待つ
     * @return 上限までにロードが終わったら true、時間切れなら false
     */
    suspend fun awaitLoadFinished(
        isLoading: StateFlow<Boolean>,
        maxWaitMillis: Long = MAX_WAIT_MILLIS
    ): Boolean {
        val finished = withTimeoutOrNull(maxWaitMillis) {
            isLoading.first { loading -> !loading }
        }
        return finished != null
    }
}
