package com.fansauchiwa.data.infra

import android.app.Activity

/**
 * Play の In-App Review API でレビュー依頼を出す
 */
interface InAppReviewDataSource {
    /**
     * レビュー依頼の画面を出す。Play 側の表示回数の上限などで実際には表示されなくても、正常に終わる。
     * Play ストアが使えない端末などでは例外を投げる
     */
    suspend fun launchReviewFlow(activity: Activity)
}
