package com.example.smartvest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartvest.ui.AppNav
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.util.services.BleService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, BleService::class.java))

        setContent {
            SmartVestTheme {
                AppNav()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BleService::class.java))
    }
}