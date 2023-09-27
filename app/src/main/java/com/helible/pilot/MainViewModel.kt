package com.helible.pilot

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.helible.pilot.components.SavedPreferences
import com.helible.pilot.components.SavedPreferencesCache

data class Device(
    val bluetoothDevice: BluetoothDevice,
    val rssi: Short,
    val isPaired: Boolean = false
)

class PermissionDialogViewModel: ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if(!isGranted && !visiblePermissionDialogQueue.contains(permission)){
            visiblePermissionDialogQueue.add(permission)
        }
    }
}

class PersistentViewModel(
    private val preferencesCache: SavedPreferencesCache,
) : ViewModel() {
    val preferences get() = preferencesCache.getPreferences()
    fun savePreferences(savedPreferences: SavedPreferences) {
        preferencesCache.savePreferences(
            preferences = savedPreferences
        )
    }
}