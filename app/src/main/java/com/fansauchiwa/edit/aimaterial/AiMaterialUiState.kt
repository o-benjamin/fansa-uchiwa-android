package com.fansauchiwa.edit.aimaterial

import android.net.Uri
import com.fansauchiwa.data.GrantedTickets

// サーバーの上限と同じ（`functions/src/generateMaterial.ts` の MAX_PROMPT_LENGTH）。絵文字も1文字で数える
const val AI_MATERIAL_MAX_PROMPT_LENGTH = 200

// 回数券1つの回数と使える期間。サーバーと同じ（`functions/src/verifyPurchase.ts` の TICKETS_PER_PURCHASE・TICKET_VALID_MONTHS）。
// 値段は公開後に Play の価格実験で変えるので書かず、Play から取る
const val AI_MATERIAL_TICKETS_PER_PURCHASE = 5
const val AI_MATERIAL_TICKET_VALID_MONTHS = 6

data class AiMaterialUiState(
    val prompt: String = "",
    val sourceImageUri: Uri? = null,
    val generation: AiMaterialGeneration = AiMaterialGeneration.Idle,
    val reportStatus: AiMaterialReportStatus = AiMaterialReportStatus.NOT_SENT,
    val purchase: AiMaterialPurchase = AiMaterialPurchase.Closed,
    // 残りを取るだけの API はないので、生成と購入の応答で分かった値を持つ。まだ分からないときは 0
    val ticketsRemaining: Int = 0,
    // 回数券を足したことを知らせる。次に「作る」を押すまで出す。
    // 「作る」を押してから足したかの判定にも使う（AiMaterialViewModel の openPurchasePanel・AiMaterialGeneration.ticketsRemaining）。消えるきっかけを変えるときはそちらも直す
    val addedTickets: GrantedTickets? = null
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
