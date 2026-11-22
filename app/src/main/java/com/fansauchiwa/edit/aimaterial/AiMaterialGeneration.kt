package com.fansauchiwa.edit.aimaterial

import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialRejectReason

sealed interface AiMaterialGeneration {
    data object Idle : AiMaterialGeneration
    data object Generating : AiMaterialGeneration
    data class Succeeded(val image: AiMaterialImage) : AiMaterialGeneration
    data class Rejected(val reason: AiMaterialRejectReason) : AiMaterialGeneration {
        // 無料の非常ブレーキに当たっても、回数券は使えるので購入の案内（#348）を開く
        val offersPurchase: Boolean
            get() = reason == AiMaterialRejectReason.NO_CREDITS ||
                reason == AiMaterialRejectReason.FREE_DAILY_LIMIT
    }

    // 送る前の失敗。回数は減っていない
    data object Failed : AiMaterialGeneration

    // 送ったあとに失敗した、または作っている途中でプロセスが止められた。回数が減ったか分からない（#387）
    data object Unconfirmed : AiMaterialGeneration
}
