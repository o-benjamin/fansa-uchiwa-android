package com.fansauchiwa.data.infra

/**
 * 利用者の識別（Firebase Auth の匿名ログイン。#337）。
 * AI 素材のサーバーは、この ID ごとに無料の回数と回数券の残りを数える
 */
interface AuthDataSource {
    /**
     * ログイン中の利用者の ID。まだログインしていなければ null。
     * 匿名ログインはアプリのデータを消す・入れ直すまで端末に残る
     */
    fun getCurrentUserId(): String?

    /**
     * 匿名でログインし、利用者の ID を返す。
     * 通信できないとき、Firebase で匿名ログインが有効になっていないときは例外を投げる
     */
    suspend fun signInAnonymously(): String
}
