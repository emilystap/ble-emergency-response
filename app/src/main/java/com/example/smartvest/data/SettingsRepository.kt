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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

private const val TAG = "SettingsStore"
private val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
private val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
private val STORED_SMS_NUMBER = stringPreferencesKey("stored_sms_number")

class SettingsRepository private constructor(
    context: Context,
    scope: CoroutineScope
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "settings"
    )

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context, scope: CoroutineScope): SettingsRepository {
            // allow only one instance across all threads
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = SettingsRepository(
                    context.applicationContext,
                    scope
                )
                INSTANCE = instance

                // return instance if created during call, otherwise INSTANCE
                instance
            }
        }
    }

    suspend fun setSmsEnabled(smsEnabled: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[SMS_ENABLED] = smsEnabled
        }
    }

    suspend fun setLocationEnabled(locationEnabled: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_ENABLED] = locationEnabled
        }
    }

    suspend fun setStoredSmsNumber(storedSmsNumber: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[STORED_SMS_NUMBER] = storedSmsNumber
        }
    }

    val smsEnabled: StateFlow<Boolean> = context.dataStore.data.map { preferences ->
            preferences[SMS_ENABLED] ?: false  // assume setting is disabled
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val locationEnabled: StateFlow<Boolean> = context.dataStore.data.map { preferences ->
            preferences[LOCATION_ENABLED] ?: false  // assume setting is disabled
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val storedSmsNumber: StateFlow<String> = context.dataStore.data.map { preferences ->
            preferences[STORED_SMS_NUMBER] ?: ""
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
}