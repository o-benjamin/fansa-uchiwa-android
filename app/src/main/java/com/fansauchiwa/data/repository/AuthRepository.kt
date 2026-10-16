package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.AppCheckDataSource
import com.fansauchiwa.data.infra.AuthDataSource
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * AI 素材のサーバー（Cloud Functions）を呼ぶための利用者の識別と App Check（#337）
 */
interface AuthRepository {
    /**
     * サーバーを呼ぶ直前に必ず呼ぶ。App Check を有効にし、まだなら匿名でログインして、利用者の ID を返す。
     *
     * アプリの起動時には呼ばない。AI 素材を使わない人のために Play Integrity を呼んだり、
     * 利用者を作ったりしないため（Firebase の設定前に出しても、今の動きが変わらない）。
     * 通信できないとき、Firebase で匿名ログインが有効になっていないときは例外を投げる
     */
    suspend fun ensureSignedIn(): String
}

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val appCheckDataSource: AppCheckDataSource
) : AuthRepository {

    // 同時に呼ばれても匿名の利用者を2人作らない（2人目で ID が変わり、無料の回数が増えてしまう）
    private val signInMutex = Mutex()

    override suspend fun ensureSignedIn(): String {
        appCheckDataSource.installProviderIfNeeded()
        return signInMutex.withLock {
            authDataSource.getCurrentUserId() ?: authDataSource.signInAnonymously()
        }
    }
}
