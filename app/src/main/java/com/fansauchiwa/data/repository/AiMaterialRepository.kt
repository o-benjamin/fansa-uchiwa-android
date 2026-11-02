package com.fansauchiwa.data.repository

import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial
import com.fansauchiwa.data.infra.AiMaterialDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * AI 素材の生成（#338）
 */
interface AiMaterialRepository {
    /**
     * サーバーに素材を1枚作ってもらう。
     * サーバーが理由つきで断ったときの失敗は `AiMaterialRejectedException`
     */
    suspend fun generate(
        prompt: String,
        sourceImage: AiMaterialSourceImage?
    ): Result<GeneratedAiMaterial>
}

class AiMaterialRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val aiMaterialDataSource: AiMaterialDataSource
) : AiMaterialRepository {

    override suspend fun generate(
        prompt: String,
        sourceImage: AiMaterialSourceImage?
    ): Result<GeneratedAiMaterial> = try {
        authRepository.prepareForServerCall()
        Result.success(aiMaterialDataSource.generateMaterial(prompt, sourceImage))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
