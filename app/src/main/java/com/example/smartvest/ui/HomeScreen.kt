package com.example.smartvest.ui

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.AppScreen
import com.example.smartvest.R
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.util.PermissionHandler

@Composable
fun HomeScreen(
    navController: NavHostController,
    permissionHandler: PermissionHandler,
    title: String? = null
) {
    SmartVestTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(navController, title, canReturn = false) },
            floatingActionButton = {
                if (permissionHandler.hasSmsPermission()) SendFab()
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ConnectionStatus()
            }
        }
    }
}

@Composable
fun ConnectionStatus(modifier: Modifier = Modifier) {
    var connected by remember { mutableStateOf(true) }

    Row(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Status: "
        )
        Text(
            text = if (connected) "Connected" else "Disconnected",
            color = (
                if (connected) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.error
            ),
            modifier = modifier.weight(1f)
        )
        FilledTonalButton(
            onClick = {
                /* TODO: Implement BT connection */
                connected = !connected
            }
        ) {
            Text("Refresh")
        }
    }
}

@Composable
fun SendFab() {
    var openSMSAlertDialog by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = {
            openSMSAlertDialog = true
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
                Log.d("SMS Alert Dialog", "Alert Confirmed")
                /* TODO: Implement SMS sending logic */
            }
        )
    }
}

@Composable
fun SMSAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning") },
        title = { Text(text = stringResource(id = R.string.sms_alert_dialog_title)) },  //** TODO: add check for location disabled
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

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(
        navController = rememberNavController(),
        permissionHandler = PermissionHandler(LocalContext.current),
        title = AppScreen.Home.route
    )
}
