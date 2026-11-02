package com.fansauchiwa.data.infra

import android.net.Uri
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial

/**
 * AI 素材の画像と端末のファイルの受け渡し（#338）
 */
interface AiMaterialImageDataSource {
    /**
     * 元画像を、サーバーの上限に収まる大きさの base64 にする
     * @throws Exception 読み込めなかったとき
     */
    suspend fun encodeSourceImage(sourceUri: Uri): AiMaterialSourceImage

    /**
     * サーバーが作った画像をキャッシュのファイルにする
     * @return ファイルのURI
     * @throws Exception 書き込めなかったとき
     */
    suspend fun writeGeneratedImage(material: GeneratedAiMaterial): Uri
}
