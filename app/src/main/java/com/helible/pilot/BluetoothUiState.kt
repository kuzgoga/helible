package com.helible.pilot

import android.bluetooth.BluetoothDevice
data class BluetoothUiState(
    val isEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isDiscovering: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val scannedDevices: List<Device> = emptyList(),
    val pairedDevices: List<Device> = emptyList(),
)