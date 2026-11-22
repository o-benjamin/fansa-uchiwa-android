package com.fansauchiwa.data.infra

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.fansauchiwa.data.PlayBillingException
import com.fansauchiwa.data.infra.PlayBillingRemoteSource.Companion.toPurchaseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PlayBillingRemoteSourceTest {

    @Test
    fun toPurchaseResult_okWithPaidPurchase_returnsPurchased() {
        assertEquals(
            PlayPurchaseResult.Purchased("token"),
            toPurchaseResult(BillingResponseCode.OK, "", listOf(PlayTicketPurchase("token", isPending = false)))
        )
    }

    @Test
    fun toPurchaseResult_okWithPendingPurchase_returnsPending() {
        assertEquals(
            PlayPurchaseResult.Pending,
            toPurchaseResult(BillingResponseCode.OK, "", listOf(PlayTicketPurchase("token", isPending = true)))
        )
    }

    @Test
    fun toPurchaseResult_okWithoutTicketPurchase_throwsIllegalState() {
        assertThrows(IllegalStateException::class.java) {
            toPurchaseResult(BillingResponseCode.OK, "", emptyList())
        }
    }

    @Test
    fun toPurchaseResult_userCanceled_returnsCancelled() {
        assertEquals(
            PlayPurchaseResult.Cancelled,
            toPurchaseResult(BillingResponseCode.USER_CANCELED, "", emptyList())
        )
    }

    @Test
    fun toPurchaseResult_itemAlreadyOwned_returnsAlreadyOwned() {
        assertEquals(
            PlayPurchaseResult.AlreadyOwned,
            toPurchaseResult(BillingResponseCode.ITEM_ALREADY_OWNED, "", emptyList())
        )
    }

    @Test
    fun toPurchaseResult_otherError_throwsWithResponseCode() {
        val error = assertThrows(PlayBillingException::class.java) {
            toPurchaseResult(BillingResponseCode.SERVICE_UNAVAILABLE, "network", emptyList())
        }

        assertEquals(BillingResponseCode.SERVICE_UNAVAILABLE, error.responseCode)
    }
}
