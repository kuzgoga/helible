package com.helible.pilot.components.deviceScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.R
import com.helible.pilot.dataclasses.BluetoothUiState

@Composable
fun DeviceConnectionStatus(bluetoothState: BluetoothUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (bluetoothState.isConnected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(56, 200, 35),
                modifier = Modifier
                    .requiredSize(Icons.Default.CheckCircle.defaultWidth)
                    .padding(2.dp)
            )
            Text("На связи")
        } else if (bluetoothState.errorMessage != null) {
            Icon(
                painter = painterResource(id = R.drawable.cancel),
                contentDescription = null,
                tint = Color(255, 24, 35),
                modifier = Modifier
                    .requiredSize(R.drawable.cancel.dp)
                    .padding(2.dp)
            )
            Text("Ошибка: ${bluetoothState.errorMessage}")
        } else if (bluetoothState.isConnecting) {
            Icon(
                painter = painterResource(id = R.drawable.sync),
                contentDescription = null,
                tint = Color(40, 123, 207),
                modifier = Modifier
                    .requiredSize(R.drawable.sync.dp)
                    .padding(2.dp)
            )
            Text("Подключение...")
        }
    }
}

@Preview
@Composable
fun DeviceConnectionStatusPreview() {
    Surface {
        DeviceConnectionStatus(bluetoothState = BluetoothUiState(isConnecting = true))
    }
}