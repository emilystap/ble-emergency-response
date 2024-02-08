package com.example.smartvest.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionsHandler {
    /* TODO: Implement, move permission checks to viewmodel? */
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(context: Context, permissions: Array<String>): Boolean {
        /* TODO: Implement */
        return false
    }

    fun requestPermission(context: Context, permission: String): Boolean {
        /* TODO: Implement */
        return false
    }
}