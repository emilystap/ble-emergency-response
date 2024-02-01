package com.example.smartvest.ui

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.AppScreen
import com.example.smartvest.data.SettingsStore
import com.example.smartvest.ui.theme.SmartVestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    title: String? = null
) {
    SmartVestTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(navController, title) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                SettingsMenu()
            }
        }
    }
}

@Composable
fun SettingsMenu() {
    val dataStore = SettingsStore(LocalContext.current)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(24.dp)) {
        LocationEnable(dataStore, scope)
        SmsEnable(dataStore, scope)
    }
}

@Composable
fun LocationEnable(dataStore: SettingsStore, scope: CoroutineScope) {
    var enabled = dataStore.locationEnable.collectAsState(initial = false).value

    Row {
        Text(
            text = "Enable Location Tracking",
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                scope.launch {
                    dataStore.setLocationEnable(enabled)
                }
            }
        )
    }
}

@Composable
fun SmsEnable(dataStore: SettingsStore, scope: CoroutineScope) {
    var enabled = dataStore.smsEnable.collectAsState(initial = false).value

    Row {
        Text(
            text = "Enable Emergency SMS",
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                scope.launch {
                    dataStore.setSmsEnable(enabled)
                }
            }
        )
    }
    if (enabled) {
        Row {
            EditSmsNumber(dataStore, scope)
        }
    }
}

@Composable
fun EditSmsNumber(dataStore: SettingsStore, scope: CoroutineScope) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var valid by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    val storedSmsNumber = dataStore.smsNumber.collectAsState(initial = "").value

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
                    text = (
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
                //** TODO: validate number
                if (text.length == 10) {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    scope.launch {
                        dataStore.setSmsNumber(text)
                    }
                }
                else {
                    valid = false
                }
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen(
        navController = rememberNavController(),
        title = AppScreen.Settings.route
    )
}
