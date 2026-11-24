package com.fansauchiwa.inappmessage

import com.fansauchiwa.data.InAppMessageTarget

data class InAppMessageUiState(
    // メッセージのボタンで押された、ホームから開く画面（DAILY_PROMPT・AI_MATERIAL）。開いたら null に戻す
    val pendingDestination: InAppMessageTarget? = null
)
