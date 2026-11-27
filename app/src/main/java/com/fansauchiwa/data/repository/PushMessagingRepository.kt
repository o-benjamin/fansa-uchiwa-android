package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.infra.PushMessagingDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

interface PushMessagingRepository {
    /**
     * 全員に送るお知らせのトピック（[ANNOUNCEMENT_TOPIC]）を購読する（#429）。起動のたびに呼ぶ（購読済みでも害はない）。
     * 購読できなくてもアプリの操作には関わらないので、例外は投げない。
     * 購読の Task が失敗するのはトピック名の形式の誤り（`IllegalArgumentException`）だけで、全員で失敗するので Crashlytics に残す。
     * 通信・Google Play 開発者サービス・サーバーの断りでは Task は終わらず、SDK が裏でやり直すか logcat に出すだけ
     * （firebase-messaging 25.0.1 の `TopicsSubscriber`・`TopicsSyncTask` で確かめた。BoM を上げたら見直す。調べ方は `.agents/push-notifications.md` の「アプリの作り」）
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
    private val pushMessagingDataSource: PushMessagingDataSource,
    private val crashReportingRepository: CrashReportingRepository
) : PushMessagingRepository {

    override suspend fun subscribeToAnnouncements() {
        try {
            pushMessagingDataSource.subscribeToTopic(PushMessagingRepository.ANNOUNCEMENT_TOPIC)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // ここに来るのは全員にお知らせが届かなくなる失敗なので、Crashlytics で気づけるようにする
            Log.w(TAG, "お知らせのトピックの購読に失敗", e)
            crashReportingRepository.recordException(e)
        }
    }

    private companion object {
        const val TAG = "PushMessaging"
    }
}
