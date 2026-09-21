package com.fansauchiwa.data.infra

import android.app.Activity
import android.content.Context
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayInAppReviewRemoteSource @Inject constructor(
    @ApplicationContext private val context: Context
) : InAppReviewDataSource {

    private val reviewManager by lazy { ReviewManagerFactory.create(context) }

    override suspend fun requestReviewInfo(): ReviewInfo = reviewManager.requestReview()

    override suspend fun launchReviewFlow(activity: Activity, reviewInfo: ReviewInfo) {
        reviewManager.launchReview(activity, reviewInfo)
    }
}
