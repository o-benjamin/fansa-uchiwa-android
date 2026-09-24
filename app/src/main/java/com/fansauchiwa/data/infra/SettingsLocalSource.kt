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

    override fun getHapticFeedbackEnabledStream(): Flow<Boolean> = dataStore.data
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

    override fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HAS_SEEN_EDIT_COMPLETION_TOOLTIP]
                ?: DEFAULT_HAS_SEEN_EDIT_COMPLETION_TOOLTIP
        }

    override suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_EDIT_COMPLETION_TOOLTIP] = hasSeen
        }
    }

    override fun getHasSeenApologyDialogStream(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HAS_SEEN_APOLOGY_DIALOG] ?: DEFAULT_HAS_SEEN_APOLOGY_DIALOG
        }

    override suspend fun setHasSeenApologyDialog(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_APOLOGY_DIALOG] = hasSeen
        }
    }

    companion object {
        private val KEY_HAPTIC_FEEDBACK_ENABLED =
            booleanPreferencesKey("haptic_feedback_enabled")
        private val KEY_HAS_SEEN_EDIT_COMPLETION_TOOLTIP =
            booleanPreferencesKey("has_seen_edit_completion_tooltip")
        private val KEY_HAS_SEEN_APOLOGY_DIALOG =
            booleanPreferencesKey("has_seen_apology_dialog")
        private const val DEFAULT_HAPTIC_FEEDBACK_ENABLED = true
        private const val DEFAULT_HAS_SEEN_EDIT_COMPLETION_TOOLTIP = false
        private const val DEFAULT_HAS_SEEN_APOLOGY_DIALOG = false
    }
}
