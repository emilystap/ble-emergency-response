package com.example.smartvest.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.smartvest.data.SettingsStore
import com.example.smartvest.ui.states.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {
    private var locationEnabled = settingsStore.locationEnabled
    private var smsEnabled = settingsStore.smsEnabled
    private var storedSmsNumber = settingsStore.storedSmsNumber

    private val _uiState = MutableStateFlow(SettingsUiState(
        locationEnabled = locationEnabled,
        smsEnabled = smsEnabled,
        storedSmsNumber = storedSmsNumber
    ))

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateLocationEnabled(enabled: Boolean) {
        //settingsStore.setLocationEnabled(enabled)
    }

    fun updateSmsEnabled(enabled: Boolean) {
        //settingsStore.setSmsEnabled(enabled)
    }

    fun updateStoredSmsNumber(number: String) {
        //settingsStore.setStoredSmsNumber(number)
    }
}