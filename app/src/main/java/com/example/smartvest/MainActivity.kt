package com.example.smartvest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartvest.ui.theme.SmartVestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartVestTheme {
                MainApp(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MainApp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        ConnectionStatus()
    }
}

@Composable
fun ConnectionStatus(modifier: Modifier = Modifier)
{
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

@Preview(showBackground = true, widthDp = 320)
@Composable
fun MainAppPreview() {
    SmartVestTheme {
        MainApp()
    }
}
