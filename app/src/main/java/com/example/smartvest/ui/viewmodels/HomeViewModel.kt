package com.example.smartvest.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.smartvest.ui.states.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/* TODO: Figure out if this needs to be application aware */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = HomeUiState(connected = false)  // assume not connected at app start
    }
}

