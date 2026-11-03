package com.fansauchiwa.data

/**
 * サーバーに送る元画像（#338）。[base64Data] は 4MB まで（`functions/src/generateMaterial.ts`）
 */
data class AiMaterialSourceImage(
    val mimeType: String,
    val base64Data: String
)
