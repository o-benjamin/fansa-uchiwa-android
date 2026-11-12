package com.fansauchiwa.data.infra

import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.TicketPurchaseRejectReason
import com.fansauchiwa.data.TicketPurchaseRejectedException
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseFunctionsTicketPurchaseRemoteSource @Inject constructor(
    private val functions: FirebaseFunctions
) : TicketPurchaseDataSource {

    override suspend fun verifyPurchase(purchaseToken: String): GrantedTickets {
        val response = try {
            functions.getHttpsCallable(VERIFY_FUNCTION_NAME).call(toVerifyRequest(purchaseToken)).await()
        } catch (e: FirebaseFunctionsException) {
            throw rejectReasonFrom(e.details)?.let(::TicketPurchaseRejectedException) ?: e
        }
        return parseResponse(response.getData())
    }

    companion object {
        private const val VERIFY_FUNCTION_NAME = "verifyPurchase"

        internal fun toVerifyRequest(purchaseToken: String): Map<String, Any> = mapOf(
            "productId" to PlayBillingRemoteSource.TICKET_PRODUCT_ID,
            "purchaseToken" to purchaseToken
        )

        internal fun rejectReasonFrom(details: Any?): TicketPurchaseRejectReason? =
            TicketPurchaseRejectReason.fromServerValue((details as? Map<*, *>)?.get("reason") as? String)

        internal fun parseResponse(data: Any?): GrantedTickets {
            val body = requireNotNull(data as? Map<*, *>) { "応答の形が違う" }
            return GrantedTickets(
                ticketsAdded = requireNotNull((body["ticketsAdded"] as? Number)?.toInt()) {
                    "応答に ticketsAdded がない"
                },
                ticketsRemaining = requireNotNull((body["ticketsRemaining"] as? Number)?.toInt()) {
                    "応答に ticketsRemaining がない"
                },
                expiresAt = Instant.parse(
                    requireNotNull(body["expiresAt"] as? String) { "応答に expiresAt がない" }
                ),
                // サーバーは必ず返す。欠けても回数と残りは上の値で正しいので、断らずに新しく足したとして扱う
                alreadyGranted = body["alreadyGranted"] as? Boolean ?: false
            )
        }
    }
}
