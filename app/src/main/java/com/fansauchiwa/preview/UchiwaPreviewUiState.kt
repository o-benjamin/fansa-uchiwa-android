package com.fansauchiwa.preview

import android.os.Parcelable
import com.fansauchiwa.data.AffiliateLink
import kotlinx.parcelize.Parcelize

@Parcelize
data class UchiwaPreviewUiState(
    val imagePath: String? = null,
    val saveSuccess: Boolean? = null,
    val isLoadingAd: Boolean = false,
    val isSaveButtonPressed: Boolean = false,
    val shareImagePath: String? = null,
    val affiliateLinks: List<AffiliateLink> = emptyList(),
    // 今日のお題から作り始めたうちわか（#360）。共有文に #今日のファンサお題 を足し、保存・共有の計測に source=daily_prompt を付ける
    val isStartedFromDailyPrompt: Boolean = false,
    // 保存のあとに通知の許可（#429）を聞く。Screen が許可のダイアログを出す直前に false に戻す
    val shouldRequestNotificationPermission: Boolean = false
) : Parcelable

