package com.fansauchiwa.data.infra

import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial

/**
 * AI 素材のサーバー（Cloud Functions の `generateMaterial`。#338）
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
}
