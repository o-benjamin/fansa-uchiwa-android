package com.fansauchiwa.data.infra

/**
 * Firebase App Check（#337）。AI 素材のサーバーを、ストアから入れた本物のアプリからだけ呼べるようにする
 */
interface AppCheckDataSource {
    /**
     * App Check の provider を入れる。2回目からは何もしない。
     * 入れたあとは、Firebase の SDK がサーバーを呼ぶたびに App Check のトークンを付ける
     */
    fun installProviderIfNeeded()
}
