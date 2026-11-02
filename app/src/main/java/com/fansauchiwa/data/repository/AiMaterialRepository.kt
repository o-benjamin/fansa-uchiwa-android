package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.infra.AiMaterialDataSource
import com.fansauchiwa.data.infra.AiMaterialImageDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * AI 素材の生成（#338）
 */
interface AiMaterialRepository {
    /**
     * サーバーに素材を1枚作ってもらい、背景を切り抜いて端末に置く。
     * 元画像に顔があるとき（確かめられないときも）はサーバーに送らない。
     * 理由つきで断ったときの失敗は `AiMaterialRejectedException`
     */
    suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage>
}

class AiMaterialRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageProcessingRepository: ImageProcessingRepository,
    private val aiMaterialDataSource: AiMaterialDataSource,
    private val aiMaterialImageDataSource: AiMaterialImageDataSource
) : AiMaterialRepository {

    override suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage> = try {
        val sourceImage = sourceImageUri?.let { uri ->
            if (imageProcessingRepository.containsFace(uri).getOrThrow()) {
                throw AiMaterialRejectedException(AiMaterialRejectReason.FACE_IN_SOURCE)
            }
            aiMaterialImageDataSource.encodeSourceImage(uri)
        }
        authRepository.prepareForServerCall()
        val material = aiMaterialDataSource.generateMaterial(prompt, sourceImage)
        val generatedUri = aiMaterialImageDataSource.writeGeneratedImage(material)
        // 回数はもう減っているので、切り抜けなくても作った絵は渡す
        val cutOutUri = imageProcessingRepository.removeBackground(generatedUri).getOrNull()
        Result.success(
            AiMaterialImage(
                uri = cutOutUri ?: generatedUri,
                backgroundRemoved = cutOutUri != null,
                payment = material.payment,
                ticketsRemaining = material.ticketsRemaining
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
