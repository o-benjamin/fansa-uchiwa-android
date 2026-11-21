package com.fansauchiwa.data.infra

import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.TicketPurchaseRejectReason
import com.fansauchiwa.data.infra.FirebaseFunctionsTicketPurchaseRemoteSource.Companion.parseResponse
import com.fansauchiwa.data.infra.FirebaseFunctionsTicketPurchaseRemoteSource.Companion.rejectReasonFrom
import com.fansauchiwa.data.infra.FirebaseFunctionsTicketPurchaseRemoteSource.Companion.toVerifyRequest
import java.time.Instant
import java.time.format.DateTimeParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseFunctionsTicketPurchaseRemoteSourceTest {

    private val validBody = mapOf(
        "ticketsAdded" to 5,
        "ticketsRemaining" to 7,
        "expiresAt" to "2027-05-11T05:00:00.000Z",
        "alreadyGranted" to false
    )

    @Test
    fun toVerifyRequest_token_sendsTicketProductIdAndToken() {
        assertEquals(
            mapOf("productId" to "ai_material_ticket_5", "purchaseToken" to "token"),
            toVerifyRequest("token")
        )
    }

    @Test
    fun rejectReasonFrom_knownReason_returnsReason() {
        assertEquals(
            TicketPurchaseRejectReason.OWNED_BY_OTHER,
            rejectReasonFrom(mapOf("reason" to "owned_by_other"))
        )
    }

    @Test
    fun rejectReasonFrom_noDetailsOrUnknownReason_returnsNull() {
        assertNull(rejectReasonFrom(null))
        assertNull(rejectReasonFrom("owned_by_other"))
        assertNull(rejectReasonFrom(mapOf("reason" to "new_reason")))
    }

    @Test
    fun parseResponse_valid_returnsGrantedTickets() {
        assertEquals(
            GrantedTickets(5, 7, Instant.parse("2027-05-11T05:00:00Z"), alreadyGranted = false),
            parseResponse(validBody)
        )
    }

    @Test
    fun parseResponse_numbersAsLong_returnsInts() {
        val granted = parseResponse(validBody + mapOf("ticketsAdded" to 5L, "ticketsRemaining" to 0L))

        assertEquals(5, granted.ticketsAdded)
        assertEquals(0, granted.ticketsRemaining)
    }

    @Test
    fun parseResponse_withoutAlreadyGranted_treatsAsNewGrant() {
        assertFalse(parseResponse(validBody - "alreadyGranted").alreadyGranted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_notMap_throws() {
        parseResponse("ok")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_withoutTicketsRemaining_throws() {
        parseResponse(validBody - "ticketsRemaining")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseResponse_withoutExpiresAt_throws() {
        parseResponse(validBody - "expiresAt")
    }

    @Test(expected = DateTimeParseException::class)
    fun parseResponse_malformedExpiresAt_throws() {
        parseResponse(validBody + mapOf("expiresAt" to "2027/05/11"))
    }
}
