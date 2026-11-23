package com.fansauchiwa.data.repository

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.PlayAppUpdateStatus
import com.fansauchiwa.data.infra.AppUpdateDataSource
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import com.fansauchiwa.update.UpdatePromptPolicy
import com.fansauchiwa.update.UpdatePromptType
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 新しい版への更新の案内（#428）。Play の In-App Updates と Remote Config の更新を促す版の境目から、出す案内を決める
 *
 * Play への問い合わせは、Play ストアがない端末やストア以外から入れたアプリで失敗するのが想定内なので、どのメソッドも例外を外に出さない
 */
interface AppUpdateRepository {
    /**
     * 起動して最初に呼んだときに出す案内を決めて返す。2回目以降は [UpdatePromptType.NONE]。
     * ただし [UpdatePromptType.FORCE] は、同じ起動の中でも呼ぶたびに返す（ホームを作り直しても閉じられないままにするため）
     */
    suspend fun getUpdatePromptOnLaunch(): UpdatePromptType

    /**
     * Play の更新の画面を出す。Play に聞き直してから出す（前に聞いた結果は1回しか使えないため）
     *
     * @param updateType `AppUpdateType.FLEXIBLE` か `AppUpdateType.IMMEDIATE`
     * @return 画面を出せたら true。新しい版がない・その出し方が許されていない・Play に聞けないときは false
     */
    suspend fun startUpdateFlow(launcher: ActivityResultLauncher<IntentSenderRequest>, updateType: Int): Boolean

    /**
     * Play ストアのアプリのページを開く。開けないときは何もしない
     */
    fun openStorePage()

    /**
     * 柔軟な更新のダウンロードが終わり、再起動すれば入る状態かを流す。
     * 購読している間はダウンロードの進み具合を Play から受け取り、[fetchUpdateDownloaded] でも今の状態を流す
     */
    fun getUpdateDownloadedStream(): Flow<Boolean>

    /**
     * 柔軟な更新のダウンロードが終わっているかを Play に聞いて流す（アプリが裏にいる間に終わった分を拾うため）
     */
    suspend fun fetchUpdateDownloaded()

    /**
     * ダウンロードが終わった更新を入れる。アプリは再起動する
     */
    suspend fun completeUpdate()
}

class AppUpdateRepositoryImpl @Inject constructor(
    private val appUpdateDataSource: AppUpdateDataSource,
    private val remoteConfigDataSource: RemoteConfigDataSource
) : AppUpdateRepository {

    private val launchPromptMutex = Mutex()

    // この起動で決めた案内。決める前は null
    private var launchPrompt: UpdatePromptType? = null

    private val _updateDownloadedStream = MutableSharedFlow<Boolean>(replay = 1)

    override suspend fun getUpdatePromptOnLaunch(): UpdatePromptType = launchPromptMutex.withLock {
        val decided = launchPrompt
        if (decided != null) {
            return@withLock if (decided == UpdatePromptType.FORCE) UpdatePromptType.FORCE else UpdatePromptType.NONE
        }
        val prompt = UpdatePromptPolicy.decide(
            currentVersionCode = BuildConfig.VERSION_CODE.toLong(),
            versionCodes = remoteConfigDataSource.getUpdateVersionCodesStream().first(),
            playUpdate = runCatchingPlay { appUpdateDataSource.requestAppUpdateInfo() }?.toPlayAppUpdateStatus()
        )
        launchPrompt = prompt
        prompt
    }

    override suspend fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: Int
    ): Boolean = runCatchingPlay {
        val info = appUpdateDataSource.requestAppUpdateInfo()
        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            info.isUpdateTypeAllowed(updateType) &&
            appUpdateDataSource.startUpdateFlow(info, launcher, updateType)
    } ?: false

    override fun openStorePage() {
        try {
            appUpdateDataSource.openStorePage()
        } catch (e: Exception) {
            Log.w(TAG, "Play ストアのページを開けなかった", e)
        }
    }

    override fun getUpdateDownloadedStream(): Flow<Boolean> = merge(
        _updateDownloadedStream,
        appUpdateDataSource.getInstallStatusStream()
            .map { it == InstallStatus.DOWNLOADED }
            .catch { e -> Log.w(TAG, "ダウンロードの状態を受け取れなかった", e) }
    ).distinctUntilChanged()

    override suspend fun fetchUpdateDownloaded() {
        val info = runCatchingPlay { appUpdateDataSource.requestAppUpdateInfo() } ?: return
        _updateDownloadedStream.emit(info.installStatus() == InstallStatus.DOWNLOADED)
    }

    override suspend fun completeUpdate() {
        runCatchingPlay { appUpdateDataSource.completeUpdate() }
    }

    private fun AppUpdateInfo.toPlayAppUpdateStatus(): PlayAppUpdateStatus? =
        if (updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            PlayAppUpdateStatus(
                isFlexibleAllowed = isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
                stalenessDays = clientVersionStalenessDays()
            )
        } else {
            null
        }

    /**
     * Play への問い合わせ。失敗するのは想定内なので、ログだけ残して null を返す
     */
    private suspend fun <T> runCatchingPlay(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Play への更新の問い合わせに失敗", e)
        null
    }

    private companion object {
        const val TAG = "AppUpdate"
    }
}
