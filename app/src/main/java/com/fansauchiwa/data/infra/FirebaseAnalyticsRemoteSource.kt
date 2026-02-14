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
        val bundle = event.params.toBundle()
        firebaseAnalytics.logEvent(event.name, bundle)
    }

    override suspend fun logScreenView(screenName: String) = withContext(Dispatchers.IO) {
        val bundle = bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        )
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Map<String, Any>をBundleに変換する
     */
    private fun Map<String, Any>.toBundle(): Bundle {
        val pairs = this.map { (key, value) ->
            key to when (value) {
                is String -> value
                is Int -> value
                is Long -> value
                is Double -> value
                is Boolean -> value
                is Float -> value
                else -> value.toString()
            }
        }.toTypedArray()
        return bundleOf(*pairs)
    }
}

