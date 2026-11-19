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
    fun appInfoSection_withInstallationIdAndUserId_showsBothAndCopiesEachIdOnTap() {
        val copiedInstallationIds = mutableListOf<String>()
        val copiedUserIds = mutableListOf<String>()
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(
                        versionName = "2.13.0",
                        installationId = "test-installation-id",
                        userId = "test-user-id"
                    ),
                    onCopyInstallationId = { copiedInstallationIds.add(it) },
                    onCopyUserId = { copiedUserIds.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_version, "2.13.0"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_installation_id))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_user_id))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("test-installation-id")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("test-user-id")
            .assertIsDisplayed()
            .performClick()

        assertEquals(listOf("test-installation-id"), copiedInstallationIds)
        assertEquals(listOf("test-user-id"), copiedUserIds)
    }

    @Test
    fun appInfoSection_withoutUserId_hidesUserIdRow() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = "test-installation-id", userId = null),
                    onCopyInstallationId = {},
                    onCopyUserId = {}
                )
            }
        }

        composeTestRule.onNodeWithText("test-installation-id")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_user_id))
            .assertDoesNotExist()
    }

    @Test
    fun appInfoSection_withoutIds_showsOnlyVersion() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = null, userId = null),
                    onCopyInstallationId = {},
                    onCopyUserId = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_version, "2.13.0"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_installation_id))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_user_id))
            .assertDoesNotExist()
    }
}
