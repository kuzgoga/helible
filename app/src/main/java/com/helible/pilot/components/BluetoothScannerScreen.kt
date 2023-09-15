package com.helible.pilot.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.helible.pilot.BluetoothUiState
import com.helible.pilot.Device


@SuppressLint("MissingPermission")
@Composable
fun BluetoothScannerScreen(
    bluetoothState: BluetoothUiState,
    selectedDevice: Device?,
    startScan: () -> Unit,
    cancelScan: () -> Unit,
    choiceDevice: (device: Device?) -> Unit,
    onScreenChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (title, devicesList, controls) = createRefs()

            Title(
                text = "Поиск устройств",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .constrainAs(title) {}
            )

            DiscoveredDevicesList(
                devices = bluetoothState.scannedDevices,
                selectedDevice = selectedDevice,
                choiceDevice = choiceDevice,
                modifier = Modifier
                    .constrainAs(devicesList) {
                        top.linkTo(title.bottom)
                        bottom.linkTo(controls.top)
                        height = Dimension.fillToConstraints
                    }
            )

            if (bluetoothState.scannedDevices.isEmpty() && bluetoothState.isDiscovering) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .constrainAs(controls) {
                        bottom.linkTo(parent.bottom)
                        width = Dimension.matchParent
                        height = Dimension.fillToConstraints
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                FilledIconToggleButton(
                    checked = bluetoothState.isDiscovering,
                    onCheckedChange = {
                        if (bluetoothState.isDiscovering) {
                            cancelScan()
                            Log.i("ScanActivity", "Trying to start scan via button")
                        } else {
                            startScan()
                        }
                    }, modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(5.dp)
                ) {
                    Icon(
                        if (bluetoothState.isDiscovering) Icons.Filled.Close
                        else Icons.Filled.Refresh,
                        contentDescription = null
                    )
                }
                Button(
                    onClick = {
                        onScreenChanged()
                    },
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(5.dp),
                    enabled = selectedDevice != null,
                ) {
                    Text(text = "Далее")
                }
            }

        }
    }
}