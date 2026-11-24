package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.InAppMessageTarget
import com.fansauchiwa.data.infra.InAppMessagingDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）。運用は `.agents/in-app-messaging.md`
 */
interface InAppMessagingRepository {
    /**
     * メッセージが表示されるたびに流す
     */
    fun getImpressionStream(): Flow<Unit>

    /**
     * メッセージのボタンが押されるたびに、そのボタンの行き先を流す
     */
    fun getClickedTargetStream(): Flow<InAppMessageTarget>

    /**
     * true の間はメッセージを出さない。ホームが今の画面のときだけ出すために、今の画面が変わるたびに呼ぶ
     */
    fun setMessagesSuppressed(suppressed: Boolean)
}

class InAppMessagingRepositoryImpl @Inject constructor(
    private val inAppMessagingDataSource: InAppMessagingDataSource
) : InAppMessagingRepository {

    override fun getImpressionStream(): Flow<Unit> = inAppMessagingDataSource.getImpressionStream()
        .catch { e -> Log.w(TAG, "メッセージの表示を受け取れなかった", e) }

    override fun getClickedTargetStream(): Flow<InAppMessageTarget> =
        inAppMessagingDataSource.getClickedActionUrlStream()
            .map(InAppMessageTarget::fromActionUrl)
            .catch { e -> Log.w(TAG, "メッセージのボタンを受け取れなかった", e) }

    override fun setMessagesSuppressed(suppressed: Boolean) {
        try {
            inAppMessagingDataSource.setMessagesSuppressed(suppressed)
        } catch (e: Exception) {
            // 止められなくても、ホームの表示は続ける。メッセージが編集画面に出ることがあるだけ
            Log.w(TAG, "メッセージの表示を切り替えられなかった", e)
        }
    }

    private companion object {
        const val TAG = "InAppMessaging"
    }
}
