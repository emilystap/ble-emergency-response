package com.example.smartvest.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartvest.ui.TopAppBar
import com.example.smartvest.ui.states.SettingsUiState
import com.example.smartvest.ui.theme.SmartVestTheme
import com.example.smartvest.ui.viewmodels.SettingsViewModel
import com.example.smartvest.util.PermissionUtil

private const val TAG = "SettingsScreen"
private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navController: NavHostController,
    title: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    permissionRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { PermissionUtil.checkPermissionRequestResults(it) }

    SmartVestTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(navController, title) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    LocationEnable(viewModel = viewModel, uiState = uiState)
                    SmsEnable(viewModel = viewModel, uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun LocationEnable(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    uiState: SettingsUiState
) {
    val enabled = uiState.locationEnabled

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    Row {
        Text(
            text = "Enable Location Tracking",
            modifier = modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                viewModel.setLocationEnabled(it)

                if (enabled) {  /* TODO: move this to view model? */
                    PermissionUtil.checkPermissions(
                        permissionRequestLauncher,
                        permissions
                    )
                }
            }
        )
    }
}

@Composable
private fun SmsEnable(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    uiState: SettingsUiState
) {
    val enabled = uiState.smsEnabled

    val permissions = arrayOf(Manifest.permission.SEND_SMS)

    Row {
        Text(
            text = "Enable Emergency SMS",
            modifier = modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                viewModel.setSmsEnabled(it)

                if (enabled) {
                    PermissionUtil.checkPermissions(
                        permissionRequestLauncher,
                        permissions
                    )
                }
            }
        )
    }
    if (enabled) {
        Row {
            EditSmsNumber(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun EditSmsNumber(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    uiState: SettingsUiState
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var valid by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    val storedSmsNumber = uiState.storedSmsNumber

    OutlinedTextField(
        value = text,
        singleLine = true,
        isError = !valid,
        label = { Text("Emergency Number") },
        supportingText = {
            if (!valid) {
                Text(
                    text = "Invalid Number",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else if (storedSmsNumber.isNotEmpty()) {
                Text(
                    text = (  // number format: (XXX) XXX-XXXX
                        "Stored Number: " + "(" + storedSmsNumber.substring(0, 3) + ") " +
                        storedSmsNumber.substring(3, 6) + "-" +
                        storedSmsNumber.substring(6, 10)
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        onValueChange = {
            valid = true
            text = it.filter { symbol ->
                symbol.isDigit()  // allow only numeric values
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                /* TODO: validate number */
                if (text.length == 10) {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    viewModel.setStoredSmsNumber(text)
                }
                else {
                    valid = false
                }
            }
        )
    )
}