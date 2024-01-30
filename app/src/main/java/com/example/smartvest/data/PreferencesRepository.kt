package com.example.smartvest.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/* TODO: Implement DataStore */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        const val TAG = "PreferencesRepository"
        val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
    }

    suspend fun saveSmsPreference(smsEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMS_ENABLED] = smsEnabled
        }
    }

    val smsEnabled = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())  // use default preferences
            }
            else {
                throw it
            }
        }
        .map { preferences ->
        preferences[SMS_ENABLED] ?: false
    }
}

