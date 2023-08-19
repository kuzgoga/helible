package com.helible.pilot.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.helible.pilot.Device


@SuppressLint("MissingPermission")
@Composable
fun BluetoothScannerScreen(
    devices: MutableList<Device>,
    selectedDevice: MutableState<Device?>,
    bluetoothIsDiscoveringState: MutableState<Boolean>,
    bluetoothAdapter: BluetoothAdapter?,
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
                devices = devices,
                selectedDevice = selectedDevice,
                modifier = Modifier
                    .constrainAs(devicesList) {
                        top.linkTo(title.bottom)
                        bottom.linkTo(controls.top)
                        height = Dimension.fillToConstraints
                    }
            )

            if (devices.isEmpty() && bluetoothIsDiscoveringState.value) {
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
                    checked = bluetoothIsDiscoveringState.value,
                    onCheckedChange = {
                        selectedDevice.value = null
                        if (bluetoothIsDiscoveringState.value)
                            bluetoothAdapter?.cancelDiscovery()
                        else {
                            devices.clear()
                            bluetoothAdapter?.startDiscovery()
                        }
                    }, modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(5.dp)
                ) {
                    Icon(
                        if (bluetoothIsDiscoveringState.value) Icons.Filled.Close
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
                    enabled = selectedDevice.value != null,
                ) {
                    Text(text = "Далее")
                }
            }

        }
    }
}