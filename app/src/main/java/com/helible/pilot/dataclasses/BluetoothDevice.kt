package com.helible.pilot.dataclasses

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String,
    val macAddress: String,
    val rssi: Short,
    val isScanned: Boolean = false,
)
