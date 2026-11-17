package com.fansauchiwa.data.repository

import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.infra.InstallationIdDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoRepositoryImplTest {

    private val installationIdDataSource: InstallationIdDataSource = mockk()
    private val repository = AppInfoRepositoryImpl(installationIdDataSource)

    @Test
    fun fetchAppInfo_installationIdAvailable_emitsVersionNameAndInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf("test-installation-id")

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(versionName = BuildConfig.VERSION_NAME, installationId = "test-installation-id"),
            repository.getAppInfoStream().first()
        )
    }

    @Test
    fun fetchAppInfo_installationIdUnavailable_emitsVersionNameWithNullInstallationId() = runTest {
        every { installationIdDataSource.getInstallationIdStream() } returns flowOf(null)

        repository.fetchAppInfo()

        assertEquals(
            AppInfo(versionName = BuildConfig.VERSION_NAME, installationId = null),
            repository.getAppInfoStream().first()
        )
    }
}
