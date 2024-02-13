package com.example.smartvest.ui.viewmodels

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvest.data.BleStatusRepository
import com.example.smartvest.ui.states.HomeUiState
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {
    val uiState = BleStatusRepository.gattConnected().map { gattConnected ->
        HomeUiState(
            connected = gattConnected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),  /* TODO: Figure this param out */
        initialValue = HomeUiState(connected = false)
    )

    override fun onCleared() {
        super.onCleared()
        BleStatusRepository.unregisterReceiver(application)
    }

    fun refreshBleService() {
        application.startService(Intent(application, BleService::class.java))
    }

    init {
        BleStatusRepository.registerReceiver(application)
    }
}