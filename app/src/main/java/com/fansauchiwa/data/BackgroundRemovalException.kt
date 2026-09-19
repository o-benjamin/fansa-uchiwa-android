package com.fansauchiwa.data

/**
 * 背景透過の失敗を、理由とともに伝える例外
 */
class BackgroundRemovalException(
    val reason: BackgroundRemovalFailureReason,
    cause: Throwable? = null
) : Exception(reason.analyticsValue, cause)
