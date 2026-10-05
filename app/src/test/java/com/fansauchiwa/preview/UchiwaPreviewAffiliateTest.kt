package com.fansauchiwa.preview

import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.analytics.AffiliateAnalyticsParams
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.ExportedFontAnalytics
import com.fansauchiwa.analytics.FontSessionTracker
import com.fansauchiwa.analytics.PuffyStateAnalytics
import com.fansauchiwa.data.AffiliateLink
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AffiliateRepository
import com.fansauchiwa.data.repository.InAppReviewRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UchiwaPreviewAffiliateTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var affiliateRepository: AffiliateRepository
    private val affiliateLinks = MutableSharedFlow<List<AffiliateLink>>(replay = 1)

    private val link = AffiliateLink(id = "jumbo_uchiwa", label = "ジャンボうちわ", url = "https://amzn.to/a")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        affiliateRepository = mockk(relaxed = true)

        every { adMobRepository.isLoadingRewardedAd } returns MutableStateFlow(false)
        every { affiliateRepository.getAffiliateLinksStream() } returns affiliateLinks
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UchiwaPreviewViewModel = UchiwaPreviewViewModel(
        masterpieceRepository = mockk<MasterpieceRepository>(relaxed = true),
        adMobRepository = adMobRepository,
        analyticsRepository = analyticsRepository,
        inAppReviewRepository = mockk<InAppReviewRepository>(relaxed = true),
        fontSessionTracker = mockk<FontSessionTracker>(relaxed = true),
        puffyStateAnalytics = mockk<PuffyStateAnalytics>(relaxed = true),
        exportedFontAnalytics = mockk<ExportedFontAnalytics>(relaxed = true),
        affiliateRepository = affiliateRepository,
        savedStateHandle = SavedStateHandle()
    )

    @Test
    fun init_fetchesAffiliateLinks() = runTest {
        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { affiliateRepository.fetchAffiliateLinks() }
    }

    @Test
    fun init_linksEmitted_setsAffiliateLinksInUiState() = runTest {
        val viewModel = createViewModel()
        affiliateLinks.emit(listOf(link))
        advanceUntilIdle()

        assertEquals(listOf(link), viewModel.uiState.value.affiliateLinks)
    }

    @Test
    fun init_noLinksEmitted_keepsAffiliateLinksEmpty() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(emptyList<AffiliateLink>(), viewModel.uiState.value.affiliateLinks)
    }

    @Test
    fun init_linksChangedToEmpty_clearsAffiliateLinks() = runTest {
        val viewModel = createViewModel()
        affiliateLinks.emit(listOf(link))
        advanceUntilIdle()
        affiliateLinks.emit(emptyList())
        advanceUntilIdle()

        assertEquals(emptyList<AffiliateLink>(), viewModel.uiState.value.affiliateLinks)
    }

    @Test
    fun logAffiliateLinksShown_sendsViewEvent() = runTest {
        val viewModel = createViewModel()

        viewModel.logAffiliateLinksShown()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.VIEW_PREVIEW_AFFILIATE))
        }
    }

    @Test
    fun logAffiliateLinkTap_sendsTapEventWithItemId() = runTest {
        val viewModel = createViewModel()

        viewModel.logAffiliateLinkTap(link)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.TAP_PREVIEW_AFFILIATE,
                    mapOf(AffiliateAnalyticsParams.PARAM_AFFILIATE_ITEM to "jumbo_uchiwa")
                )
            )
        }
    }
}
