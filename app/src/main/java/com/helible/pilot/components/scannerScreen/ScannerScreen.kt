package com.helible.pilot.components.scannerScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import com.helible.pilot.components.Title
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.BluetoothDevice


@SuppressLint("MissingPermission")
@Composable
fun BluetoothScannerScreen(
    bluetoothState: BluetoothUiState,
    selectedDevice: BluetoothDevice?,
    startScan: () -> Unit,
    cancelScan: () -> Unit,
    choiceDevice: (device: BluetoothDevice?) -> Unit,
    onScreenChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (title, devicesList, controls) = createRefs()

            Title(
                text = "Устройства поблизости",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .constrainAs(title) {}
            )

            DiscoveredDevicesList(
                bluetoothState = bluetoothState,
                selectedDevice = selectedDevice,
                choiceDevice = choiceDevice,
                modifier = Modifier
                    .constrainAs(devicesList) {
                        top.linkTo(title.bottom)
                        bottom.linkTo(controls.top)
                        height = Dimension.fillToConstraints
                    }
            )

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