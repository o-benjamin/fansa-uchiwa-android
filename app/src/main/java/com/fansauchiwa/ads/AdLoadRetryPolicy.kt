package com.fansauchiwa.ads

/**
 * 広告のロード失敗時に再試行するまでの待ち時間を決める（指数バックオフ）
 * 失敗が続く状況（在庫切れ・オフラインなど）でリクエストを連発しないようにする
 */
object AdLoadRetryPolicy {
    const val INITIAL_DELAY_MILLIS = 2_000L
    const val MAX_ATTEMPTS = 5

    /**
     * @param attempt 何回目の再試行か（0始まり）
     * @return 再試行までの待ち時間。上限回数に達した場合は null（再試行しない）
     */
    fun delayMillisFor(attempt: Int): Long? {
        if (attempt !in 0 until MAX_ATTEMPTS) return null
        return INITIAL_DELAY_MILLIS shl attempt
    }
}
