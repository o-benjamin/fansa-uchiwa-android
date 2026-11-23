package com.fansauchiwa.data.infra

import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial

/**
 * AI 素材のサーバー（Cloud Functions の `generateMaterial`（#338）と `reportMaterial`（#339））
 */
interface AiMaterialDataSource {
    /**
     * 素材を1枚作る。[requestId] は頼みの番号で、同じ番号の前の頼みで回数を使っていれば、サーバーは回数を使わずに作り直す（#387）。
     * サーバーが理由つきで断ったときは `AiMaterialRejectedException`、通信できないときなどはそのほかの例外を投げる
     */
    suspend fun generateMaterial(
        prompt: String,
        sourceImage: AiMaterialSourceImage?,
        requestId: String
    ): GeneratedAiMaterial

    /**
     * 作った素材を報告する。頼んだ文章・理由・モデル名を送る（画像は送らない）。
     * 1人1日の上限に当たったときは `AiMaterialReportLimitException`、送れなかったときはそのほかの例外を投げる
     */
    suspend fun reportMaterial(prompt: String, reason: AiMaterialReportReason, model: String)
}
