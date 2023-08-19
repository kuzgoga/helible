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

data class BluetoothUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val bluetoothController: BluetoothController
) : ViewModel() {
    val devices: MutableList<Device> = mutableStateListOf()
    val selectedDevice: MutableState<Device?> = mutableStateOf(null)
    val bluetoothTurnOnState: MutableState<Boolean?> = mutableStateOf(false)
    val locationTurnOnState: MutableState<Boolean?> = mutableStateOf(null)
    val isBluetoothDiscoveryRunning: MutableState<Boolean> = mutableStateOf(false)

    private val _bluetoothState = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState>
        get() = _bluetoothState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _bluetoothState.value)

    init {
        bluetoothController.isConnected.onEach {
            isConnected -> _bluetoothState.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
        bluetoothController.errors.onEach { error ->
            _bluetoothState.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _bluetoothState.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }
                is ConnectionResult.Error -> {
                    _bluetoothState.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _bluetoothState.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
    private var deviceConnectionJob: Job? = null

    fun connectToDevice(device: Device) {
        _bluetoothState.update {it.copy(isConnecting = true)}
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _bluetoothState.update {
            it.copy(
                isConnecting = false,
                isConnected = false
            )
        }
    }
}

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