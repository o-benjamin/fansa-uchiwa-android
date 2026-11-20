package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.AiMaterialFaceCheckException
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.infra.AiMaterialDataSource
import com.fansauchiwa.data.infra.AiMaterialImageDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * AI 素材の生成（#338）
 */
interface AiMaterialRepository {
    /**
     * サーバーに素材を1枚作ってもらい、背景を切り抜いて端末に置く。
     * 元画像に顔があるとき（確かめられないときも）はサーバーに送らない。
     * 理由つきで断ったときの失敗は `AiMaterialRejectedException`、顔を確かめられなかったときは
     * `AiMaterialFaceCheckException`
     */
    suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage>
}

class AiMaterialRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageProcessingRepository: ImageProcessingRepository,
    private val aiMaterialDataSource: AiMaterialDataSource,
    private val aiMaterialImageDataSource: AiMaterialImageDataSource,
    private val crashReportingRepository: CrashReportingRepository
) : AiMaterialRepository {

    override suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage> = try {
        val sourceImage = sourceImageUri?.let { uri ->
            val hasFace = imageProcessingRepository.containsFace(uri)
                .getOrElse { throw AiMaterialFaceCheckException(it) }
            if (hasFace) {
                throw AiMaterialRejectedException(AiMaterialRejectReason.FACE_IN_SOURCE)
            }
            aiMaterialImageDataSource.encodeSourceImage(uri)
        }
        authRepository.prepareForServerCall()
        val material = aiMaterialDataSource.generateMaterial(prompt, sourceImage)
        val generatedUri = aiMaterialImageDataSource.writeGeneratedImage(material)
        // 回数はもう減っているので、切り抜けなくても作った絵を渡す。
        // 「背景が残る」と言われたときに原因を追えるよう、失敗は非致命として残す
        val cutOutUri = imageProcessingRepository.removeBackground(generatedUri)
            .onFailure { crashReportingRepository.recordException(it) }
            .getOrNull()
        Result.success(
            AiMaterialImage(
                uri = cutOutUri ?: generatedUri,
                backgroundRemoved = cutOutUri != null,
                payment = material.payment,
                ticketsRemaining = material.ticketsRemaining
            )
        )
    } catch (e: CancellationException) {
        // Play 開発者サービス（ML Kit）や Firebase の Task が取り消されたときも await() は CancellationException を投げる。
        // 呼び出し元のコルーチンが取り消されたときだけそのまま投げ、それ以外は失敗として画面に出す
        // （投げたままにすると、画面が「作っています」のまま戻れなくなる）
        currentCoroutineContext().ensureActive()
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
