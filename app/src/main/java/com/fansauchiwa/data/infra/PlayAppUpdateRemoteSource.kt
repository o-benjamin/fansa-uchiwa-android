package com.fansauchiwa.data.infra

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.net.toUri
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestCompleteUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PlayAppUpdateRemoteSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AppUpdateDataSource {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }

    override suspend fun requestAppUpdateInfo(): AppUpdateInfo = appUpdateManager.requestAppUpdateInfo()

    override fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: Int
    ): Boolean = appUpdateManager.startUpdateFlowForResult(
        appUpdateInfo,
        launcher,
        AppUpdateOptions.newBuilder(updateType).build()
    )

    override fun getInstallStatusStream(): Flow<Int> = callbackFlow {
        val listener = InstallStateUpdatedListener { state -> trySend(state.installStatus()) }
        appUpdateManager.registerListener(listener)
        awaitClose { appUpdateManager.unregisterListener(listener) }
    }

    override suspend fun completeUpdate() {
        appUpdateManager.requestCompleteUpdate()
    }

    override fun openStorePage() {
        val intent = Intent(Intent.ACTION_VIEW, "$STORE_PAGE_URL${context.packageName}".toUri())
            .setPackage(PLAY_STORE_PACKAGE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Play ストアのアプリがない端末では、ブラウザでストアのページを開く
            context.startActivity(intent.setPackage(null))
        }
    }

    private companion object {
        const val STORE_PAGE_URL = "https://play.google.com/store/apps/details?id="
        const val PLAY_STORE_PACKAGE = "com.android.vending"
    }
}
