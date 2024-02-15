package com.example.smartvest

import android.content.Intent
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.smartvest.ui.AppNav
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.util.PermissionUtil
import com.example.smartvest.util.services.BleService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* TODO: Clean up ALL Log statements, add documentation */

        val blePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            /* TODO: Handle permission denied */
            if (PermissionUtil.checkPermissionRequestResults(it))
                startForegroundService(Intent(this, BleService::class.java))
        }

        PermissionUtil.checkPermissions(
            blePermissionLauncher,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        )

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