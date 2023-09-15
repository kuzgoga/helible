package com.helible.pilot

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class BluetoothUiState(
    val isEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isDiscovering: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val scannedDevices: List<Device> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
)

class BluetoothViewModel(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _selectedDevice: MutableStateFlow<Device?> = MutableStateFlow(null)
    val selectedDevice: StateFlow<Device?>
        get () = _selectedDevice.asStateFlow()

    private val _state: MutableStateFlow<BluetoothUiState> = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = combine(bluetoothController.scannedDevices, bluetoothController.pairedDevices, _state)
    { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices.toList(),
            pairedDevices = pairedDevices.toList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    init {
        bluetoothController.isConnected.onEach {
                isConnected -> _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
        bluetoothController.isScanning.onEach { result ->
            _state.update {it.copy(
                isDiscovering = result,
            )}
        }.launchIn(viewModelScope)
        bluetoothController.isEnabled.onEach { result ->
            _state.update {it.copy(
                 isEnabled = result,
            )}
        }.launchIn(viewModelScope)
        bluetoothController.isLocationEnabled.onEach { result ->
            _state.update { it.copy(
                isLocationEnabled = result
            ) }
        }.launchIn(viewModelScope)
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }
                is ConnectionResult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update {
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
        _state.update {it.copy(isConnecting = true)}
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false
            )
        }
    }

    fun selectDevice(selectedDevice: Device?) {
        _selectedDevice.update { selectedDevice }
    }

    fun onDestroy() {
        cancelScan()
        bluetoothController.onDestroy()
    }

    fun startScan(){
        selectDevice(null)
        bluetoothController.startDiscovery()
    }

    fun cancelScan() {
        bluetoothController.cancelDiscovery()
    }
}