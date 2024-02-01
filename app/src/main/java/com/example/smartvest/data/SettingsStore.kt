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
        const val TAG = "SettingsStore"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        val SMS_ENABLE = booleanPreferencesKey("sms_enable")
        val LOCATION_ENABLE = booleanPreferencesKey("location_enable")
        val SMS_NUMBER = stringPreferencesKey("sms_number")
    }

    suspend fun setSmsEnable(smsEnable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMS_ENABLE] = smsEnable
        }
    }

    suspend fun setLocationEnable(locationEnable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_ENABLE] = locationEnable
        }
    }

    suspend fun setSmsNumber(smsNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[SMS_NUMBER] = smsNumber
        }
    }

    val smsEnable: Flow<Boolean> = context.dataStore.data
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
            preferences[SMS_ENABLE] ?: false
        }

    val locationEnable: Flow<Boolean> = context.dataStore.data
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
            preferences[LOCATION_ENABLE] ?: false
        }

    val smsNumber: Flow<String> = context.dataStore.data
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
            preferences[SMS_NUMBER] ?: ""
        }
}
