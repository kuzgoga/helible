package com.helible.pilot.components.scannerScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.dataclasses.BluetoothDevice
import com.helible.pilot.R

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    deviceInfo: BluetoothDevice,
    selectedDevice: BluetoothDevice?,
    choiceDevice: (device: BluetoothDevice?) -> Unit,
    modifier: Modifier,
) {
    ElevatedCard(
        modifier = modifier.clickable {
            choiceDevice(deviceInfo)
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (deviceInfo == selectedDevice)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = deviceInfo.name,
                    fontWeight = FontWeight.Bold,
                    softWrap = true
                )
                Text(
                    text = "MAC: ${deviceInfo.macAddress}",
                    fontWeight = FontWeight.Thin
                )
            }
            if (deviceInfo.isScanned) {
                Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painterResource(id = getSignalIconForRssiValue(deviceInfo.rssi)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(10.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun getSignalIconForRssiValue(rssi: Short): Int {
    if (rssi >= -80) return R.drawable.signal_icon4
    else if (rssi >= -90) return R.drawable.signal_icon3
    else if (rssi >= -100) return R.drawable.signal_icon2
    return R.drawable.signal_icon1
}

@Preview
@Composable
fun DeviceItemPreview() {
    DeviceItem(
        BluetoothDevice("Helicopter", "AA:BB:CC:DD:FF", -90, true),
        null,
        {_ ->  },
        modifier = Modifier.size(500.dp, 60.dp)
    )
}