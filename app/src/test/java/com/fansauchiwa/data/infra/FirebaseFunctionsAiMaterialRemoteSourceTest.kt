package com.fansauchiwa.data.infra

import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial
import com.fansauchiwa.data.infra.FirebaseFunctionsAiMaterialRemoteSource.Companion.parseResponse
import com.fansauchiwa.data.infra.FirebaseFunctionsAiMaterialRemoteSource.Companion.rejectReasonFrom
import com.fansauchiwa.data.infra.FirebaseFunctionsAiMaterialRemoteSource.Companion.toReportRequest
import com.fansauchiwa.data.infra.FirebaseFunctionsAiMaterialRemoteSource.Companion.toRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseFunctionsAiMaterialRemoteSourceTest {

    private val validBody = mapOf(
        "image" to "aW1n",
        "mimeType" to "image/png",
        "paidWith" to "ticket",
        "ticketsRemaining" to 4,
        "model" to "gemini-3.1-flash-lite-image"
    )

    @Test
    fun toRequest_withoutSourceImage_sendsOnlyPrompt() {
        assertEquals(mapOf("prompt" to "星"), toRequest("星", null))
    }

    @Test
    fun toRequest_withSourceImage_sendsMimeTypeAndData() {
        val request = toRequest("星", AiMaterialSourceImage("image/jpeg", "aW1n"))

        assertEquals(mapOf("mimeType" to "image/jpeg", "data" to "aW1n"), request["sourceImage"])
    }

    @Test
    fun rejectReasonFrom_knownReason_returnsReason() {
        assertEquals(
            AiMaterialRejectReason.BLOCKED_NAME,
            rejectReasonFrom(mapOf("reason" to "blocked_name"))
        )
    }

    @Test
    fun rejectReasonFrom_noDetailsOrUnknownReason_returnsNull() {
        assertNull(rejectReasonFrom(null))
        assertNull(rejectReasonFrom("blocked_name"))
        assertNull(rejectReasonFrom(mapOf("reason" to "new_reason")))
    }

    @Test
    fun parseResponse_valid_returnsMaterial() {
        assertEquals(
            GeneratedAiMaterial("aW1n", "image/png", AiMaterialPayment.TICKET, 4, "gemini-3.1-flash-lite-image"),
            parseResponse(validBody)
        )
    }

    @Test
    fun parseResponse_ticketsRemainingAsLongOrZero_isConverted() {
        assertEquals(0, parseResponse(validBody + ("ticketsRemaining" to 0L)).ticketsRemaining)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_null_throws() {
        parseResponse(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_missingImage_throws() {
        parseResponse(validBody - "image")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_unknownPayment_throws() {
        parseResponse(validBody + ("paidWith" to "coupon"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_missingModel_throws() {
        parseResponse(validBody - "model")
    }

    @Test
    fun toReportRequest_sendsPromptReasonServerValueAndModel() {
        assertEquals(
            mapOf("prompt" to "星", "reason" to "real_person", "model" to "gemini-3.1-flash-lite-image"),
            toReportRequest("星", AiMaterialReportReason.REAL_PERSON, "gemini-3.1-flash-lite-image")
        )
    }
}
