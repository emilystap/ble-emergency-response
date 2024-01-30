package com.example.smartvest.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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
    Row(modifier = Modifier.padding(24.dp)) {
        Text("Placeholder")
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen(navController = rememberNavController(), title = "Settings")
}
