package com.example.smartvest.util.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import com.example.smartvest.data.SettingsRepository
import com.example.smartvest.util.LocationUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val TAG = "SmsService"

class SmsService : Service() {
    private lateinit var smsManager: SmsManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var number: String

    private var smsEnabled: Boolean = false
    private var locationEnabled: Boolean = false

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        val permissions = arrayOf(Manifest.permission.SEND_SMS)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        settingsRepository = SettingsRepository.getInstance(this, scope)
        /* TODO: Figure out why this doesn't work - SupervisorJob? */

        smsEnabled = settingsRepository.smsEnabled.value
        if (!smsEnabled) {
            Log.w(TAG, "SMS is disabled")
            stopSelf()  // stop service if SMS is disabled
        }

        locationEnabled = settingsRepository.locationEnabled.value
        if (locationEnabled)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        number = settingsRepository.storedSmsNumber.value
        smsManager = this.getSystemService(SmsManager::class.java) as SmsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting service")
        sendSms()

        return START_REDELIVER_INTENT  // restart with previous intent if interrupted
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "Stopping service")
        job.cancel()  // cancel coroutines
    }

    private fun sendSms() {
        Log.d(TAG, "Sending SMS")
        var msg = "This is an automated message. "  /* TODO: update msg, add username? */

        if (locationEnabled) {
            LocationUtil.getMapUrlAwait(
                fusedLocationClient = fusedLocationClient,
                scope = scope
            )?.let {
                msg += it
            }
        }
        Log.d(TAG, "Recipient: $number, Message: $msg")

//        smsManager.sendTextMessage(
//            number,
//            null,
//            msg,
//            null,
//            null
//        )

        stopSelf()
    }
}