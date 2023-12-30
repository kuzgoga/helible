package com.helible.pilot.dataclasses

data class BluetoothUiState(
    val isEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isDiscovering: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val scannedBluetoothDevices: List<BluetoothDevice> = emptyList(),
    val pairedBluetoothDevices: List<BluetoothDevice> = emptyList(),
)