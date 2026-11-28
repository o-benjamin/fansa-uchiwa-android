package com.fansauchiwa.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.fansauchiwa.data.infra.SettingsLocalSource
import com.fansauchiwa.data.repository.SettingsRepositoryImpl
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tmpFolder.newFile("settings.preferences_pb") }
        )
        localSource = SettingsLocalSource(dataStore)
        repository = SettingsRepositoryImpl(localSource)
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
    fun fetchDailyPromptDismissedDate_neverDismissed_emitsNull() = runTest {
        repository.fetchDailyPromptDismissedDate()

        assertNull(repository.getDailyPromptDismissedDateStream().first())
    }

    @Test
    fun setDailyPromptDismissedDate_emitsAndKeepsDate() = runTest {
        val date = LocalDate.of(2026, 11, 4)

        repository.setDailyPromptDismissedDate(date)
        assertEquals(date, repository.getDailyPromptDismissedDateStream().first())

        // 作り直した Repository（アプリを開き直したとき）でも読める
        val reopened = SettingsRepositoryImpl(localSource)
        reopened.fetchDailyPromptDismissedDate()
        assertEquals(date, reopened.getDailyPromptDismissedDateStream().first())
    }
}
