package com.example.smartvest.ui.states

data class SettingsUiState(
    val locationEnabled: Boolean = false,
    val smsEnabled: Boolean = false,
    val storedSmsNumber: String = ""
)