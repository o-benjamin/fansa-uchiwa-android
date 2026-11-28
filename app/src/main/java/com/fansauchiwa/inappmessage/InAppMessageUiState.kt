package com.fansauchiwa.inappmessage

import com.fansauchiwa.data.InAppMessageTarget

data class InAppMessageUiState(
    // メッセージのボタンで押された、ホームから開く行き先（DAILY_PROMPT・AI_MATERIAL）。
    // ホームが受け取ったら（開けなかったときも）、またはホームを離れたら null に戻す
    val pendingTarget: InAppMessageTarget? = null
)
