package com.example.smartvest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartvest.data.BleStatusRepository
import com.example.smartvest.ui.states.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        BleStatusRepository.unregisterReceiver(application)
    }

    init {
        _uiState.value = HomeUiState(connected = false)  // assume not connected at app start
        BleStatusRepository.registerReceiver(application)
    }
}