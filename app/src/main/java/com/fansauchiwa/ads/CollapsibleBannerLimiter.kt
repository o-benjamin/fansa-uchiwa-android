package com.fansauchiwa.ads

/**
 * 折りたたみ式バナーのリクエストを、画面（placement）ごとに1回までにする
 * 折りたたみ式は最初に大きく広がって画面を覆うので、画面を開くたびに広がると操作の邪魔になる
 * 2回目からは通常のバナーとしてリクエストする
 * 同じ AdView の自動更新は、AdMob 側が折りたたみ式でリクエストしないので、ここでは扱わない
 * （https://developers.google.com/admob/android/banner/collapsible ）
 */
class CollapsibleBannerLimiter {
    private val requestedPlacements = mutableSetOf<String>()

    /**
     * @return この placement で初めて呼ばれたら true（折りたたみ式でリクエストしてよい）。2回目からは false
     */
    fun tryAcquire(placement: String): Boolean = requestedPlacements.add(placement)
}
