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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.AppScreen
import com.example.smartvest.ui.theme.SmartVestTheme

@Composable
fun SettingsScreen(navController: NavHostController, title: String? = null) {
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
    Column(modifier = Modifier.padding(24.dp)) {
        LocationEnable()
        SMSEnable()
    }
}

@Composable
fun LocationEnable() {
    var enabled by remember { mutableStateOf(true) }

    Row {
        Text(
            text = "Enable Location Tracking",
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
            }
        )
    }
}

@Composable
fun SMSEnable() {
    var enabled by remember { mutableStateOf(true) }

    Row {
        Text(
            text = "Enable Emergency SMS",
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
            }
        )
    }
    if (enabled) {
        Row {
            SMSEdit()
        }
    }
}

@Composable
fun SMSEdit() {
    var text by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
                if (text.length == 10) {
                    text = "(" + text.substring(0, 3) + ") " +
                            text.substring(3, 6) + "-" + text.substring(6, 10)

                    keyboardController?.hide()
                    focusManager.clearFocus()
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
    SettingsScreen(navController = rememberNavController(), title = AppScreen.Settings.route)
}
