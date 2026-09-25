package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.edit.pager.UchiwaBackgroundPage
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UchiwaBackgroundPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        isAllPuffyEnabled: Boolean,
        isPukuPukuSupported: Boolean,
        onAllPuffyEnabledChanged: (Boolean) -> Unit = {},
        onPuffyUnsupportedClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                UchiwaBackgroundPage(
                    onUchiwaColorSelected = {},
                    onBackgroundColorSelected = {},
                    onOverallBorderColorSelected = {},
                    onOverallBorderWeightChanged = {},
                    onOverallBorderWeightChangedFinished = {},
                    onAllPuffyEnabledChanged = onAllPuffyEnabledChanged,
                    onPuffyUnsupportedClick = onPuffyUnsupportedClick,
                    currentUchiwaColor = Color.Red,
                    currentBackgroundColor = Color.Blue,
                    currentOverallBorderColor = Color.White,
                    currentOverallBorderWidth = 8f,
                    isAllPuffyEnabled = isAllPuffyEnabled,
                    isPukuPukuSupported = isPukuPukuSupported
                )
            }
        }
    }

    @Test
    fun puffySwitch_allPuffy_isOn() {
        setContent(isAllPuffyEnabled = true, isPukuPukuSupported = true)

        composeTestRule
            .onNode(hasTestTag(TestTags.PUFFY_SWITCH))
            .performScrollTo()
            .assertIsOn()
    }

    @Test
    fun puffySwitch_notAllPuffy_isOff() {
        setContent(isAllPuffyEnabled = false, isPukuPukuSupported = true)

        composeTestRule
            .onNode(hasTestTag(TestTags.PUFFY_SWITCH))
            .performScrollTo()
            .assertIsOff()
    }

    @Test
    fun puffySwitch_clickWhenOff_requestsAllPuffyOn() {
        var requested: Boolean? = null
        setContent(
            isAllPuffyEnabled = false,
            isPukuPukuSupported = true,
            onAllPuffyEnabledChanged = { requested = it }
        )

        composeTestRule
            .onNode(hasTestTag(TestTags.PUFFY_SWITCH))
            .performScrollTo()
            .performClick()

        assertEquals(true, requested)
    }

    @Test
    fun puffySwitch_whenUnsupported_isDisabled() {
        setContent(isAllPuffyEnabled = false, isPukuPukuSupported = false)

        composeTestRule
            .onNode(hasTestTag(TestTags.PUFFY_SWITCH))
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun puffyRow_whenUnsupported_callsUnsupportedCallback() {
        var wasClicked = false
        setContent(
            isAllPuffyEnabled = false,
            isPukuPukuSupported = false,
            onPuffyUnsupportedClick = { wasClicked = true }
        )

        composeTestRule
            .onNode(hasTestTag(TestTags.PUFFY_ROW))
            .performScrollTo()
            .performClick()

        assertTrue(wasClicked)
    }
}
