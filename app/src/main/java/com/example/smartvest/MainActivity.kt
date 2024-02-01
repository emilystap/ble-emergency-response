package com.example.smartvest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.util.PermissionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionHandler = PermissionHandler(this)

        setContent {
            SmartVestTheme {
                AppNav(permissionHandler)
            }
        }
    }
}
