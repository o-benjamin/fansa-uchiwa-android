package com.fansauchiwa.data.infra

/**
 * 通知の許可（Android 13 以上の `POST_NOTIFICATIONS`）の状態
 */
interface NotificationPermissionDataSource {
    /**
     * 通知を出すのに許可を求める必要があれば true。Android 12 以下は許可なしで出せるので false
     */
    fun isPermissionRequestNeeded(): Boolean
}
