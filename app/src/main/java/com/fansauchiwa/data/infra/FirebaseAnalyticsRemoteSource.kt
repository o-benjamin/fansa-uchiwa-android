package com.fansauchiwa.data.infra

import android.os.Bundle
import androidx.core.os.bundleOf
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseAnalyticsRemoteSource @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsDataSource {

    override suspend fun logEvent(event: AnalyticsEvent) = withContext(Dispatchers.IO) {
        firebaseAnalytics.logEvent(event.name, event.params.toBundle())
    }

    override suspend fun logScreenView(screenName: String) = withContext(Dispatchers.IO) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName)
        )
    }

    companion object {
        private fun Map<String, Any>.toBundle(): Bundle =
            bundleOf(*map { (key, value) ->
                key to when (value) {
                    is String -> value
                    is Int -> value
                    is Long -> value
                    is Double -> value
                    is Boolean -> value
                    is Float -> value
                    else -> value.toString()
                }
            }.toTypedArray())
    }
}

