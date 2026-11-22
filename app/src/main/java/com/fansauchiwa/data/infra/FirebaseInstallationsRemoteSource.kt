package com.fansauchiwa.data.infra

import android.util.Log
import com.google.firebase.installations.FirebaseInstallations
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseInstallationsRemoteSource @Inject constructor(
    private val firebaseInstallations: FirebaseInstallations
) : InstallationIdDataSource {

    override fun getInstallationIdStream(): Flow<String?> = flow {
        emit(getInstallationIdOrNull())
    }

    /**
     * 取れないときは設定画面に行を出さないだけなので、ログだけ残して null を返す。
     * ID は画面に出すだけで、ログには出さない（#411）
     */
    private suspend fun getInstallationIdOrNull(): String? = try {
        firebaseInstallations.id.await()
    } catch (e: Exception) {
        // Task が取り消されたときも CancellationException が来るので、ここでまとめて受ける。
        // 呼び出し側のコルーチンが止められたときだけ投げ直す
        currentCoroutineContext().ensureActive()
        Log.w(TAG, "インストール ID の取得に失敗", e)
        null
    }

    private companion object {
        const val TAG = "FirebaseInstallations"
    }
}
