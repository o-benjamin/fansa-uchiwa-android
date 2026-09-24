package com.fansauchiwa.data.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class InAppReviewHistoryLocalSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : InAppReviewHistoryDataSource {

    override fun getSaveSuccessCountStream(): Flow<Int> =
        getPreferencesStream().map { preferences -> preferences[KEY_SAVE_SUCCESS_COUNT] ?: 0 }

    override suspend fun incrementSaveSuccessCount() {
        dataStore.edit { preferences ->
            preferences[KEY_SAVE_SUCCESS_COUNT] = (preferences[KEY_SAVE_SUCCESS_COUNT] ?: 0) + 1
        }
    }

    override fun getLastRequestedAtMillisStream(): Flow<Long?> =
        getPreferencesStream().map { preferences -> preferences[KEY_LAST_REQUESTED_AT_MILLIS] }

    override suspend fun setLastRequestedAtMillis(millis: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_REQUESTED_AT_MILLIS] = millis
        }
    }

    private fun getPreferencesStream(): Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    companion object {
        private val KEY_SAVE_SUCCESS_COUNT = intPreferencesKey("in_app_review_save_success_count")
        private val KEY_LAST_REQUESTED_AT_MILLIS =
            longPreferencesKey("in_app_review_last_requested_at_millis")
    }
}
