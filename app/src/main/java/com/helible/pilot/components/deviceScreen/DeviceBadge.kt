package com.helible.pilot.components.deviceScreen

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.R
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.viewmodels.AppPreferences

@Composable
fun DeviceBadge(
    bluetoothUiState: BluetoothUiState,
    tryToReconnect: () -> Unit,
    getPreferences: () -> AppPreferences?
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        shape = RoundedCornerShape(15)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(15)
                }
                .fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.helicopter),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = getPreferences()?.deviceName ?: "null",
                    fontWeight = FontWeight.Bold
                )
                DeviceConnectionStatus(bluetoothUiState)
                Text(text = "Заряд батареи: 79%")
            }
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .requiredSize(Icons.Default.Refresh.defaultWidth)
                        .clickable { tryToReconnect() }
                )
            }
        }
    }
}

@Preview
@Composable
fun DeviceBadgePreview() {
    DeviceBadge(
        bluetoothUiState = BluetoothUiState(isConnected = true),
        tryToReconnect = {},
        getPreferences = {AppPreferences("Helicopter", "AA:BB:CC:FF:DD")}
    )
}