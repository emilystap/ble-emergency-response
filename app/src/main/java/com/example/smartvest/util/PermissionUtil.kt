package com.example.smartvest.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

private const val TAG = "PermissionUtil"

object PermissionUtil {
    fun checkPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) {
        Log.d(TAG, "Checking permissions: ${permissions.contentToString()}")
        launcher.launch(permissions)
    }

    fun checkPermissionsBackground(context: Context, permissions: Array<String>): Boolean {
        Log.d(TAG, "Checking permissions: ${permissions.contentToString()}")
        for (permission in permissions) {
            when (PackageManager.PERMISSION_DENIED) {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) -> {
                    Log.w(TAG, "Permission denied: $permission")
                    return false
                }
            }
        }
        Log.d(TAG, "All permissions granted")
        return true
    }

    fun checkPermissionRequestResults(results: Map<String, Boolean>): Boolean {
        Log.d(TAG, "Checking permission request results: $results")
        if (results.all { permission -> permission.value }) {
            Log.d(TAG, "All permissions granted")
            return true
        }

        Log.d(TAG, "One or more permissions denied")
        return false
    }
}