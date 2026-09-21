package com.fansauchiwa.data.infra

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class FirebaseCrashlyticsRemoteSource @Inject constructor(
    private val firebaseCrashlytics: FirebaseCrashlytics
) : CrashReportingDataSource {

    override fun recordException(throwable: Throwable) {
        firebaseCrashlytics.recordException(throwable)
    }
}
