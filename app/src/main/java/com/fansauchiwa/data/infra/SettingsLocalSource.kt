package com.fansauchiwa.data.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsLocalSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsDataSource {

    override val isHapticFeedbackEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK_ENABLED] ?: DEFAULT_HAPTIC_FEEDBACK_ENABLED
        }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    companion object {
        private val KEY_HAPTIC_FEEDBACK_ENABLED =
            booleanPreferencesKey("haptic_feedback_enabled")
        private const val DEFAULT_HAPTIC_FEEDBACK_ENABLED = true
    }
}

