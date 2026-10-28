package com.fansauchiwa.preview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.AffiliateLink
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveSuccessDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val links = listOf(
        AffiliateLink(id = "jumbo_uchiwa", label = "ジャンボうちわ", url = "https://amzn.to/a"),
        AffiliateLink(id = "mirror_sheet", label = "ミラーシート", url = "https://amzn.to/b")
    )

    @Test
    fun saveSuccessDialog_noLinks_hidesAffiliateSection() {
        composeTestRule.setContent {
            SaveSuccessDialog(
                affiliateLinks = emptyList(),
                onAffiliateLinkClick = {},
                onConfirm = {},
                onShare = {},
                onDismissRequest = {}
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.save_success_message)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.affiliate_pr_label)).assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.affiliate_disclosure)).assertDoesNotExist()
    }

    @Test
    fun saveSuccessDialog_withLinks_showsPrLabelLinksAndDisclosure() {
        composeTestRule.setContent {
            SaveSuccessDialog(
                affiliateLinks = links,
                onAffiliateLinkClick = {},
                onConfirm = {},
                onShare = {},
                onDismissRequest = {}
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.affiliate_pr_label)).assertIsDisplayed()
        links.forEach {
            composeTestRule.onNodeWithText(context.getString(R.string.affiliate_link_label, it.label))
                .performScrollTo()
                .assertIsDisplayed()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.affiliate_disclosure))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun saveSuccessDialog_tapLink_passesLinkWithoutClosing() {
        val clicked = mutableListOf<AffiliateLink>()
        var confirmCount = 0
        var shareCount = 0
        var dismissCount = 0
        composeTestRule.setContent {
            SaveSuccessDialog(
                affiliateLinks = links,
                onAffiliateLinkClick = { clicked += it },
                onConfirm = { confirmCount++ },
                onShare = { shareCount++ },
                onDismissRequest = { dismissCount++ }
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.affiliate_link_label, "ミラーシート"))
            .performScrollTo()
            .performClick()

        assertEquals(listOf(links[1]), clicked)
        assertEquals(0, confirmCount)
        assertEquals(0, shareCount)
        assertEquals(0, dismissCount)
    }
}
