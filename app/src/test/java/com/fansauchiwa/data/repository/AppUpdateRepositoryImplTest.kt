package com.fansauchiwa.data.repository

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.UpdateVersionCodes
import com.fansauchiwa.data.infra.AppUpdateDataSource
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import com.fansauchiwa.update.UpdatePromptPolicy
import com.fansauchiwa.update.UpdatePromptType
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppUpdateRepositoryImplTest {

    private lateinit var appUpdateDataSource: AppUpdateDataSource
    private lateinit var remoteConfigDataSource: RemoteConfigDataSource
    private lateinit var repository: AppUpdateRepositoryImpl
    private val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()
    private val current = BuildConfig.VERSION_CODE.toLong()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0

        appUpdateDataSource = mockk(relaxed = true)
        remoteConfigDataSource = mockk()
        repository = AppUpdateRepositoryImpl(appUpdateDataSource, remoteConfigDataSource)

        givenVersionCodes(UpdateVersionCodes())
        givenAppUpdateInfo(noUpdate())
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun givenVersionCodes(versionCodes: UpdateVersionCodes) {
        every { remoteConfigDataSource.getUpdateVersionCodesStream() } returns flowOf(versionCodes)
    }

    private fun givenAppUpdateInfo(info: AppUpdateInfo) {
        coEvery { appUpdateDataSource.requestAppUpdateInfo() } returns info
    }

    private fun noUpdate(installStatus: Int = InstallStatus.UNKNOWN): AppUpdateInfo = mockk {
        every { updateAvailability() } returns UpdateAvailability.UPDATE_NOT_AVAILABLE
        every { installStatus() } returns installStatus
    }

    private fun availableUpdate(
        stalenessDays: Int? = UpdatePromptPolicy.FLEXIBLE_MIN_STALENESS_DAYS,
        isFlexibleAllowed: Boolean = true,
        isImmediateAllowed: Boolean = true,
        installStatus: Int = InstallStatus.UNKNOWN,
        updateAvailability: Int = UpdateAvailability.UPDATE_AVAILABLE
    ): AppUpdateInfo = mockk {
        every { updateAvailability() } returns updateAvailability
        every { availableVersionCode() } returns (current + 1).toInt()
        every { isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) } returns isFlexibleAllowed
        every { isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) } returns isImmediateAllowed
        every { clientVersionStalenessDays() } returns stalenessDays
        every { installStatus() } returns installStatus
    }

    // アプリから始めた更新が途中にある。続きを出すときに isUpdateTypeAllowed に頼らないことを確かめるため、どちらも false にする
    private fun updateInProgress(): AppUpdateInfo = availableUpdate(
        isFlexibleAllowed = false,
        isImmediateAllowed = false,
        updateAvailability = UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
    )

    @Test
    fun takeUpdatePromptForLaunch_staleFlexibleUpdate_returnsFlexibleOnlyOnce() = runTest {
        givenAppUpdateInfo(availableUpdate())

        assertEquals(UpdatePromptType.FLEXIBLE, repository.takeUpdatePromptForLaunch())
        assertEquals(UpdatePromptType.NONE, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_belowMin_returnsMinOnlyOnce() = runTest {
        givenVersionCodes(UpdateVersionCodes(minVersionCode = current + 1))

        assertEquals(UpdatePromptType.MIN, repository.takeUpdatePromptForLaunch())
        assertEquals(UpdatePromptType.NONE, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_belowForce_returnsForceEveryTimeWithoutAskingAgain() = runTest {
        givenVersionCodes(UpdateVersionCodes(forceVersionCode = current + 1))
        givenAppUpdateInfo(availableUpdate())

        assertEquals(UpdatePromptType.FORCE, repository.takeUpdatePromptForLaunch())
        assertEquals(UpdatePromptType.FORCE, repository.takeUpdatePromptForLaunch())
        coVerify(exactly = 1) { appUpdateDataSource.requestAppUpdateInfo() }
    }

    @Test
    fun takeUpdatePromptForLaunch_belowForceButPlayFails_returnsForceFallback() = runTest {
        givenVersionCodes(UpdateVersionCodes(forceVersionCode = current + 1))
        coEvery { appUpdateDataSource.requestAppUpdateInfo() } throws IOException("Play がない")

        assertEquals(UpdatePromptType.FORCE_FALLBACK, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_belowForceButNoUpdateOnPlay_returnsForceFallback() = runTest {
        givenVersionCodes(UpdateVersionCodes(forceVersionCode = current + 1))
        givenAppUpdateInfo(noUpdate())

        assertEquals(UpdatePromptType.FORCE_FALLBACK, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_belowForceAndUpdateInProgress_returnsForce() = runTest {
        givenVersionCodes(UpdateVersionCodes(forceVersionCode = current + 1))
        givenAppUpdateInfo(updateInProgress())

        assertEquals(UpdatePromptType.FORCE, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_staleUpdateAlreadyDownloading_returnsNone() = runTest {
        givenAppUpdateInfo(availableUpdate(installStatus = InstallStatus.DOWNLOADING))

        assertEquals(UpdatePromptType.NONE, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun takeUpdatePromptForLaunch_noUpdate_returnsNone() = runTest {
        assertEquals(UpdatePromptType.NONE, repository.takeUpdatePromptForLaunch())
    }

    @Test
    fun startImmediateUpdate_updateAllowed_startsFlowWithFreshInfo() = runTest {
        val info = availableUpdate()
        givenAppUpdateInfo(info)
        every { appUpdateDataSource.startUpdateFlow(info, launcher, AppUpdateType.IMMEDIATE) } returns true

        assertTrue(repository.startImmediateUpdate(launcher))
        coVerify(exactly = 1) { appUpdateDataSource.requestAppUpdateInfo() }
    }

    @Test
    fun startImmediateUpdate_typeNotAllowed_returnsFalseWithoutStarting() = runTest {
        givenAppUpdateInfo(availableUpdate(isImmediateAllowed = false))

        assertFalse(repository.startImmediateUpdate(launcher))
        verify(exactly = 0) { appUpdateDataSource.startUpdateFlow(any(), any(), any()) }
    }

    @Test
    fun startImmediateUpdate_updateInProgress_resumesImmediateUpdate() = runTest {
        val info = updateInProgress()
        givenAppUpdateInfo(info)
        every { appUpdateDataSource.startUpdateFlow(info, launcher, AppUpdateType.IMMEDIATE) } returns true

        assertTrue(repository.startImmediateUpdate(launcher))
    }

    @Test
    fun startFlexibleUpdate_updateInProgress_returnsFalse() = runTest {
        givenAppUpdateInfo(updateInProgress())

        assertFalse(repository.startFlexibleUpdate(launcher))
        verify(exactly = 0) { appUpdateDataSource.startUpdateFlow(any(), any(), any()) }
    }

    @Test
    fun startFlexibleUpdate_alreadyDownloaded_returnsFalse() = runTest {
        givenAppUpdateInfo(availableUpdate(installStatus = InstallStatus.DOWNLOADED))

        assertFalse(repository.startFlexibleUpdate(launcher))
        verify(exactly = 0) { appUpdateDataSource.startUpdateFlow(any(), any(), any()) }
    }

    @Test
    fun startFlexibleUpdate_noUpdate_returnsFalse() = runTest {
        assertFalse(repository.startFlexibleUpdate(launcher))
        verify(exactly = 0) { appUpdateDataSource.startUpdateFlow(any(), any(), any()) }
    }

    @Test
    fun startFlexibleUpdate_launcherThrows_returnsFalse() = runTest {
        val info = availableUpdate()
        givenAppUpdateInfo(info)
        every { appUpdateDataSource.startUpdateFlow(info, launcher, AppUpdateType.FLEXIBLE) } throws
            IllegalStateException("登録が外れた launcher")

        assertFalse(repository.startFlexibleUpdate(launcher))
    }

    @Test
    fun openStorePage_dataSourceThrows_doesNotThrow() {
        every { appUpdateDataSource.openStorePage() } throws IllegalStateException("開けない")

        repository.openStorePage()
    }

    @Test
    fun getUpdateDownloadedStream_installStatusDownloaded_emitsTrue() = runTest {
        every { appUpdateDataSource.getInstallStatusStream() } returns
            flowOf(InstallStatus.DOWNLOADING, InstallStatus.DOWNLOADED)

        assertEquals(listOf(false, true), repository.getUpdateDownloadedStream().take(2).toList())
    }

    @Test
    fun getUpdateDownloadedStream_listenerFails_stillEmitsFetchedState() = runTest {
        every { appUpdateDataSource.getInstallStatusStream() } returns flow { throw IllegalStateException("受け取れない") }
        givenAppUpdateInfo(noUpdate(installStatus = InstallStatus.DOWNLOADED))

        repository.fetchUpdateDownloaded()

        assertTrue(repository.getUpdateDownloadedStream().first())
    }

    @Test
    fun fetchUpdateDownloaded_notDownloaded_emitsFalse() = runTest {
        every { appUpdateDataSource.getInstallStatusStream() } returns emptyFlow()

        repository.fetchUpdateDownloaded()

        assertFalse(repository.getUpdateDownloadedStream().first())
    }

    @Test
    fun fetchUpdateDownloaded_playFails_emitsNothing() = runTest {
        every { appUpdateDataSource.getInstallStatusStream() } returns emptyFlow()
        coEvery { appUpdateDataSource.requestAppUpdateInfo() } throws IOException("Play がない")

        repository.fetchUpdateDownloaded()

        assertNull(withTimeoutOrNull(1_000) { repository.getUpdateDownloadedStream().first() })
    }

    @Test
    fun completeUpdate_playFails_doesNotThrow() = runTest {
        coEvery { appUpdateDataSource.completeUpdate() } throws IOException("Play がない")

        repository.completeUpdate()
    }
}
