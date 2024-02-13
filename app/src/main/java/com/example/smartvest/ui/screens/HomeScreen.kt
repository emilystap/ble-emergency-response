package com.example.smartvest.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartvest.R
import com.example.smartvest.ui.TopAppBar
import com.example.smartvest.ui.states.HomeUiState
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.ui.viewmodels.HomeViewModel
import com.example.smartvest.util.PermissionUtil
import com.example.smartvest.util.services.BleService
import com.example.smartvest.util.services.SmsService

private const val TAG = "HomeScreen"
/* TODO: Switch to Hilt for dependency injection */

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navController: NavHostController,
    title: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    SmartVestTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(navController, title, canReturn = false) },
            floatingActionButton = {
                SendFab(viewModel = viewModel)  // floating action button for manually sending SMS
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ConnectionStatus(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

/* TODO: add buttons to start tracking, refresh connection */
@Composable
private fun ConnectionStatus(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    uiState: HomeUiState
) {
    val blePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (PermissionUtil.checkPermissionRequestResults(it))
            viewModel.refreshBleService()
        else
            Log.w(TAG, "Permission check returned false")
    }
    val connected = uiState.connected

    Row(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Status: "
        )
        Text(
            text = if (connected) "Connected" else "Disconnected",
            color = (
                    if (connected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                    ),
            modifier = modifier.weight(1f)
        )
        FilledTonalButton(
            onClick = {
                PermissionUtil.checkPermissions(
                    blePermissionLauncher,
                    BleService.permissions
                )
            }
        ) {
            Text("Refresh")
        }
    }
}

@Composable
private fun SendFab(viewModel: HomeViewModel) {
    var openSMSAlertDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (PermissionUtil.checkPermissionRequestResults(it))
            openSMSAlertDialog = true
    }

    FloatingActionButton(
        onClick = {
            PermissionUtil.checkPermissions(
                permissionLauncher,
                SmsService.permissions
            )
        },
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
    ) {
        Icon(Icons.AutoMirrored.Filled.Send, "Send SMS")
    }

    if (openSMSAlertDialog) {
        SMSAlertDialog(
            onDismiss = { openSMSAlertDialog = false },
            onConfirm = {
                openSMSAlertDialog = false
                viewModel.startSmsService()
            }
        )
    }
}

@Composable
private fun SMSAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning") },
        title = { Text(text = stringResource(id = R.string.sms_alert_dialog_title)) },  /* TODO: add check for location disabled */
        text = { Text(text = stringResource(id = R.string.sms_alert_dialog_text)) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Dismiss")
            }
        }
    )
}