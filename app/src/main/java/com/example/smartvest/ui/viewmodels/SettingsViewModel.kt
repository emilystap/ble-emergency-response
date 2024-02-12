package com.example.smartvest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvest.data.SettingsRepository
import com.example.smartvest.ui.states.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val settingsRepository: SettingsRepository = SettingsRepository(application)

    fun setLocationEnabled(enable: Boolean) {
        viewModelScope.launch { settingsRepository.setLocationEnabled(enable) }
    }

    fun setSmsEnabled(enable: Boolean) {
        viewModelScope.launch { settingsRepository.setSmsEnabled(enable) }
    }

    fun setStoredSmsNumber(number: String) {
        viewModelScope.launch { settingsRepository.setStoredSmsNumber(number) }
    }

    init {
        _uiState.value = SettingsUiState(
            /* TODO: Figure this out */
//            locationEnabled = settingsRepository.locationEnabled,
//            smsEnabled = settingsRepository.smsEnabled,
//            storedSmsNumber = settingsRepository.storedSmsNumber
        )
    }
}