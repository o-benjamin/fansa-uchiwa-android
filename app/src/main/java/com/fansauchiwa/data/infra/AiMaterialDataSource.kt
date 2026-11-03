package com.fansauchiwa.data.infra

import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial

/**
 * AI 素材のサーバー（Cloud Functions の `generateMaterial`（#338）と `reportMaterial`（#339））
 */
interface AiMaterialDataSource {
    /**
     * 素材を1枚作る。
     * サーバーが理由つきで断ったときは `AiMaterialRejectedException`、通信できないときなどはそのほかの例外を投げる
     */
    suspend fun generateMaterial(
        prompt: String,
        sourceImage: AiMaterialSourceImage?
    ): GeneratedAiMaterial

    /**
     * 作った素材を報告する。頼んだ文章・理由・モデル名を送る（画像は送らない）。
     * 送れなかったときは例外を投げる
     */
    suspend fun reportMaterial(prompt: String, reason: AiMaterialReportReason, model: String)
}
