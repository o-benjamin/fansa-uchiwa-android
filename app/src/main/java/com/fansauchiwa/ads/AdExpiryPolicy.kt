package com.fansauchiwa.ads

/**
 * 読み込んだ全画面広告（リワード・インタースティシャル）が、まだ表示に使えるかを決める
 * AdMob の広告は読み込みから1時間で期限が切れ、それ以降に表示しても収益にならないため、
 * 期限切れの広告は捨てて読み直す
 *
 * 1時間の出典（AdMob 公式。どちらも「Because ads expire after an hour」）
 * - リワード：https://developers.google.com/admob/android/rewarded の「Load a rewarded ad object」
 * - インタースティシャル：https://developers.google.com/admob/android/interstitial の「Load an ad」
 */
object AdExpiryPolicy {
    const val MAX_AGE_MILLIS = 60 * 60 * 1_000L

    /**
     * @param loadedAtMillis 広告を読み込んだ時刻（`SystemClock.elapsedRealtime()` の値）
     * @param nowMillis 今の時刻（`loadedAtMillis` と同じ時計の値）
     * @return 読み込みから1時間以上たっていれば true
     */
    fun isExpired(loadedAtMillis: Long, nowMillis: Long): Boolean =
        nowMillis - loadedAtMillis >= MAX_AGE_MILLIS
}
