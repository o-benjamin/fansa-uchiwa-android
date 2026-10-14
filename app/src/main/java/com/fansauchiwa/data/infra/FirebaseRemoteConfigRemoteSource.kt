package com.fansauchiwa.data.infra

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseRemoteConfigRemoteSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigDataSource {

    // 取得の前に1度だけ設定する。既定の12時間だと、値を空にしてリンクを止めても半日残るため1時間にする
    private val settingsTask by lazy {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
                .setFetchTimeoutInSeconds(FETCH_TIMEOUT_SECONDS)
                .build()
        )
    }

    override fun getAffiliateMaterialsJsonStream(): Flow<String> = flow {
        fetchAndActivate()
        emit(remoteConfig.getString(KEY_AFFILIATE_MATERIALS))
    }

    /**
     * オフライン・取得回数の制限などで失敗するのは想定内なので、ログだけ残して前回の値を使う
     */
    private suspend fun fetchAndActivate() {
        try {
            settingsTask.await()
            remoteConfig.fetchAndActivate().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Remote Config の取得に失敗", e)
        }
    }

    private companion object {
        const val TAG = "RemoteConfig"
        const val KEY_AFFILIATE_MATERIALS = "affiliate_materials"
        const val MINIMUM_FETCH_INTERVAL_SECONDS = 60L * 60
        const val FETCH_TIMEOUT_SECONDS = 10L
    }
}
