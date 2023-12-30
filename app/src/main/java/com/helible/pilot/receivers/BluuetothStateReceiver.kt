package com.helible.pilot.receivers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothStateReceiver(
    private val onDeviceFound: (device: BluetoothDevice, rssi: Short) -> Unit,
    private val onConnectedStateChanged: (isConnected: Boolean, BluetoothDevice) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                @SuppressLint("MissingPermission") if (device?.name != null)
                    onDeviceFound(device, rssi)
            }

            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onConnectedStateChanged(true, device ?: return)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onConnectedStateChanged(false, device ?: return)
            }
        }
    }
}