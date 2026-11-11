package com.fansauchiwa.data.repository

import android.app.Activity
import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.PlayBillingException
import com.fansauchiwa.data.TicketProduct
import com.fansauchiwa.data.TicketProductUnavailableException
import com.fansauchiwa.data.TicketPurchaseOutcome
import com.fansauchiwa.data.TicketPurchaseRejectReason
import com.fansauchiwa.data.TicketPurchaseRejectedException
import com.fansauchiwa.data.infra.PlayBillingDataSource
import com.fansauchiwa.data.infra.PlayPurchaseResult
import com.fansauchiwa.data.infra.PlayTicketPurchase
import com.fansauchiwa.data.infra.TicketPurchaseDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TicketPurchaseRepositoryImplTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var playBillingDataSource: PlayBillingDataSource
    private lateinit var ticketPurchaseDataSource: TicketPurchaseDataSource
    private lateinit var repository: TicketPurchaseRepositoryImpl

    private val activity = mockk<Activity>()
    private val granted = GrantedTickets(5, 5, Instant.parse("2027-05-11T05:00:00Z"), alreadyGranted = false)

    @Before
    fun setUp() {
        authRepository = mockk()
        playBillingDataSource = mockk()
        ticketPurchaseDataSource = mockk()
        coEvery { authRepository.prepareForServerCall() } returns "uid"
        coEvery { ticketPurchaseDataSource.verifyPurchase(any()) } returns granted
        repository = TicketPurchaseRepositoryImpl(authRepository, playBillingDataSource, ticketPurchaseDataSource)
    }

    @Test
    fun getTicketProduct_found_returnsProduct() = runTest {
        coEvery { playBillingDataSource.queryTicketProduct() } returns TicketProduct("¥160")

        assertEquals(TicketProduct("¥160"), repository.getTicketProduct().getOrThrow())
    }

    @Test
    fun getTicketProduct_notFound_failsWithUnavailable() = runTest {
        coEvery { playBillingDataSource.queryTicketProduct() } returns null

        assertTrue(repository.getTicketProduct().exceptionOrNull() is TicketProductUnavailableException)
    }

    @Test
    fun getTicketProduct_playError_fails() = runTest {
        val error = PlayBillingException(2, "network")
        coEvery { playBillingDataSource.queryTicketProduct() } throws error

        assertEquals(error, repository.getTicketProduct().exceptionOrNull())
    }

    @Test
    fun purchase_purchased_preparesAuthThenVerifiesAndReturnsGranted() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.Purchased("token")

        val outcome = repository.purchase(activity).getOrThrow()

        assertEquals(TicketPurchaseOutcome.Granted(granted), outcome)
        coVerifyOrder {
            authRepository.prepareForServerCall()
            ticketPurchaseDataSource.verifyPurchase("token")
        }
    }

    @Test
    fun purchase_pending_returnsPendingWithoutCallingServer() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.Pending

        assertEquals(TicketPurchaseOutcome.Pending, repository.purchase(activity).getOrThrow())
        coVerify(exactly = 0) { ticketPurchaseDataSource.verifyPurchase(any()) }
    }

    @Test
    fun purchase_cancelled_returnsCancelledWithoutCallingServer() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.Cancelled

        assertEquals(TicketPurchaseOutcome.Cancelled, repository.purchase(activity).getOrThrow())
        coVerify(exactly = 0) { authRepository.prepareForServerCall() }
    }

    @Test
    fun purchase_serverRejects_failsWithReason() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.Purchased("token")
        coEvery { ticketPurchaseDataSource.verifyPurchase("token") } throws
            TicketPurchaseRejectedException(TicketPurchaseRejectReason.VERIFICATION_FAILED)

        val error = repository.purchase(activity).exceptionOrNull()

        assertEquals(
            TicketPurchaseRejectReason.VERIFICATION_FAILED,
            (error as TicketPurchaseRejectedException).reason
        )
    }

    @Test
    fun purchase_alreadyOwnedPaid_verifiesOwnedPurchase() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.AlreadyOwned
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns
            listOf(PlayTicketPurchase("owned", isPending = false))

        assertEquals(TicketPurchaseOutcome.Granted(granted), repository.purchase(activity).getOrThrow())
        coVerify { ticketPurchaseDataSource.verifyPurchase("owned") }
    }

    @Test
    fun purchase_alreadyOwnedPending_returnsPendingWithoutCallingServer() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.AlreadyOwned
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns
            listOf(PlayTicketPurchase("owned", isPending = true))

        assertEquals(TicketPurchaseOutcome.Pending, repository.purchase(activity).getOrThrow())
        coVerify(exactly = 0) { ticketPurchaseDataSource.verifyPurchase(any()) }
    }

    @Test
    fun purchase_alreadyOwnedButNothingFound_fails() = runTest {
        coEvery { playBillingDataSource.purchaseTicket(activity) } returns PlayPurchaseResult.AlreadyOwned
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns emptyList()

        assertTrue(repository.purchase(activity).exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun purchase_playError_fails() = runTest {
        val error = PlayBillingException(3, "billing unavailable")
        coEvery { playBillingDataSource.purchaseTicket(activity) } throws error

        assertEquals(error, repository.purchase(activity).exceptionOrNull())
    }

    @Test
    fun grantUnconsumedPurchases_none_returnsEmptyWithoutSigningIn() = runTest {
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns emptyList()

        assertEquals(emptyList<GrantedTickets>(), repository.grantUnconsumedPurchases().getOrThrow())
        coVerify(exactly = 0) { authRepository.prepareForServerCall() }
    }

    @Test
    fun grantUnconsumedPurchases_paidAndPending_verifiesOnlyPaid() = runTest {
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns listOf(
            PlayTicketPurchase("paid", isPending = false),
            PlayTicketPurchase("pending", isPending = true)
        )

        assertEquals(listOf(granted), repository.grantUnconsumedPurchases().getOrThrow())
        coVerify(exactly = 1) { ticketPurchaseDataSource.verifyPurchase("paid") }
        coVerify(exactly = 0) { ticketPurchaseDataSource.verifyPurchase("pending") }
    }

    @Test
    fun grantUnconsumedPurchases_serverFails_fails() = runTest {
        val error = IllegalStateException("network")
        coEvery { playBillingDataSource.queryUnconsumedTicketPurchases() } returns
            listOf(PlayTicketPurchase("paid", isPending = false))
        coEvery { ticketPurchaseDataSource.verifyPurchase("paid") } throws error

        assertEquals(error, repository.grantUnconsumedPurchases().exceptionOrNull())
    }
}
