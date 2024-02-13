package com.example.smartvest.util.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import com.example.smartvest.data.SettingsRepository

private const val TAG = "SmsService"

class SmsService : Service() {
    private lateinit var smsManager: SmsManager
    //private lateinit var settingsRepository: SettingsRepository
    companion object {
        val permissions = arrayOf(Manifest.permission.SEND_SMS)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        smsManager = this.getSystemService(SmsManager::class.java) as SmsManager
        //settingsRepository = SettingsRepository.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting service")
        sendSms()

        return START_REDELIVER_INTENT  // restart with previous intent if interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Stopping service")
    }

    private fun sendSms() {
        Log.d(TAG, "Sending SMS")
        stopSelf()
    }
}