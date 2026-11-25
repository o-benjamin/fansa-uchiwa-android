package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.NotificationPermissionDataSource
import com.fansauchiwa.data.infra.SettingsDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * 通知の許可（Android 13 以上）を、うちわを保存したあとに1回だけ聞くための判定（#429）。
 * プッシュ通知のお知らせと、今後足す通知（#361 のお題の通知など）で、聞く場面をこの1つにまとめる。
 * イベント予定のリマインド（`EventEditBottomSheet`）は、リマインドを付けたときにその場で聞く（こちらとは別に数えない）
 */
interface NotificationPermissionRepository {
    /**
     * 保存のあとに通知の許可を聞くなら true。まだ1回も聞いておらず、許可を求める必要がある端末（Android 13 以上で未許可）のとき
     */
    suspend fun shouldRequestAfterSave(): Boolean

    /**
     * 聞いたことを残す。許可されたかどうかにかかわらず、以後は保存のあとに聞かない
     */
    suspend fun markRequestedAfterSave()
}

class NotificationPermissionRepositoryImpl @Inject constructor(
    private val notificationPermissionDataSource: NotificationPermissionDataSource,
    private val settingsDataSource: SettingsDataSource
) : NotificationPermissionRepository {

    override suspend fun shouldRequestAfterSave(): Boolean =
        notificationPermissionDataSource.isPermissionRequestNeeded() &&
            !settingsDataSource.getHasRequestedNotificationPermissionStream().first()

    override suspend fun markRequestedAfterSave() {
        settingsDataSource.setHasRequestedNotificationPermission()
    }
}
