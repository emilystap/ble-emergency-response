package com.example.smartvest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvest.data.SettingsRepository
import com.example.smartvest.ui.states.SettingsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository: SettingsRepository = SettingsRepository
        .getInstance(application)

    val uiState = combine(
            settingsRepository.locationEnabled,
            settingsRepository.smsEnabled,
            settingsRepository.storedSmsNumber,
        ) { locationEnabled: Boolean, smsEnabled: Boolean, storedSmsNumber: String ->
        SettingsUiState(
            locationEnabled = locationEnabled,
            smsEnabled = smsEnabled,
            storedSmsNumber = storedSmsNumber
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),  /* TODO: Figure this param out */
        initialValue = SettingsUiState(
            locationEnabled = false,
            smsEnabled = false,
            storedSmsNumber = ""
        )
    )

    fun setLocationEnabled(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLocationEnabled(enable, getApplication())
        }
    }

    fun setSmsEnabled(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSmsEnabled(enable, getApplication())
        }
    }

    fun setStoredSmsNumber(number: String) {
        viewModelScope.launch {
            settingsRepository.setStoredSmsNumber(number, getApplication())
        }
    }
}