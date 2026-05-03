package com.fansauchiwa.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.fansauchiwa.data.infra.SettingsLocalSource
import com.fansauchiwa.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryImplTest {
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { tmpFolder.newFile("settings.preferences_pb") }
        )
    }
    private val localSource by lazy { SettingsLocalSource(dataStore) }
    private val repository by lazy { SettingsRepositoryImpl(localSource) }

    @Test
    fun fetchHapticFeedbackEnabled_defaultIsTrue() = runTest {
        repository.fetchHapticFeedbackEnabled()

        assertTrue(repository.getHapticFeedbackEnabledStream().first())
    }

    @Test
    fun setHapticFeedbackEnabled_updatesValue() = runTest {
        repository.setHapticFeedbackEnabled(false)
        assertFalse(repository.getHapticFeedbackEnabledStream().first())

        repository.setHapticFeedbackEnabled(true)
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

        assertTrue(repository.getHasSeenEditCompletionTooltipStream().first())
    }
}
