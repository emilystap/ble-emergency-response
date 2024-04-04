package com.example.smartvest.util.services

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartvest.R
import com.example.smartvest.data.SettingsRepository
import com.example.smartvest.util.LocationUtil
import com.example.smartvest.util.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "SmsService"
private const val CANCELLATION_TIMEOUT = 30000L  // 30 seconds

class SmsService : Service() {
    private var serviceHandler: Handler? = null
    private var sendSms: Boolean = false

    private lateinit var smsManager: SmsManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var number: String

    private var smsEnabled: Boolean = false
    private var locationEnabled: Boolean = false

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val SERVICE_ID = 2
        const val ACTION_STOP_SERVICE = "com.example.smartvest.util.services.SmsService.STOP_SERVICE"
        const val NOTIFICATION_TITLE = "SMS Service"
        const val NOTIFICATION_CHANNEL_ID = "services.SmsService"
        const val NOTIFICATION_CHANNEL_NAME = "SmsService"

        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SEND_SMS
        )
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
        smsManager = this.getSystemService(SmsManager::class.java) as SmsManager
        serviceHandler = Handler(Looper.getMainLooper())

        setInitialNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.w(TAG,"Cancelling emergency response")
            sendSms = false  // extra check, since removeCallbacks can't stop in-progress tasks
            serviceHandler?.removeCallbacksAndMessages(null)
            stopSelf()
        }
        else {
            start()
        }
        return START_REDELIVER_INTENT  // restart with previous intent if interrupted
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "Stopping service")
        job.cancel()  // cancel coroutines
    }

    private fun start() {
        Log.d(TAG, "Starting service")

        if (!PermissionUtil.checkPermissionsBackground(this, permissions)) {
            Log.w(TAG, "Missing required permissions")
            stopSelf()
        }

        if (sendSms) {
            Log.d(TAG, "Received SMS trigger while already sending SMS")
            return
        }

        sendSms = true
        serviceHandler?.postDelayed({
            stopNotificationTimer()
            getSms()
        }, CANCELLATION_TIMEOUT)
    }

    private fun setInitialNotification() {
        val stopSelfIntent = Intent(this, SmsService::class.java)
        stopSelfIntent.action = ACTION_STOP_SERVICE
        val pendingStopSelfIntent = PendingIntent.getForegroundService(
            this,
            0,
            stopSelfIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText("Preparing to send SMS...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(System.currentTimeMillis() + CANCELLATION_TIMEOUT)
            .addAction(R.drawable.ic_launcher_foreground, "Cancel", pendingStopSelfIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        startForeground(SERVICE_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
    }

    private fun stopNotificationTimer() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText("Sending SMS...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
                as NotificationManager
        notificationManager.notify(SERVICE_ID, notification)
    }

    private fun getSms() {
        if (!sendSms) {
            Log.d(TAG, "Send SMS canceled")
            return
        }
        var msg = "This is an automated message."

        if (locationEnabled) {
            LocationUtil.getCurrentLocation(
                context = this,
                onSuccess = {
                    if (it != null)
                        msg += " Location: ${LocationUtil.getMapUrl(it)}"
                    else
                        Log.w(TAG, "Location is null")
                    sendSms(msg)
                },
                onFailure = {
                    Log.e(TAG, "Failed to get location", it)
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