package com.fansauchiwa

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fansauchiwa.ui.notification.UchiwaReminderScheduler
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FansaUchiwaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        // super.onCreate() で Hilt が workerFactory を注入するため、WorkManager を使う処理はこの後に呼ぶ
        super.onCreate()
        UchiwaReminderScheduler.schedule(this)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@FansaUchiwaApplication) { initializationStatus ->
                Log.d("AdMob", "Initialized: $initializationStatus")
            }
        }
    }
}
