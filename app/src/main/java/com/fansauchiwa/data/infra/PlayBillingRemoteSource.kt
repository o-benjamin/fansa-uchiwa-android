package com.fansauchiwa.data.infra

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.fansauchiwa.data.PlayBillingException
import com.fansauchiwa.data.TicketProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayBillingRemoteSource @Inject constructor(
    @ApplicationContext private val context: Context
) : PlayBillingDataSource {

    /** 購入の画面を閉じたときに Play から届く結果。[purchaseTicket] が待っている間だけ受け取る */
    private val purchaseUpdates = MutableSharedFlow<PlayPurchasesUpdate>(extraBufferCapacity = 1)

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener { result, purchases ->
                purchaseUpdates.tryEmit(PlayPurchasesUpdate(result, purchases.orEmpty()))
            }
            // コンビニ払いなど、あとで支払う購入を受け付ける（Play Billing Library 8 以降は指定が必須）
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            // 一度つないだあとに切れても、次の呼び出しでつなぎ直す
            .enableAutoServiceReconnection()
            .build()
    }

    private val connectMutex = Mutex()

    // 購入の画面を出すのに要る。値段を出すときに受け取っておき、購入のたびに問い合わせ直さない
    private var ticketDetails: ProductDetails? = null

    override suspend fun queryTicketProduct(): TicketProduct? {
        val details = queryTicketDetails() ?: return null
        val offer = details.oneTimePurchaseOfferDetails ?: return null
        return TicketProduct(formattedPrice = offer.formattedPrice)
    }

    override suspend fun purchaseTicket(activity: Activity): PlayPurchaseResult {
        val details = ticketDetails ?: queryTicketDetails()
        // 購入の案内は値段が取れてから「買う」を出すので、ふつうは起きない。起きたら Play のエラーとして Crashlytics に残る
        val offer = details?.oneTimePurchaseOfferDetails
            ?: throw PlayBillingException(BillingResponseCode.ITEM_UNAVAILABLE, "回数券のアイテムが見つからない")
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            // 値段の種類（購入オプション・割引）を1つしか作らないアイテムでは、トークンがないことがある
            .apply { offer.offerToken?.let(::setOfferToken) }
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        connect()
        return coroutineScope {
            // 画面を出す前から待つ（出したあとに待ち始めると、すぐ閉じたときの結果を取りこぼす）
            val update = async(start = CoroutineStart.UNDISPATCHED) { purchaseUpdates.first() }
            val launchResult = billingClient.launchBillingFlow(activity, params)
            if (launchResult.responseCode != BillingResponseCode.OK) {
                update.cancel()
                return@coroutineScope toPurchaseResult(launchResult.responseCode, launchResult.debugMessage, emptyList())
            }
            val (result, purchases) = update.await()
            toPurchaseResult(result.responseCode, result.debugMessage, purchases.mapNotNull(::toTicketPurchase))
        }
    }

    override suspend fun queryUnconsumedTicketPurchases(): List<PlayTicketPurchase> {
        connect()
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        )
        result.billingResult.throwIfNotOk()
        return result.purchasesList.mapNotNull(::toTicketPurchase)
    }

    private suspend fun queryTicketDetails(): ProductDetails? {
        connect()
        val result = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(TICKET_PRODUCT_ID)
                            .setProductType(ProductType.INAPP)
                            .build()
                    )
                )
                .build()
        )
        result.billingResult.throwIfNotOk()
        return result.productDetailsList.orEmpty()
            .firstOrNull { it.productId == TICKET_PRODUCT_ID }
            ?.also { ticketDetails = it }
    }

    private suspend fun connect() = connectMutex.withLock {
        if (billingClient.isReady) return@withLock
        val result = suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    // つなぎ直したときにも呼ばれるので、最初の1回だけ受け取る
                    if (continuation.isActive) continuation.resume(billingResult)
                }

                // つなぐ途中で切れたとき（Play ストアの更新中など）は、待ち続けずに失敗にする。つないだあとに切れたときは何もしない
                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            PlayBillingException(BillingResponseCode.SERVICE_DISCONNECTED, "つなぐ途中で Play の課金サービスが切れた")
                        )
                    }
                }
            })
        }
        result.throwIfNotOk()
    }

    private fun BillingResult.throwIfNotOk() {
        if (responseCode != BillingResponseCode.OK) throw PlayBillingException(responseCode, debugMessage)
    }

    companion object {
        /** 回数券のアイテム ID。functions/src/verifyPurchase.ts の `TICKET_PRODUCT_ID` とそろえる */
        const val TICKET_PRODUCT_ID = "ai_material_ticket_5"

        private fun toTicketPurchase(purchase: Purchase): PlayTicketPurchase? =
            if (TICKET_PRODUCT_ID in purchase.products) {
                PlayTicketPurchase(
                    purchaseToken = purchase.purchaseToken,
                    // PENDING のほか、状態が分からない（UNSPECIFIED_STATE）ものも、支払いが済んだとはみなさない
                    isPending = purchase.purchaseState != Purchase.PurchaseState.PURCHASED
                )
            } else {
                null
            }

        /**
         * 購入の画面を閉じたときの Play の応答を、結果に直す。
         * 応答が OK でも回数券の購入が含まれないときは、何が起きたか分からない（支払いが済んでいるかもしれない）ので、
         * Play のエラーとは分けて IllegalStateException にする（Crashlytics に残り、画面は「開き直すと足される」になる）
         */
        internal fun toPurchaseResult(
            responseCode: Int,
            debugMessage: String,
            ticketPurchases: List<PlayTicketPurchase>
        ): PlayPurchaseResult = when (responseCode) {
            BillingResponseCode.OK -> {
                val purchase = ticketPurchases.firstOrNull()
                    ?: throw IllegalStateException("Play が OK を返したが、回数券の購入がない")
                if (purchase.isPending) {
                    PlayPurchaseResult.Pending
                } else {
                    PlayPurchaseResult.Purchased(purchase.purchaseToken)
                }
            }

            BillingResponseCode.USER_CANCELED -> PlayPurchaseResult.Cancelled
            BillingResponseCode.ITEM_ALREADY_OWNED -> PlayPurchaseResult.AlreadyOwned
            else -> throw PlayBillingException(responseCode, debugMessage)
        }
    }
}
