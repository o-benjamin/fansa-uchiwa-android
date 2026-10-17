package com.fansauchiwa.data.infra

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * provider はビルドタイプごとに `AppCheckProviderModule` が渡す（製品版は Play Integrity、debug と beta はデバッグ用）
 */
class FirebaseAppCheckRemoteSource @Inject constructor(
    private val firebaseAppCheck: FirebaseAppCheck,
    private val providerFactory: AppCheckProviderFactory
) : AppCheckDataSource {

    private val isInstalled = AtomicBoolean(false)

    override fun installProviderIfNeeded() {
        if (!isInstalled.compareAndSet(false, true)) return
        // トークンの自動更新は切る。オンにすると、AI 素材を使わない間も裏で Play Integrity を呼び続け、
        // 1日の呼び出し回数の上限（標準で1万回）を使ってしまう。トークンは SDK がサーバーを呼ぶときに取る
        firebaseAppCheck.installAppCheckProviderFactory(providerFactory, false)
    }
}
