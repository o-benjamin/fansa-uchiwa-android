package com.fansauchiwa.edit.aimaterial

import android.net.Uri

// サーバーの上限と同じ（`functions/src/generateMaterial.ts` の MAX_PROMPT_LENGTH）。絵文字も1文字で数える
const val AI_MATERIAL_MAX_PROMPT_LENGTH = 200

data class AiMaterialUiState(
    val prompt: String = "",
    val sourceImageUri: Uri? = null,
    val generation: AiMaterialGeneration = AiMaterialGeneration.Idle,
    val reportStatus: AiMaterialReportStatus = AiMaterialReportStatus.NOT_SENT
) {
    // サーバーは前後の空白を除いて数えるので、表示の文字数も同じ数え方にする
    val promptLength: Int
        get() = prompt.trim().let { it.codePointCount(0, it.length) }

    val isPromptTooLong: Boolean
        get() = promptLength > AI_MATERIAL_MAX_PROMPT_LENGTH

    val canGenerate: Boolean
        get() = promptLength > 0 &&
            !isPromptTooLong &&
            generation !is AiMaterialGeneration.Generating &&
            generation !is AiMaterialGeneration.Succeeded

    // 送っている間と送ったあとは、同じ素材をもう一度報告しない
    val canReport: Boolean
        get() = generation is AiMaterialGeneration.Succeeded &&
            (reportStatus == AiMaterialReportStatus.NOT_SENT || reportStatus == AiMaterialReportStatus.FAILED)
}
