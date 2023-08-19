package com.helible.pilot.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helible.pilot.Device

@Composable
fun DiscoveredDevicesList(devices: MutableList<Device>, selectedDevice: MutableState<Device?>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(devices) { device ->
            DeviceItem(
                deviceInfo = device,
                selectedDevice = selectedDevice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        5.dp
                    )
            )
        }
    }
}