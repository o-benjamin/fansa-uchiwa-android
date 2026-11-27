package com.fansauchiwa.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
                        userId = "test-user-id",
                        pushToken = null
                    ),
                    onCopyInstallationId = { copiedInstallationIds.add(it) },
                    onCopyUserId = { copiedUserIds.add(it) },
                    onCopyPushToken = {}
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
                    appInfo = AppInfo(versionName = "2.13.0", installationId = "test-installation-id", userId = null, pushToken = null),
                    onCopyInstallationId = {},
                    onCopyUserId = {},
                    onCopyPushToken = {}
                )
            }
        }

        composeTestRule.onNodeWithText("test-installation-id")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_user_id))
            .assertDoesNotExist()
    }

    @Test
    fun appInfoSection_withoutInstallationId_stillShowsUserId() {
        val copiedUserIds = mutableListOf<String>()
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = null, userId = "test-user-id", pushToken = null),
                    onCopyInstallationId = {},
                    onCopyUserId = { copiedUserIds.add(it) },
                    onCopyPushToken = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_installation_id))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("test-user-id")
            .assertIsDisplayed()
            .performClick()

        assertEquals(listOf("test-user-id"), copiedUserIds)
    }

    @Test
    fun appInfoSection_withoutIds_showsOnlyVersion() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(versionName = "2.13.0", installationId = null, userId = null, pushToken = null),
                    onCopyInstallationId = {},
                    onCopyUserId = {},
                    onCopyPushToken = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_app_version, "2.13.0"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_installation_id))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_user_id))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_push_token))
            .assertDoesNotExist()
    }

    @Test
    fun appInfoSection_withPushToken_showsTokenAndCopiesItOnTap() {
        val copiedPushTokens = mutableListOf<String>()
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(
                        versionName = "2.13.0",
                        installationId = null,
                        userId = null,
                        pushToken = "test-push-token"
                    ),
                    onCopyInstallationId = {},
                    onCopyUserId = {},
                    onCopyPushToken = { copiedPushTokens.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings_push_token))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("test-push-token")
            .assertIsDisplayed()
            .performClick()

        assertEquals(listOf("test-push-token"), copiedPushTokens)
    }

    @Test
    fun appInfoSection_withAllIds_showsUserIdAtTheBottom() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                AppInfoSection(
                    appInfo = AppInfo(
                        versionName = "2.13.0",
                        installationId = "test-installation-id",
                        userId = "test-user-id",
                        pushToken = "test-push-token"
                    ),
                    onCopyInstallationId = {},
                    onCopyUserId = {},
                    onCopyPushToken = {}
                )
            }
        }

        // 問い合わせの案内が「設定画面のいちばん下の利用者 ID」としているので、行を足しても利用者 ID をいちばん下に置く
        val userIdTop = composeTestRule.onNodeWithText("test-user-id").getUnclippedBoundsInRoot().top
        val pushTokenTop = composeTestRule.onNodeWithText("test-push-token").getUnclippedBoundsInRoot().top
        val installationIdTop = composeTestRule.onNodeWithText("test-installation-id").getUnclippedBoundsInRoot().top
        assertTrue(userIdTop > pushTokenTop)
        assertTrue(userIdTop > installationIdTop)
    }
}
