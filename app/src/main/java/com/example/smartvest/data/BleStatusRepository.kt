package com.example.smartvest.data

import android.content.Context
import android.content.IntentFilter
import com.example.smartvest.util.receivers.BleStatusReceiver
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.StateFlow

object BleStatusRepository {
    private val receiver = BleStatusReceiver()
    private val intentFilter = IntentFilter(BleService.PKG_CLASS_NAME)

    fun getStatus(): StateFlow<BleService.Status> {
        return receiver.getStatus()
    }

    fun gattConnected(): StateFlow<Boolean> {
        return receiver.gattConnected()
    }

    fun registerReceiver(context: Context) {
        context.registerReceiver(
            receiver,
            intentFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }
}

