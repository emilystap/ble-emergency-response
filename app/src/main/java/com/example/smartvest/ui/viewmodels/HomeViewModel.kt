package com.example.smartvest.ui.viewmodels

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.example.smartvest.data.BleStatusRepository
import com.example.smartvest.ui.states.HomeUiState
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /* TODO: Update uiState when ble status changes */

    override fun onCleared() {
        super.onCleared()
        BleStatusRepository.unregisterReceiver(application)
    }

    fun refreshBleService() {
        application.startService(Intent(application, BleService::class.java))
    }

    init {
        _uiState.value = HomeUiState(connected = false)  // assume not connected at app start
        BleStatusRepository.registerReceiver(application)
    }
}