package com.helible.pilot.components.scannerScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helible.pilot.components.scannerScreen.DeviceItem
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.BluetoothDevice

@Composable
fun DiscoveredDevicesList(
    bluetoothState: BluetoothUiState,
    selectedDevice: BluetoothDevice?,
    choiceDevice: (device: BluetoothDevice?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "Ранее подключенные устройства",
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
        }
        items(bluetoothState.pairedBluetoothDevices) { device ->
            DeviceItem(
                deviceInfo = device,
                selectedDevice = selectedDevice,
                choiceDevice = choiceDevice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        5.dp
                    )
            )
        }
        if (bluetoothState.pairedBluetoothDevices.isEmpty()) {
            item {
                Text(
                    text = "Нет элементов для отображения",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            Text(
                text = "Доступные устройства",
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
        }

        items(bluetoothState.scannedBluetoothDevices) { device ->
            DeviceItem(
                deviceInfo = device,
                selectedDevice = selectedDevice,
                choiceDevice = choiceDevice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        5.dp
                    )
            )
        }
        if (bluetoothState.scannedBluetoothDevices.isEmpty()) {
            if (bluetoothState.isDiscovering) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Поиск устройств", modifier = Modifier.padding(10.dp))
                    }
                }
            } else {
                item {
                    Text(
                        text = "Устройства поблизости не обнаружены",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}