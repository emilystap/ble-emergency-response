package com.example.smartvest.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "BleBroadcastReceiver"

class BleBroadcastReceiver : BroadcastReceiver() {
    private val _result = MutableStateFlow(BleService.BroadcastMsg())
    val result = _result.asStateFlow()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ${intent?.action}")
        _result.value = BleService.BroadcastMsg(intent)
    }
}