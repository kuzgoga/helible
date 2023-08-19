package com.helible.pilot

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.util.Log


fun registerIntentFilters(activity: Activity, receiver: BroadcastReceiver) {
    activity.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    activity.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    activity.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
    activity.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    if (Build.VERSION.SDK_INT <= 30)
        activity.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
}

@SuppressLint("MissingPermission")
fun receiveIntentChanges(
    intent: Intent,
    mainViewModel: MainViewModel,
    bluetoothAdapter: BluetoothAdapter?,
    locationManager: LocationManager,
) {
    when (intent.action) {
        BluetoothDevice.ACTION_FOUND -> {
            val device = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
            if (device?.name != null)
                mainViewModel.devices.add(Device(device, rssi))
            Log.i(
                "ScanActivity",
                "Found new device: ${device?.name} ${device?.address} $rssi"
            )
        }

        BluetoothAdapter.ACTION_STATE_CHANGED -> {
            val state: Int = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            Log.i("ScanActvity", "Bluetooth state: $state")
            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    Log.i("ScanActvity", "Bluetooth turned on")
                    mainViewModel.bluetoothTurnOnState.value = true
                    mainViewModel.devices.clear()
                    bluetoothAdapter?.startDiscovery()
                }

                BluetoothAdapter.STATE_OFF -> {
                    Log.i("ScanActvity", "Bluetooth turned off")
                    mainViewModel.bluetoothTurnOnState.value = false
                }
            }
        }
        LocationManager.PROVIDERS_CHANGED_ACTION -> {
            mainViewModel.locationTurnOnState.value = locationManager.isLocationEnabled
            if (mainViewModel.locationTurnOnState.value == true) {
                Log.i("ScanActivity", "LOCATION IS ON")
            } else if (mainViewModel.locationTurnOnState.value == false)
                Log.i("ScanActivity", "LOCATION IS OFF")
        }

        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
            mainViewModel.isBluetoothDiscoveryRunning.value = false
            Log.i("ScanActivity", "DISCOVERY FINISHED")
        }

        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
            mainViewModel.isBluetoothDiscoveryRunning.value = true
            Log.i("ScanActivity", "DISCOVERY STARTED")
        }
    }
}