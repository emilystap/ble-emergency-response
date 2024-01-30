package com.example.smartvest.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.ui.theme.SmartVestTheme

@Composable
fun HomeScreen(navController: NavHostController, title: String? = null) {
    SmartVestTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(navController, title, canReturn = false) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ConnectionStatus()
            }
        }
    }
}

@Composable
fun ConnectionStatus(modifier: Modifier = Modifier) {
    // val connected by remember { mutableStateOf(true) }
    val connected = remember { mutableStateOf(true) }

    Row(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Status: "
        )
        Text(
            text = if (connected.value) "Connected" else "Disconnected",
            color = (
                    if (connected.value) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error
                    ),
            modifier = modifier.weight(1f)
        )
        FilledTonalButton(
            onClick = {
                /* TODO: Implement BT connection */
                connected.value = !connected.value
            }
        ) {
            Text("Refresh")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen(navController = rememberNavController(), title = "Home")
}