package com.example.smartvest.util

import android.Manifest
import android.content.Context

class PermissionHandler(private var context: Context) {
    companion object {
        const val PERMISSION_REQUEST_ALL = 1000
    }

    private var pm = context.packageManager

    val telephonyAvailable = pm.hasSystemFeature(
        android.content.pm.PackageManager.FEATURE_TELEPHONY
    )

    val bleAvailable = pm.hasSystemFeature(
        android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE
    )

    fun hasSmsPermission(): Boolean {
        return telephonyAvailable && context.checkSelfPermission(Manifest.permission.SEND_SMS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun hasBlePermission(): Boolean {
        return bleAvailable && context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(): Boolean {
        return hasSmsPermission() && hasBlePermission() && hasLocationPermission()
    }

    fun getSmsPermission() {
        if (hasSmsPermission()) return
    }

    fun getBlePermission() {
        if (hasBlePermission()) return
    }

    fun getAllPermissions() {
        if (hasAllPermissions()) return
    }
}