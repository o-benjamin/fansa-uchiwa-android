package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.AiMaterialFaceCheckException
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.AiMaterialUnconfirmedException
import com.fansauchiwa.data.infra.AiMaterialDataSource
import com.fansauchiwa.data.infra.AiMaterialImageDataSource
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * AI 素材の生成（#338）と報告・入口スイッチ（#339）
 */
interface AiMaterialRepository {
    /**
     * サーバーに素材を1枚作ってもらい、背景を切り抜いて端末に置く。
     * 元画像に顔があるとき（確かめられないときも）はサーバーに送らない。
     * 理由つきで断ったときの失敗は `AiMaterialRejectedException`、顔を確かめられなかったときは
     * `AiMaterialFaceCheckException`。送ったあとの理由のない失敗（通信の切断・時間切れ・応答の読み取りや
     * 端末への書き込みの失敗）は、回数が減ったか分からないので `AiMaterialUnconfirmedException` で包む。
     * それ以外の失敗は送る前のもので、回数は減っていない
     */
    suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage>

    /**
     * 作った素材のファイルが端末に残っているか。キャッシュに置くので、端末の空きが少ないと OS に消されることがある
     */
    suspend fun isOnDevice(image: AiMaterialImage): Boolean

    /**
     * 作った素材を報告する。頼んだ文章・理由・モデル名をサーバーに送る（画像は送らない）。
     * 1人1日の上限に当たったときの失敗は `AiMaterialReportLimitException`
     */
    suspend fun report(prompt: String, reason: AiMaterialReportReason, model: String): Result<Unit>

    /**
     * 入口を出してよいか（Remote Config の `ai_material_enabled`。既定は false）。端末に残っている値を先に流し、取得を試みてからもう一度流す。
     * 止めたあとも端末には最大約1時間前の値が残るので、ここが true でもサーバーが断ることがある（`.agents/remote-config.md`）
     */
    fun getEntryEnabledStream(): Flow<Boolean>
}

class AiMaterialRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageProcessingRepository: ImageProcessingRepository,
    private val aiMaterialDataSource: AiMaterialDataSource,
    private val aiMaterialImageDataSource: AiMaterialImageDataSource,
    private val crashReportingRepository: CrashReportingRepository,
    private val remoteConfigDataSource: RemoteConfigDataSource
) : AiMaterialRepository {

    override suspend fun generate(prompt: String, sourceImageUri: Uri?): Result<AiMaterialImage> {
        val sourceImage = runServerCall { prepareRequest(sourceImageUri) }
            .getOrElse { return Result.failure(it) }
        // ここから先の失敗は、サーバーに届いて回数が減ったあとかもしれない（届いたかはアプリには分からない）。
        // runServerCall の外で包むのは、呼び出し元が取り消されたときの CancellationException を投げ直してもらうため
        val result = runServerCall { requestMaterial(prompt, sourceImage) }
        val error = result.exceptionOrNull()
        return if (error != null && error !is AiMaterialRejectedException) {
            Result.failure(AiMaterialUnconfirmedException(error))
        } else {
            result
        }
    }

    override suspend fun isOnDevice(image: AiMaterialImage): Boolean =
        aiMaterialImageDataSource.exists(image.uri)

    // 送る前の準備。ここでの失敗は、サーバーに届いていないので回数は減っていない
    private suspend fun prepareRequest(sourceImageUri: Uri?): AiMaterialSourceImage? {
        val sourceImage = sourceImageUri?.let { uri ->
            val hasFace = imageProcessingRepository.containsFace(uri)
                .getOrElse { throw AiMaterialFaceCheckException(it) }
            if (hasFace) {
                throw AiMaterialRejectedException(AiMaterialRejectReason.FACE_IN_SOURCE)
            }
            aiMaterialImageDataSource.encodeSourceImage(uri)
        }
        authRepository.prepareForServerCall()
        return sourceImage
    }

    private suspend fun requestMaterial(prompt: String, sourceImage: AiMaterialSourceImage?): AiMaterialImage {
        val material = aiMaterialDataSource.generateMaterial(prompt, sourceImage)
        val generatedUri = aiMaterialImageDataSource.writeGeneratedImage(material)
        // 回数はもう減っているので、切り抜けなくても作った絵を渡す。
        // 「背景が残る」と言われたときに原因を追えるよう、失敗は非致命として残す
        val cutOutUri = imageProcessingRepository.removeBackground(generatedUri)
            .onFailure { crashReportingRepository.recordException(it) }
            .getOrNull()
        return AiMaterialImage(
            uri = cutOutUri ?: generatedUri,
            backgroundRemoved = cutOutUri != null,
            payment = material.payment,
            ticketsRemaining = material.ticketsRemaining,
            model = material.model
        )
    }

    override suspend fun report(
        prompt: String,
        reason: AiMaterialReportReason,
        model: String
    ): Result<Unit> = runServerCall {
        authRepository.prepareForServerCall()
        aiMaterialDataSource.reportMaterial(prompt, reason, model)
    }

    override fun getEntryEnabledStream(): Flow<Boolean> =
        remoteConfigDataSource.getAiMaterialEnabledStream()
}
