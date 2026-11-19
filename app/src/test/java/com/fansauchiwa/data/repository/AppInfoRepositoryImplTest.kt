package com.fansauchiwa.data.repository

import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.infra.AuthDataSource
import com.fansauchiwa.data.infra.InstallationIdDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoRepositoryImplTest {

    private val installationIdDataSource: InstallationIdDataSource = mockk()
    private val authDataSource: AuthDataSource = mockk()
    private val repository = AppInfoRepositoryImpl(installationIdDataSource, authDataSource)

    @Test
    fun fetchAppInfo_installationIdAvailable_emitsVersionNameAndInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(versionName = BuildConfig.VERSION_NAME, installationId = "test-installation-id", userId = null),
            repository.getAppInfoStream().first()
        )
    }

    @Test
    fun fetchAppInfo_installationIdUnavailable_emitsVersionNameWithNullInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf(null)
        every { authDataSource.getCurrentUserId() } returns null

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(versionName = BuildConfig.VERSION_NAME, installationId = null, userId = null),
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
}
