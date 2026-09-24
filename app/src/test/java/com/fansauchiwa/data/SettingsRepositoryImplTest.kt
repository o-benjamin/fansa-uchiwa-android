package com.fansauchiwa.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.fansauchiwa.data.infra.AppInstallDataSource
import com.fansauchiwa.data.infra.SettingsLocalSource
import com.fansauchiwa.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryImplTest {
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var localSource: SettingsLocalSource
    private lateinit var repository: SettingsRepositoryImpl
    // FakeAppInstallDataSource が返す値
    private var fakeIsFreshInstall = false

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tmpFolder.newFile("settings.preferences_pb") }
        )
        localSource = SettingsLocalSource(dataStore)
        repository = SettingsRepositoryImpl(localSource, FakeAppInstallDataSource())
    }

    @Test
    fun fetchHapticFeedbackEnabled_defaultIsTrue() = runTest {
        repository.fetchHapticFeedbackEnabled()

        assertTrue(repository.getHapticFeedbackEnabledStream().first())
    }

    @Test
    fun setHapticFeedbackEnabled_updatesValue() = runTest {
        repository.setHapticFeedbackEnabled(false)
        repository.fetchHapticFeedbackEnabled()
        assertFalse(repository.getHapticFeedbackEnabledStream().first())

        repository.setHapticFeedbackEnabled(true)
        repository.fetchHapticFeedbackEnabled()
        assertTrue(repository.getHapticFeedbackEnabledStream().first())
    }

    @Test
    fun fetchHasSeenEditCompletionTooltip_defaultIsFalse() = runTest {
        repository.fetchHasSeenEditCompletionTooltip()

        assertFalse(repository.getHasSeenEditCompletionTooltipStream().first())
    }

    @Test
    fun setHasSeenEditCompletionTooltip_updatesValue() = runTest {
        repository.setHasSeenEditCompletionTooltip(true)
        repository.fetchHasSeenEditCompletionTooltip()

        assertTrue(repository.getHasSeenEditCompletionTooltipStream().first())
    }

    @Test
    fun fetchHasSeenApologyDialog_updatedInstallNotSeen_returnsFalse() = runTest {
        fakeIsFreshInstall = false

        repository.fetchHasSeenApologyDialog()

        assertFalse(repository.getHasSeenApologyDialogStream().first())
    }

    @Test
    fun fetchHasSeenApologyDialog_freshInstall_returnsTrue() = runTest {
        fakeIsFreshInstall = true

        repository.fetchHasSeenApologyDialog()

        assertTrue(repository.getHasSeenApologyDialogStream().first())
    }

    @Test
    fun fetchHasSeenApologyDialog_freshInstallThenUpdated_staysTrue() = runTest {
        fakeIsFreshInstall = true
        repository.fetchHasSeenApologyDialog()

        fakeIsFreshInstall = false
        repository.fetchHasSeenApologyDialog()

        assertTrue(repository.getHasSeenApologyDialogStream().first())
    }

    @Test
    fun fetchHasSeenApologyDialog_updatedInstallAlreadySeen_returnsTrue() = runTest {
        fakeIsFreshInstall = false
        repository.setHasSeenApologyDialog(true)

        repository.fetchHasSeenApologyDialog()

        assertTrue(repository.getHasSeenApologyDialogStream().first())
    }

    // region フォント計測（#242）

    @Test
    fun getLastSavedFontName_notYetSaved_returnsNull() = runTest {
        assertEquals(null, repository.getLastSavedFontName())
    }

    @Test
    fun setLastSavedFontName_thenGet_returnsSavedValue() = runTest {
        repository.setLastSavedFontName("KEI_FONT")

        assertEquals("KEI_FONT", repository.getLastSavedFontName())
    }

    @Test
    fun setLastSavedFontName_calledTwice_overwritesPreviousValue() = runTest {
        repository.setLastSavedFontName("KEI_FONT")
        repository.setLastSavedFontName("ZEN_MARU_GOTHIC")

        assertEquals("ZEN_MARU_GOTHIC", repository.getLastSavedFontName())
    }

    // endregion

    private inner class FakeAppInstallDataSource : AppInstallDataSource {
        override fun getIsFreshInstallStream(): Flow<Boolean> = flowOf(fakeIsFreshInstall)

        override fun getFirstInstallTimeMillisStream(): Flow<Long?> = flowOf(null)
    }
}
