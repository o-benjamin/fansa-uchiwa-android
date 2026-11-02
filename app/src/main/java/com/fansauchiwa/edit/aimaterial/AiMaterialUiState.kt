package com.fansauchiwa.edit.aimaterial

import android.net.Uri
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialRejectReason

// サーバーの上限と同じ（`functions/src/generateMaterial.ts` の MAX_PROMPT_LENGTH）。絵文字も1文字で数える
const val AI_MATERIAL_MAX_PROMPT_LENGTH = 200

data class AiMaterialUiState(
    val prompt: String = "",
    val sourceImageUri: Uri? = null,
    val generation: AiMaterialGeneration = AiMaterialGeneration.Idle
) {
    val canGenerate: Boolean
        get() {
            val trimmed = prompt.trim()
            return trimmed.isNotEmpty() &&
                trimmed.codePointCount(0, trimmed.length) <= AI_MATERIAL_MAX_PROMPT_LENGTH &&
                generation !is AiMaterialGeneration.Generating &&
                generation !is AiMaterialGeneration.Succeeded
        }
}

sealed interface AiMaterialGeneration {
    data object Idle : AiMaterialGeneration
    data object Generating : AiMaterialGeneration
    data class Succeeded(val image: AiMaterialImage) : AiMaterialGeneration
    data class Rejected(val reason: AiMaterialRejectReason) : AiMaterialGeneration {
        // 無料の非常ブレーキに当たっても、回数券は使えるので購入画面（#348）へつなぐ
        val offersPurchase: Boolean
            get() = reason == AiMaterialRejectReason.NO_CREDITS ||
                reason == AiMaterialRejectReason.FREE_DAILY_LIMIT
    }

    data object Failed : AiMaterialGeneration
}
