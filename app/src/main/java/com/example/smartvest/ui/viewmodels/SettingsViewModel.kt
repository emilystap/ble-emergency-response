package com.example.smartvest.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.smartvest.data.SettingsStore
import com.example.smartvest.ui.states.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private lateinit var dataStore: SettingsStore

    private var locationEnabled: Boolean = false
    private var smsEnabled: Boolean = false
    private lateinit var storedSmsNumber: String
    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
}