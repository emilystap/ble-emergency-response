package com.example.smartvest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.smartvest.util.services.BleService
import com.example.smartvest.util.services.SmsService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationChannels = listOf(
            NotificationChannel(
                BleService.NOTIFICATION_CHANNEL_ID,
                BleService.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ),
            NotificationChannel(
                SmsService.NOTIFICATION_CHANNEL_ID,
                SmsService.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
            as NotificationManager
        notificationManager.createNotificationChannels(notificationChannels)
    }
}