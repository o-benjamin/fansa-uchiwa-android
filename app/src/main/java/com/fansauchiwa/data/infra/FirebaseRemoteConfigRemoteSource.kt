package com.fansauchiwa.data.infra

import android.util.Log
import com.fansauchiwa.data.UpdateVersionCodes
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

    override fun getDailyPromptsJsonStream(): Flow<String> = flow {
        // 取得を待つと、遅い回線では取得のタイムアウトまでお題が出ないので、端末に残っている値を先に流す
        awaitInitialized()
        emit(remoteConfig.getString(KEY_DAILY_PROMPTS))
        fetchAndActivate()
        emit(remoteConfig.getString(KEY_DAILY_PROMPTS))
    }

    override fun getAffiliateMaterialsJsonStream(): Flow<String> = flow {
        fetchAndActivate()
        emit(remoteConfig.getString(KEY_AFFILIATE_MATERIALS))
    }

    override fun getAiMaterialEnabledStream(): Flow<Boolean> = flow {
        // 入口はお題のカードの副ボタンで、カードは端末に残っている値ですぐ出る。取得を待ってから流すと、
        // 遅い回線では副ボタンだけ後から出てカードの中身がずれるので、お題と同じく端末に残っている値を先に流す
        awaitInitialized()
        emit(remoteConfig.getBoolean(KEY_AI_MATERIAL_ENABLED))
        fetchAndActivate()
        emit(remoteConfig.getBoolean(KEY_AI_MATERIAL_ENABLED))
    }

    override fun getUpdateVersionCodesStream(): Flow<UpdateVersionCodes> = flow {
        // 取得に失敗したとき（オフラインなど）も前回の値で決められるよう、先に端末から読み込んでおく
        awaitInitialized()
        fetchAndActivate()
        emit(
            UpdateVersionCodes(
                minVersionCode = remoteConfig.getLong(KEY_MIN_VERSION_CODE),
                forceVersionCode = remoteConfig.getLong(KEY_FORCE_VERSION_CODE)
            )
        )
    }

    /**
     * 前回までに有効にした値を端末から読み込む。読み込む前の getString は初期値（空文字）を返すため
     */
    private suspend fun awaitInitialized() {
        try {
            remoteConfig.ensureInitialized().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Remote Config の読み込みに失敗", e)
        }
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
        const val KEY_DAILY_PROMPTS = "daily_prompts"
        const val KEY_AFFILIATE_MATERIALS = "affiliate_materials"
        const val KEY_AI_MATERIAL_ENABLED = "ai_material_enabled"
        const val KEY_MIN_VERSION_CODE = "min_version_code"
        const val KEY_FORCE_VERSION_CODE = "force_version_code"
        const val MINIMUM_FETCH_INTERVAL_SECONDS = 60L * 60
        const val FETCH_TIMEOUT_SECONDS = 10L
    }
}
