package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * アプリ内レビュー依頼（#243）の条件判定に使う記録（保存成功の回数・前回依頼を試みた日時）を読み書きする
 */
interface InAppReviewHistoryDataSource {
    /**
     * 保存成功の通算回数のFlowを返す。一度も保存していない場合は 0
     */
    fun getSaveSuccessCountStream(): Flow<Int>

    /**
     * 保存成功の通算回数を1増やす
     */
    suspend fun incrementSaveSuccessCount()

    /**
     * 前回レビュー依頼を試みた日時（ミリ秒）のFlowを返す。一度も試みていない場合は null
     */
    fun getLastRequestedAtMillisStream(): Flow<Long?>

    /**
     * レビュー依頼を試みた日時（ミリ秒）を保存する
     */
    suspend fun setLastRequestedAtMillis(millis: Long)
}
