package com.fansauchiwa.data.infra

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import kotlinx.coroutines.flow.Flow

/**
 * Play の In-App Updates API で、新しい版への更新を案内する（#428）
 */
interface AppUpdateDataSource {
    /**
     * 今の版より新しい版があるかを Play に聞く。
     * Play ストアが使えない端末や、ストア以外から入れたアプリでは例外を投げる
     */
    suspend fun requestAppUpdateInfo(): AppUpdateInfo

    /**
     * 更新の画面を出す。[appUpdateInfo] は1回しか使えないので、出すたびに [requestAppUpdateInfo] で取り直すこと
     *
     * @param updateType `AppUpdateType.FLEXIBLE` か `AppUpdateType.IMMEDIATE`
     * @return 画面を出せたら true
     */
    fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: Int
    ): Boolean

    /**
     * 柔軟な更新のダウンロードの状態（`InstallStatus` の値）を流す。購読している間だけ Play から受け取る
     */
    fun getInstallStatusStream(): Flow<Int>

    /**
     * ダウンロードが終わった更新を入れる。アプリは再起動する
     */
    suspend fun completeUpdate()

    /**
     * Play ストアのアプリのページを開く。In-App Updates の画面を出せないときに使う
     */
    fun openStorePage()
}
