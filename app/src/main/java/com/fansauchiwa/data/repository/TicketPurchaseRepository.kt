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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * AI 素材の回数券の購入（#348）。Play で買い、サーバー（`verifyPurchase`）が購入を確かめて回数を足す。
 * 消費（同じアイテムをまた買えるようにすること）もサーバーが行うので、アプリは消費しない。
 * [purchase] と [grantUnconsumedPurchases] は同時には走らない（同じ購入を2か所から送ると、片方だけが「足し済み」を受け取り、知らせを取り違える）
 */
interface TicketPurchaseRepository {
    /**
     * 購入の案内に出す回数券（値段）。
     * アイテムが見つからないとき（Play Console で作っていない・有効でないなど）の失敗は `TicketProductUnavailableException`
     */
    suspend fun getTicketProduct(): Result<TicketProduct>

    /**
     * 回数券を買い、サーバーで回数を足してもらう。
     * 前に買った分がまだ足せていなくて Play が新しい購入を受け付けなかったときは、その分を足して返す
     * （足し済みだったときは `TicketPurchaseOutcome.AlreadyGranted`）。
     * サーバーが断ったときの失敗は `TicketPurchaseRejectedException`、Play のエラーは `PlayBillingException`
     */
    suspend fun purchase(activity: Activity): Result<TicketPurchaseOutcome>

    /**
     * 買ったのに回数を足せていない購入（足す前にアプリが落ちた・通信が切れた・コンビニ払いがあとで済んだなど）を、
     * サーバーに送り直して足してもらう。足した分を返す（ふつうは空）。AI 素材の画面に来た・戻ったときと、購入の案内を開いたときに呼ぶ
     */
    suspend fun grantUnconsumedPurchases(): Result<List<GrantedTickets>>
}

class TicketPurchaseRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val playBillingDataSource: PlayBillingDataSource,
    private val ticketPurchaseDataSource: TicketPurchaseDataSource
) : TicketPurchaseRepository {

    // 同じ購入を購入と送り直しの2か所から送ると、片方だけが「足し済み」を受け取り、知らせを取り違えるので、同時に走らせない
    private val purchaseMutex = Mutex()

    override suspend fun getTicketProduct(): Result<TicketProduct> = runServerCall {
        playBillingDataSource.queryTicketProduct() ?: throw TicketProductUnavailableException()
    }

    // Play の購入の画面を出している間も離さない。閉じて AI 素材の画面に戻ったときの送り直しが、買ったばかりの購入を先に送らないように
    override suspend fun purchase(activity: Activity): Result<TicketPurchaseOutcome> = runServerCall {
        purchaseMutex.withLock {
            when (val result = playBillingDataSource.purchaseTicket(activity)) {
                is PlayPurchaseResult.Purchased -> TicketPurchaseOutcome.Granted(verify(result.purchaseToken))
                PlayPurchaseResult.Pending -> TicketPurchaseOutcome.Pending
                PlayPurchaseResult.Cancelled -> TicketPurchaseOutcome.Cancelled
                PlayPurchaseResult.AlreadyOwned -> grantOwnedPurchase()
            }
        }
    }

    override suspend fun grantUnconsumedPurchases(): Result<List<GrantedTickets>> = runServerCall {
        purchaseMutex.withLock {
            playBillingDataSource.queryUnconsumedTicketPurchases()
                .filterNot { it.isPending }
                .map { verify(it.purchaseToken) }
        }
    }

    /**
     * Play が「もう持っている」と言ったときに、持っている購入を足す。
     * 消費型のアイテムは消費するまで1つしか持てないので、残っている購入は1つ
     */
    private suspend fun grantOwnedPurchase(): TicketPurchaseOutcome {
        val owned = playBillingDataSource.queryUnconsumedTicketPurchases().firstOrNull()
            ?: throw IllegalStateException("Play は回数券を持っていると返したが、購入が見つからない")
        if (owned.isPending) return TicketPurchaseOutcome.Pending
        val tickets = verify(owned.purchaseToken)
        return if (tickets.alreadyGranted) {
            TicketPurchaseOutcome.AlreadyGranted(tickets.ticketsRemaining)
        } else {
            TicketPurchaseOutcome.Granted(tickets)
        }
    }

    private suspend fun verify(purchaseToken: String): GrantedTickets {
        authRepository.prepareForServerCall()
        return ticketPurchaseDataSource.verifyPurchase(purchaseToken)
    }
}
