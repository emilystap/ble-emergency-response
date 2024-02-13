package com.example.smartvest.util.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

private const val TAG = "SmsService"

class SmsService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting service")
        sendSms(intent)

        return START_REDELIVER_INTENT  // restart with previous intent if interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Stopping service")
    }

    private fun sendSms(intent: Intent?) {
        Log.d(TAG, "Sending SMS")
        stopSelf()
    }
}