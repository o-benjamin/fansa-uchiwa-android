package com.fansauchiwa.preview

import com.fansauchiwa.R

internal enum class RewardConsentAction(
    val titleResId: Int,
    val messageResId: Int,
    val confirmResId: Int
) {
    Save(
        titleResId = R.string.rewarded_save_dialog_title,
        messageResId = R.string.rewarded_save_dialog_message,
        confirmResId = R.string.rewarded_save_dialog_confirm
    ),
    Share(
        titleResId = R.string.rewarded_share_dialog_title,
        messageResId = R.string.rewarded_share_dialog_message,
        confirmResId = R.string.rewarded_share_dialog_confirm
    )
}
