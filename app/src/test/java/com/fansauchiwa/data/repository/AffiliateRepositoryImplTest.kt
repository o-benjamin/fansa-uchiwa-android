package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.AffiliateLink
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AffiliateRepositoryImplTest {

    private lateinit var remoteConfigDataSource: RemoteConfigDataSource
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var repository: AffiliateRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0

        remoteConfigDataSource = mockk()
        crashReportingRepository = mockk(relaxed = true)
        repository = AffiliateRepositoryImpl(remoteConfigDataSource, crashReportingRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun fetchAffiliateLinks_validJson_emitsParsedLinks() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flowOf(
            """{"items": [{"id": "a", "label": "うちわ", "url": "https://amzn.to/a"}]}"""
        )

        repository.fetchAffiliateLinks()

        assertEquals(
            listOf(AffiliateLink("a", "うちわ", "https://amzn.to/a")),
            repository.getAffiliateLinksStream().first()
        )
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchAffiliateLinks_emptyValue_emitsEmptyListWithoutRecording() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flowOf("")

        repository.fetchAffiliateLinks()

        assertEquals(emptyList<AffiliateLink>(), repository.getAffiliateLinksStream().first())
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchAffiliateLinks_malformedJson_emitsEmptyListAndRecordsException() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flowOf("""{"items": [""")

        repository.fetchAffiliateLinks()

        assertEquals(emptyList<AffiliateLink>(), repository.getAffiliateLinksStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchAffiliateLinks_malformedJsonTwice_recordsExceptionOnlyOnce() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flowOf("""{"items": [""")

        repository.fetchAffiliateLinks()
        repository.fetchAffiliateLinks()

        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchAffiliateLinks_allItemsUnusable_emitsEmptyListAndRecordsException() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flowOf(
            """{"items": [{"id": "a", "label": "A", "url": "https://example.com/a"}]}"""
        )

        repository.fetchAffiliateLinks()

        assertEquals(emptyList<AffiliateLink>(), repository.getAffiliateLinksStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun fetchAffiliateLinks_dataSourceThrows_emitsEmptyListAndRecordsException() = runTest {
        val error = IOException("読めない")
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returns flow { throw error }

        repository.fetchAffiliateLinks()

        assertEquals(emptyList<AffiliateLink>(), repository.getAffiliateLinksStream().first())
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun fetchAffiliateLinks_calledTwice_emitsLatestValue() = runTest {
        every { remoteConfigDataSource.getAffiliateMaterialsJsonStream() } returnsMany listOf(
            flowOf("""{"items": [{"id": "a", "label": "うちわ", "url": "https://amzn.to/a"}]}"""),
            flowOf("")
        )

        repository.fetchAffiliateLinks()
        repository.fetchAffiliateLinks()

        assertEquals(emptyList<AffiliateLink>(), repository.getAffiliateLinksStream().first())
    }
}
