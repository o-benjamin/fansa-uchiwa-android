package com.fansauchiwa.data.infra

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseMessagingRemoteSource @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) : PushMessagingDataSource {

    override suspend fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic).await()
    }

    override fun getTokenStream(): Flow<String?> = flow {
        emit(getTokenOrNull())
    }

    /**
     * 取れないときは設定画面に行を出さないだけなので、ログだけ残して null を返す。
     * トークンは画面に出すだけで、ログには出さない（[FirebaseInstallationsRemoteSource] と同じ）
     */
    private suspend fun getTokenOrNull(): String? = try {
        firebaseMessaging.token.await()
    } catch (e: Exception) {
        // Task が取り消されたときも CancellationException が来るので、ここでまとめて受ける。
        // 呼び出し側のコルーチンが止められたときだけ投げ直す
        currentCoroutineContext().ensureActive()
        Log.w(TAG, "通知の登録トークンの取得に失敗", e)
        null
    }

    private companion object {
        const val TAG = "FirebaseMessaging"
    }
}
