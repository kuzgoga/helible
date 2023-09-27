package com.helible.pilot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build

class BluetoothAdapterStateReceiver(
    private val onBluetoothEnabledChanged: (isBluetoothEnabled: Boolean) -> Unit,
    private val onDiscoveryRunningChanged: (isDiscoveryRunning: Boolean) -> Unit,
    private val onLocationEnabledChanged: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_ON -> {
                        onBluetoothEnabledChanged(true)
                    }

                    BluetoothAdapter.STATE_OFF -> {
                        onBluetoothEnabledChanged(false)
                    }
                }
            }
            LocationManager.PROVIDERS_CHANGED_ACTION -> {
                onLocationEnabledChanged()
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryRunningChanged(false)
            }

            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                onDiscoveryRunningChanged(true)
            }
        }
    }
}

class BluetoothStateReceiver(
    private val onDeviceFound: (device: BluetoothDevice, rssi: Short) -> Unit,
    private val onConnectedStateChanged: (isConnected: Boolean, BluetoothDevice) -> Unit
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
        when(intent?.action) {
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