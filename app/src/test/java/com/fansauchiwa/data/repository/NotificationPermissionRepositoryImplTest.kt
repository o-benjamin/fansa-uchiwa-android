package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.NotificationPermissionDataSource
import com.fansauchiwa.data.infra.SettingsDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionRepositoryImplTest {

    private val notificationPermissionDataSource: NotificationPermissionDataSource = mockk()
    private val settingsDataSource: SettingsDataSource = mockk(relaxed = true)
    private val repository =
        NotificationPermissionRepositoryImpl(notificationPermissionDataSource, settingsDataSource)

    @Test
    fun shouldRequestAfterSave_permissionNeededAndNotRequestedYet_returnsTrue() = runTest {
        every { notificationPermissionDataSource.isPermissionRequestNeeded() } returns true
        every { settingsDataSource.getHasRequestedNotificationPermissionStream() } returns flowOf(false)

        assertTrue(repository.shouldRequestAfterSave())
    }

    @Test
    fun shouldRequestAfterSave_alreadyRequested_returnsFalse() = runTest {
        every { notificationPermissionDataSource.isPermissionRequestNeeded() } returns true
        every { settingsDataSource.getHasRequestedNotificationPermissionStream() } returns flowOf(true)

        assertFalse(repository.shouldRequestAfterSave())
    }

    @Test
    fun shouldRequestAfterSave_permissionNotNeeded_returnsFalse() = runTest {
        // Android 12 以下、または許可済み（イベント予定のリマインドで許可した人など）
        every { notificationPermissionDataSource.isPermissionRequestNeeded() } returns false
        every { settingsDataSource.getHasRequestedNotificationPermissionStream() } returns flowOf(false)

        assertFalse(repository.shouldRequestAfterSave())
    }

    @Test
    fun markRequestedAfterSave_called_savesRequested() = runTest {
        repository.markRequestedAfterSave()

        coVerify(exactly = 1) { settingsDataSource.setHasRequestedNotificationPermission() }
    }
}
