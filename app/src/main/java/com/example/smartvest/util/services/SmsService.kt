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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "SmsService"

class SmsService : Service() {
    /* TODO: Move to IntentService / Background Service? (separate thread) */
    /* TODO: Add timer notification/pop-up on BLE trigger */
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
        runBlocking(Dispatchers.IO) {  // blocking, since can't continue without this
            smsEnabled = settingsRepository.smsEnabled.first()
            locationEnabled = settingsRepository.locationEnabled.first()
            number = settingsRepository.storedSmsNumber.first()
        }

        if (!smsEnabled) {
            Log.w(TAG, "SMS is disabled")
            stopSelf()  // stop service if SMS is disabled
        }

        if (locationEnabled)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        smsManager = this.getSystemService(SmsManager::class.java) as SmsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting service")
        getSms()  // generates and sends SMS msg

        return START_REDELIVER_INTENT  // restart with previous intent if interrupted
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "Stopping service")
        job.cancel()  // cancel coroutines
    }

    private fun getSms() {
        var msg = "This is an automated message."  /* TODO: update msg, add username? */

        if (locationEnabled) {
            LocationUtil.getLocation(
                fusedLocationClient = fusedLocationClient,
                onSuccess = {
                    msg += " Location: ${LocationUtil.getMapUrl(it)}"
                    sendSms(msg)
                }
            )
        } else {
            sendSms(msg)
        }
    }

    private fun sendSms(msg: String = "") {
        Log.d(TAG, "Recipient: $number, Message: $msg")

        smsManager.sendTextMessage(
            number,
            null,
            msg,
            null,
            null
        )
        stopSelf()  // stop service after sending SMS
    }
}