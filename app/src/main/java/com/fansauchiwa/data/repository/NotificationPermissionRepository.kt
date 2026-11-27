package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.NotificationPermissionDataSource
import com.fansauchiwa.data.infra.SettingsDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * 通知の許可（Android 13 以上）を、うちわを保存したあとに1回だけ聞くための判定（#429）。
 * アプリのほうから自動で聞くのは、この保存のあとの1か所だけにする（お知らせのプッシュ通知はここで許可をもらう）。
 * 利用者が自分で通知をオンにする操作（イベント予定のリマインドを付ける `EventEditBottomSheet`、#361 のお題の通知のスイッチなど）では、その場で聞く。
 * そこで聞いても、ここの「1回」には数えない（リマインドで許可しなかった人には、保存のあとにもう一度聞く。許可した人には聞かない）
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
        settingsDataSource.markNotificationPermissionRequested()
    }
}
