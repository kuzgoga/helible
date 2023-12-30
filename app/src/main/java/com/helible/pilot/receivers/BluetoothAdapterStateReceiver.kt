package com.helible.pilot.receivers

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
    private val onLocationEnabledChanged: () -> Unit,
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