package com.example.smartvest.ui.states

import kotlinx.coroutines.flow.Flow

data class SettingsUiState(
    val locationEnabled: Flow<Boolean>,
    val smsEnabled: Flow<Boolean>,
    val storedSmsNumber: Flow<String>
)