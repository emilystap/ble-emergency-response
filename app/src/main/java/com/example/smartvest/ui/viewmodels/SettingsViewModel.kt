package com.example.smartvest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvest.data.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
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
}