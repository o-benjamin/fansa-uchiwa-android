package com.fansauchiwa.data.infra

interface CrashReportingDataSource {
    /**
     * 非致命の例外を記録する
     *
     * @param throwable 記録する例外
     */
    fun recordException(throwable: Throwable)
}
