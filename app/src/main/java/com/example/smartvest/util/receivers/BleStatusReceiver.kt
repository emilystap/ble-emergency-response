package com.example.smartvest.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.MutableStateFlow

class BleStatusReceiver : BroadcastReceiver() {
    private val status: MutableStateFlow<BleService.Status> = MutableStateFlow(
        BleService.Status.UNKNOWN
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getStringExtra("status")?.let {
            status.value = BleService.Status.valueOf(it)
        }
    }

    fun getStatus(): MutableStateFlow<BleService.Status> {
        return status
    }
}