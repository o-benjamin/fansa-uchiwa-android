package com.fansauchiwa.data.infra

import android.util.Log
import com.google.firebase.installations.FirebaseInstallations
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
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
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "端末の ID の取得に失敗", e)
        null
    }

    private companion object {
        const val TAG = "FirebaseInstallations"
    }
}
