package com.fansauchiwa.data

/**
 * サーバー（`verifyPurchase`）が回数を足すのを断った理由。[serverValue] は functions/src/verifyPurchase.ts の `PurchaseRejectReason` とそろえる
 */
enum class TicketPurchaseRejectReason(val serverValue: String) {
    UNAUTHENTICATED("unauthenticated"),
    INVALID_PURCHASE("invalid_purchase"),
    PENDING("pending"),
    NOT_PURCHASED("not_purchased"),
    OWNED_BY_OTHER("owned_by_other"),
    VERIFICATION_FAILED("verification_failed");

    companion object {
        fun fromServerValue(value: String?): TicketPurchaseRejectReason? =
            entries.firstOrNull { it.serverValue == value }
    }
}
