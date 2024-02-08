package com.example.smartvest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.smartvest.util.services.BleService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val bleNotificationChannel = NotificationChannel(
            BleService.NOTIFICATION_CHANNEL_ID,
            BleService.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
            as NotificationManager
        notificationManager.createNotificationChannel(bleNotificationChannel)
    }
}