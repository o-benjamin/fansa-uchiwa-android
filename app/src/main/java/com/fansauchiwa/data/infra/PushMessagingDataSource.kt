package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * プッシュ通知（Firebase Cloud Messaging）の購読と、送り先の登録トークン（#429）
 */
interface PushMessagingDataSource {
    /**
     * トピックを購読する。購読できなかったときは例外を投げる
     */
    suspend fun subscribeToTopic(topic: String)

    /**
     * この端末のアプリに通知を送るための登録トークンの Flow を返す。取れなかったときは null
     */
    fun getTokenStream(): Flow<String?>
}
