package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * Firebase In-App Messaging のアプリ内メッセージ（#428）。文面・相手・期間は Firebase コンソールのキャンペーンで決め、アプリには持たない
 */
interface InAppMessagingDataSource {
    /**
     * メッセージが表示されるたびに流す。購読している間だけ受け取る
     */
    fun getImpressionStream(): Flow<Unit>

    /**
     * メッセージのボタンが押されるたびに、そのボタンの URL を流す（URL のないボタンは閉じるだけで、ここには来ない）。
     * URL を開く動きは Firebase In-App Messaging が別に行う。購読している間だけ受け取る
     */
    fun getClickedActionUrlStream(): Flow<String>

    /**
     * true の間はメッセージを出さない。止めている間に来たきっかけ（起動など）のメッセージは、次のきっかけまで出ない
     */
    fun setMessagesSuppressed(suppressed: Boolean)
}
