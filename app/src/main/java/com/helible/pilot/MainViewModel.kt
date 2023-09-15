package com.helible.pilot

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helible.pilot.components.SavedPreferences
import com.helible.pilot.components.SavedPreferencesCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class Device(
    val bluetoothDevice: BluetoothDevice,
    val rssi: Short,
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