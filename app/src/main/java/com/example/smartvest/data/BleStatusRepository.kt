package com.example.smartvest.data

import android.content.Context
import android.content.IntentFilter
import com.example.smartvest.util.receivers.BleStatusReceiver
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.StateFlow

class BleStatusRepository {
    private val receiver = BleStatusReceiver()
    private val intentFilter = IntentFilter(BleService.PKG_CLASS_NAME)

    companion object {
        @Volatile
        private var INSTANCE: BleStatusRepository? = null

        fun getInstance(): BleStatusRepository {
            // allow only one instance across all threads
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = BleStatusRepository()
                INSTANCE = instance

                // return instance if created during call, otherwise INSTANCE
                instance
            }
        }
    }

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

