package com.fansauchiwa.data.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
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

    override fun getHapticFeedbackEnabledStream(): Flow<Boolean> =
        dataStore.data
            .recoverPreferences()
            .map(SettingsPreferencesSchema::readHapticFeedbackEnabled)

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            SettingsPreferencesSchema.writeHapticFeedbackEnabled(
                preferences = preferences,
                enabled = enabled
            )
        }
    }
}

private fun Flow<Preferences>.recoverPreferences(): Flow<Preferences> =
    catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

private object SettingsPreferencesSchema {
    private val keyHapticFeedbackEnabled = booleanPreferencesKey("haptic_feedback_enabled")
    private const val defaultHapticFeedbackEnabled = true

    fun readHapticFeedbackEnabled(preferences: Preferences): Boolean =
        preferences[keyHapticFeedbackEnabled] ?: defaultHapticFeedbackEnabled

    fun writeHapticFeedbackEnabled(
        preferences: MutablePreferences,
        enabled: Boolean
    ) {
        preferences[keyHapticFeedbackEnabled] = enabled
    }
}
