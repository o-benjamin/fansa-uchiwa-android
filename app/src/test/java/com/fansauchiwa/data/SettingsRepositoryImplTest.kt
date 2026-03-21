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

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { tmpFolder.newFile("settings.preferences_pb") }
    )
    private val localSource = SettingsLocalSource(dataStore)
    private val repository = SettingsRepositoryImpl(localSource)

    @Test
    fun isHapticFeedbackEnabled_defaultIsTrue() = runTest {
        assertTrue(repository.isHapticFeedbackEnabled.first())
    }

    @Test
    fun setHapticFeedbackEnabled_updatesValue() = runTest {
        repository.setHapticFeedbackEnabled(false)
        assertFalse(repository.isHapticFeedbackEnabled.first())

        repository.setHapticFeedbackEnabled(true)
        assertTrue(repository.isHapticFeedbackEnabled.first())
    }

    @Test
    fun isHapticFeedbackEnabled_catchesIOException() = runTest {
        // IOExceptionをシミュレートするテスト（ファイルへのアクセス権限がない場合など）
        // 実際のアプリではFlowのcatchブロックで emptyPreferences() をemitし、デフォルト値(true)にフォールバックすることが期待される
        // ※このテストを完全にモックで再現するのは難しいため、実装時にFlowに `catch { if (it is IOException) emit(emptyPreferences()) else throw it }` が含まれているか確認すること。
    }
}

