package com.fansauchiwa.data.repository

import android.app.Activity
import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.TicketProduct
import com.fansauchiwa.data.TicketProductUnavailableException
import com.fansauchiwa.data.TicketPurchaseOutcome
import com.fansauchiwa.data.infra.PlayBillingDataSource
import com.fansauchiwa.data.infra.PlayPurchaseResult
import com.fansauchiwa.data.infra.TicketPurchaseDataSource
import javax.inject.Inject

/**
 * AI 素材の回数券の購入（#348）。Play で買い、サーバー（`verifyPurchase`）が購入を確かめて回数を足す。
 * 消費（同じアイテムをまた買えるようにすること）もサーバーが行うので、アプリは消費しない
 */
interface TicketPurchaseRepository {
    /**
     * 購入の画面に出す回数券（値段）。
     * アイテムが見つからないとき（Play Console で作っていない・有効でないなど）の失敗は `TicketProductUnavailableException`
     */
    suspend fun getTicketProduct(): Result<TicketProduct>

    /**
     * 回数券を買い、サーバーで回数を足してもらう。
     * 前に買った分がまだ足せていなくて Play が新しい購入を受け付けなかったときは、その分を足して返す。
     * サーバーが断ったときの失敗は `TicketPurchaseRejectedException`、Play のエラーは `PlayBillingException`
     */
    suspend fun purchase(activity: Activity): Result<TicketPurchaseOutcome>

    /**
     * 買ったのに回数を足せていない購入（足す前にアプリが落ちた・通信が切れた・コンビニ払いがあとで済んだなど）を、
     * サーバーに送り直して足してもらう。足した分を返す（ふつうは空）。購入の画面を開いたときに呼ぶ
     */
    suspend fun grantUnconsumedPurchases(): Result<List<GrantedTickets>>
}

class TicketPurchaseRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val playBillingDataSource: PlayBillingDataSource,
    private val ticketPurchaseDataSource: TicketPurchaseDataSource
) : TicketPurchaseRepository {

    override suspend fun getTicketProduct(): Result<TicketProduct> = runServerCall {
        playBillingDataSource.queryTicketProduct() ?: throw TicketProductUnavailableException()
    }

    override suspend fun purchase(activity: Activity): Result<TicketPurchaseOutcome> = runServerCall {
        when (val result = playBillingDataSource.purchaseTicket(activity)) {
            is PlayPurchaseResult.Purchased -> TicketPurchaseOutcome.Granted(verify(result.purchaseToken))
            PlayPurchaseResult.Pending -> TicketPurchaseOutcome.Pending
            PlayPurchaseResult.Cancelled -> TicketPurchaseOutcome.Cancelled
            PlayPurchaseResult.AlreadyOwned -> grantOwnedPurchase()
        }
    }

    override suspend fun grantUnconsumedPurchases(): Result<List<GrantedTickets>> = runServerCall {
        playBillingDataSource.queryUnconsumedTicketPurchases()
            .filterNot { it.isPending }
            .map { verify(it.purchaseToken) }
    }

    /**
     * Play が「もう持っている」と言ったときに、持っている購入を足す。
     * 消費型のアイテムは消費するまで1つしか持てないので、残っている購入は1つ
     */
    private suspend fun grantOwnedPurchase(): TicketPurchaseOutcome {
        val owned = playBillingDataSource.queryUnconsumedTicketPurchases().firstOrNull()
            ?: throw IllegalStateException("Play は回数券を持っていると返したが、購入が見つからない")
        return if (owned.isPending) {
            TicketPurchaseOutcome.Pending
        } else {
            TicketPurchaseOutcome.Granted(verify(owned.purchaseToken))
        }
    }

    private suspend fun verify(purchaseToken: String): GrantedTickets {
        authRepository.prepareForServerCall()
        return ticketPurchaseDataSource.verifyPurchase(purchaseToken)
    }
}
