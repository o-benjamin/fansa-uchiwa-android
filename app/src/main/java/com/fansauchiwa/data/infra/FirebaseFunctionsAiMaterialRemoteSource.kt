package com.fansauchiwa.data.infra

import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportLimitException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseFunctionsAiMaterialRemoteSource @Inject constructor(
    private val functions: FirebaseFunctions
) : AiMaterialDataSource {

    override suspend fun generateMaterial(
        prompt: String,
        sourceImage: AiMaterialSourceImage?
    ): GeneratedAiMaterial {
        val callable = functions.getHttpsCallable(GENERATE_FUNCTION_NAME)
        callable.setTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        val response = try {
            callable.call(toGenerateRequest(prompt, sourceImage)).await()
        } catch (e: FirebaseFunctionsException) {
            throw rejectReasonFrom(e.details)?.let(::AiMaterialRejectedException) ?: e
        }
        return parseResponse(response.getData())
    }

    override suspend fun reportMaterial(
        prompt: String,
        reason: AiMaterialReportReason,
        model: String
    ) {
        try {
            functions.getHttpsCallable(REPORT_FUNCTION_NAME)
                .call(toReportRequest(prompt, reason, model))
                .await()
        } catch (e: FirebaseFunctionsException) {
            throw if (isReportLimit(e.details)) AiMaterialReportLimitException() else e
        }
    }

    companion object {
        private const val GENERATE_FUNCTION_NAME = "generateMaterial"
        private const val REPORT_FUNCTION_NAME = "reportMaterial"
        private const val REPORT_LIMIT_REASON = "too_many_reports"

        // 既定の70秒だと、サーバー（timeoutSeconds: 120）が作り終える前に諦めて、回数だけ減ってしまう
        private const val CALL_TIMEOUT_SECONDS = 130L

        internal fun toGenerateRequest(
            prompt: String,
            sourceImage: AiMaterialSourceImage?
        ): Map<String, Any> = buildMap {
            put("prompt", prompt)
            if (sourceImage != null) {
                put(
                    "sourceImage",
                    mapOf("mimeType" to sourceImage.mimeType, "data" to sourceImage.base64Data)
                )
            }
        }

        internal fun toReportRequest(
            prompt: String,
            reason: AiMaterialReportReason,
            model: String
        ): Map<String, Any> = mapOf(
            "prompt" to prompt,
            "reason" to reason.serverValue,
            "model" to model
        )

        internal fun isReportLimit(details: Any?): Boolean =
            (details as? Map<*, *>)?.get("reason") == REPORT_LIMIT_REASON

        internal fun rejectReasonFrom(details: Any?): AiMaterialRejectReason? =
            AiMaterialRejectReason.fromServerValue((details as? Map<*, *>)?.get("reason") as? String)

        internal fun parseResponse(data: Any?): GeneratedAiMaterial {
            val body = requireNotNull(data as? Map<*, *>) { "応答の形が違う" }
            return GeneratedAiMaterial(
                imageBase64 = requireNotNull(body["image"] as? String) { "応答に image がない" },
                mimeType = requireNotNull(body["mimeType"] as? String) { "応答に mimeType がない" },
                payment = requireNotNull(
                    AiMaterialPayment.fromServerValue(body["paidWith"] as? String)
                ) { "応答の paidWith が想定外" },
                ticketsRemaining = requireNotNull((body["ticketsRemaining"] as? Number)?.toInt()) {
                    "応答に ticketsRemaining がない"
                },
                model = requireNotNull(body["model"] as? String) { "応答に model がない" }
            )
        }
    }
}
