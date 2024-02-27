package com.example.smartvest.data

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import com.example.smartvest.util.receivers.BleStatusReceiver
import com.example.smartvest.util.services.BleService
import kotlinx.coroutines.flow.StateFlow

class BleStatusRepository private constructor() {
    private val receiver = BleStatusReceiver()

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

    fun registerReceiver(application: Application) {
        val intentFilter = IntentFilter(BleStatusReceiver.ACTION_UPDATE_STATUS)

        application.registerReceiver(
            receiver,
            intentFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregisterReceiver(application: Application) {
        application.unregisterReceiver(receiver)
    }
}

