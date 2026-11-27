package com.fansauchiwa.data.repository

import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.infra.AuthDataSource
import com.fansauchiwa.data.infra.InstallationIdDataSource
import com.fansauchiwa.data.infra.PushMessagingDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AppInfoRepositoryImplTest {

    private val installationIdDataSource: InstallationIdDataSource = mockk()
    private val authDataSource: AuthDataSource = mockk()
    private val pushMessagingDataSource: PushMessagingDataSource = mockk()
    private val repository =
        AppInfoRepositoryImpl(installationIdDataSource, authDataSource, pushMessagingDataSource)

    @Before
    fun setUp() {
        every { pushMessagingDataSource.getTokenStream() } returns flowOf(null)
    }

    @Test
    fun fetchAppInfo_installationIdAvailable_emitsVersionNameAndInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(
                versionName = BuildConfig.VERSION_NAME,
                installationId = "test-installation-id",
                userId = null,
                pushToken = null
            ),
            repository.getAppInfoStream().first()
        )
    }

    @Test
    fun fetchAppInfo_installationIdUnavailable_emitsVersionNameWithNullInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf(null)
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(versionName = BuildConfig.VERSION_NAME, installationId = null, userId = null, pushToken = null),
            repository.getAppInfoStream().first()
        )
    }

    @Test
    fun fetchAppInfo_signedIn_emitsUserId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns "test-user-id"

        repository.fetchAppInfo()

        assertEquals("test-user-id", repository.getAppInfoStream().first().userId)
    }

    @Test
    fun fetchAppInfo_notSignedIn_doesNotSignInAnonymously() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        // 設定画面を開いただけで、AI 素材を使わない人の匿名の利用者を作らない
        coVerify(exactly = 0) { authDataSource.signInAnonymously() }
    }

    @Test
    fun fetchAppInfo_pushTokenAvailable_emitsPushToken() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns null
        every { pushMessagingDataSource.getTokenStream() } returns flowOf("test-push-token")

        repository.fetchAppInfo()

        assertEquals("test-push-token", repository.getAppInfoStream().first().pushToken)
    }

    @Test
    fun fetchAppInfo_pushTokenUnavailable_emitsNullPushToken() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        assertNull(repository.getAppInfoStream().first().pushToken)
    }

    @Test
    fun fetchAppInfo_pushTokenPending_emitsOtherIdsWithoutWaiting() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns "test-user-id"
        // オフラインなどでトークンの取得が終わらない
        every { pushMessagingDataSource.getTokenStream() } returns flow { awaitCancellation() }

        backgroundScope.launch { repository.fetchAppInfo() }
        runCurrent()

        assertEquals(
            AppInfo(
                versionName = BuildConfig.VERSION_NAME,
                installationId = "test-installation-id",
                userId = "test-user-id",
                pushToken = null
            ),
            repository.getAppInfoStream().first()
        )
    }
}
