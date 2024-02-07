package com.example.smartvest.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsStore(
    private val context: Context
) {
    private companion object {
        const val TAG = "SettingsStore"  // used for logging
        val Context.dataStore: DataStore<Preferences> by
            preferencesDataStore(name = "settings")

        val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
        val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        val STORED_SMS_NUMBER = stringPreferencesKey("stored_sms_number")
    }

    suspend fun setSmsEnable(smsEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMS_ENABLED] = smsEnabled
        }
    }

    suspend fun setLocationEnable(locationEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_ENABLED] = locationEnabled
        }
    }

    suspend fun setSmsNumber(storedSmsNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[STORED_SMS_NUMBER] = storedSmsNumber
        }
    }

    val smsEnabled: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())  // use default preference
            }
            else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SMS_ENABLED] ?: false  // assume setting is disabled
        }

    val locationEnabled: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())  // use default preference
            }
            else {
                throw it
            }
        }
        .map { preferences ->
            preferences[LOCATION_ENABLED] ?: false  // assume setting is disabled
        }

    val storedSmsNumber: Flow<String> = context.dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())  // use default preference
            }
            else {
                throw it
            }
        }
        .map { preferences ->
            preferences[STORED_SMS_NUMBER] ?: ""
        }
}