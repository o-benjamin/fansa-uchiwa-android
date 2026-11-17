package com.fansauchiwa.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInfoSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun appInfoSection_withInstallationId_showsVersionAndIdAndCopiesIdOnTap() {
        val copiedIds = mutableListOf<String>()
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = "test-installation-id"),
                    onCopyInstallationId = { copiedIds.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_version, "2.13.0"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("test-installation-id")
            .assertIsDisplayed()
            .performClick()

        assertEquals(listOf("test-installation-id"), copiedIds)
    }

    @Test
    fun appInfoSection_withoutInstallationId_showsOnlyVersion() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = null),
                    onCopyInstallationId = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_version, "2.13.0"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_installation_id))
            .assertDoesNotExist()
    }
}
