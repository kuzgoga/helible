package com.helible.pilot.components.deviceScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.R
import com.helible.pilot.components.Title
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.viewmodels.AppPreferences

@Composable
fun DeviceControlScreen(
    bluetoothUiState: BluetoothUiState,
    getPreferences: () -> AppPreferences?,
    navigateToPage: (String) -> Unit,
    connectToDevice: (String) -> Unit,
    disconnectFromDevice: () -> Unit,
    deviceActionsList: Map<String, Array<Pair<String, Pair<Pair<Int, Color>, String>>>>,
    scannerPageName: String = "scanner",
) {
    LaunchedEffect(Unit) {
        val preferences: AppPreferences? = getPreferences()
        if (preferences == null) {
            navigateToPage(scannerPageName)
        } else {
            connectToDevice(preferences.deviceAddress)
        }
    }

    LaunchedEffect(key1 = bluetoothUiState.isEnabled) {
        /* Trying to reconnect, when bluetooth is turned on */
        val preferences = getPreferences()
        if(preferences != null && bluetoothUiState.isEnabled)
            connectToDevice(preferences.deviceAddress)
    }

    LaunchedEffect(key1 = bluetoothUiState.isLocationEnabled) {
        /* Trying to reconnect, when location is turned on */
        val preferences = getPreferences()
        if(preferences != null && bluetoothUiState.isLocationEnabled)
            connectToDevice(preferences.deviceAddress)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Title(
            text = "Ваше устройство",
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)
        )
        DeviceBadge(
            bluetoothUiState = bluetoothUiState,
            tryToReconnect = {
                /* Trying to reconnect, when error occurred */
                val preferences = getPreferences()
                if(preferences != null)
                    connectToDevice(preferences.deviceAddress)
            },
            getPreferences = getPreferences
        )

        Column(modifier = Modifier.padding(horizontal = 3.dp)) {
            for (section in deviceActionsList) {
                Text(section.key,
                    color = Color.Gray,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)
                )
                for (action in section.value) {
                    TextButton(onClick = { /* TODO */}) {
                        Icon(
                            painter = painterResource(id = action.second.first.first),
                            tint = action.second.first.second,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                        Text(
                            text = action.second.second,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }

            TextButton(onClick = {
                disconnectFromDevice()
                navigateToPage(scannerPageName)
            }, modifier = Modifier.padding(vertical = 10.dp)) {
                Icon(painterResource(id = R.drawable.logout), contentDescription = null)
                Text(
                    text = "Отвязать устройство",
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun DeviceControlScreenPreview() {
    Surface {
        DeviceControlScreen(
            bluetoothUiState = BluetoothUiState(isConnected = true),
            getPreferences = { AppPreferences("Helicopter", "AA:BB:CC:DD:FF") },
            navigateToPage = { /*TODO*/ },
            connectToDevice = {},
            disconnectFromDevice = { /*TODO*/ },
            deviceActionsList = defaultDeviceActionsList()
        )
    }
}