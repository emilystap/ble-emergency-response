package com.example.smartvest.ui.viewmodels

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvest.data.BleStatusRepository
import com.example.smartvest.ui.states.HomeUiState
import com.example.smartvest.util.services.BleService
import com.example.smartvest.util.services.SmsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {
    private val bleStatusRepository = BleStatusRepository.getInstance()
    val uiState = bleStatusRepository.gattConnected().map { gattConnected ->
        HomeUiState(
            connected = gattConnected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(connected = false)
    )

    override fun onCleared() {
        super.onCleared()
        bleStatusRepository.unregisterReceiver(application)
    }

    fun refreshBleService() {
        application.startForegroundService(Intent(application, BleService::class.java))
    }

    fun startSmsService() {
        application.startForegroundService(Intent(application, SmsService::class.java))
    }

    init {
        bleStatusRepository.registerReceiver(application)
    }
}