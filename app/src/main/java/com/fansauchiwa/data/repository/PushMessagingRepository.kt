package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.infra.PushMessagingDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

interface PushMessagingRepository {
    /**
     * 全員に送るお知らせのトピック（[ANNOUNCEMENT_TOPIC]）を購読する（#429）。起動のたびに呼ぶ（購読済みでも害はない）。
     * 購読できなくてもアプリの操作には関わらないので、例外は投げずにログだけ残す
     */
    suspend fun subscribeToAnnouncements()

    companion object {
        /**
         * Firebase コンソールからお知らせを送るときに選ぶトピック。名前を変えると、変える前の版を入れた人に届かなくなる
         * （送り方は `.agents/push-notifications.md`）
         */
        const val ANNOUNCEMENT_TOPIC = "all"
    }
}

class PushMessagingRepositoryImpl @Inject constructor(
    private val pushMessagingDataSource: PushMessagingDataSource
) : PushMessagingRepository {

    override suspend fun subscribeToAnnouncements() {
        try {
            pushMessagingDataSource.subscribeToTopic(PushMessagingRepository.ANNOUNCEMENT_TOPIC)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Google Play 開発者サービスがない端末などで失敗する。次の起動でまた試す
            Log.w(TAG, "お知らせのトピックの購読に失敗", e)
        }
    }

    private companion object {
        const val TAG = "PushMessaging"
    }
}
