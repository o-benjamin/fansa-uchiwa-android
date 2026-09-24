package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.CrashReportingDataSource
import javax.inject.Inject

interface CrashReportingRepository {
    /**
     * 非致命の例外を Crashlytics に記録する
     *
     * @param throwable 記録する例外
     */
    fun recordException(throwable: Throwable)
}

class CrashReportingRepositoryImpl @Inject constructor(
    private val crashReportingDataSource: CrashReportingDataSource
) : CrashReportingRepository {

    override fun recordException(throwable: Throwable) {
        crashReportingDataSource.recordException(throwable)
    }
}
