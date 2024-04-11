package com.example.smartvest.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "BleStatusReceiver"

class BleStatusReceiver : BroadcastReceiver() {
    private val status: MutableStateFlow<BleService.Status> = MutableStateFlow(
        BleService.Status.UNKNOWN
    )

    private val gattConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: $intent")
        intent.getStringExtra("status")?.let {
            status.value = BleService.Status.valueOf(it)

            if (status.value == BleService.Status.GATT_CONNECTED)
                gattConnected.value = true
            else if (status.value == BleService.Status.GATT_DISCONNECTED)
                gattConnected.value = false
        }
    }

    fun getStatus(): MutableStateFlow<BleService.Status> {
        return status
    }

    fun gattConnected(): MutableStateFlow<Boolean> {
        return gattConnected
    }
}