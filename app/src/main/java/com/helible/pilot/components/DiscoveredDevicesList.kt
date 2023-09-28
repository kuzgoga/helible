package com.helible.pilot.components

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
import com.helible.pilot.BluetoothUiState
import com.helible.pilot.Device

@Composable
fun DiscoveredDevicesList(
    bluetoothState: BluetoothUiState,
    selectedDevice: Device?,
    choiceDevice: (device: Device?) -> Unit,
    modifier: Modifier = Modifier
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
        items(bluetoothState.pairedDevices) { device ->
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
        if(bluetoothState.pairedDevices.isEmpty()){
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

        items(bluetoothState.scannedDevices) { device ->
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
        if(bluetoothState.scannedDevices.isEmpty()) {
            if(bluetoothState.isDiscovering) {
                item {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(text = "Поиск устройств", modifier=Modifier.padding(10.dp))
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