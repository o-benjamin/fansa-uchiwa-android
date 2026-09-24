package com.fansauchiwa.data.infra

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo

/**
 * Play の In-App Review API でレビュー依頼を出す
 */
interface InAppReviewDataSource {
    /**
     * レビュー依頼に必要な情報を Play から取得する（通信するため時間がかかることがある）。
     * Play ストアが使えない端末などでは例外を投げる
     */
    suspend fun requestReviewInfo(): ReviewInfo

    /**
     * レビュー依頼の画面を出す。Play 側の表示回数の上限などで実際には表示されなくても、正常に終わる
     */
    suspend fun launchReviewFlow(activity: Activity, reviewInfo: ReviewInfo)
}
