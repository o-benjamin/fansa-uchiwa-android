package com.fansauchiwa

import android.app.Application
import android.util.Log
import com.fansauchiwa.ui.notification.UchiwaReminderScheduler
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class FansaUchiwaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UchiwaReminderScheduler.schedule(this)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@FansaUchiwaApplication) { initializationStatus ->
                Log.d("AdMob", "Initialized: $initializationStatus")
            }
        }
    }
}
