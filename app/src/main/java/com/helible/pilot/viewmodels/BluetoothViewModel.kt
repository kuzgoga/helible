package com.helible.pilot.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helible.pilot.controllers.BluetoothController
import com.helible.pilot.controllers.ConnectionResult
import com.helible.pilot.dataclasses.AlarmStateMessage
import com.helible.pilot.dataclasses.BluetoothDevice
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.EmergStopMessage
import com.helible.pilot.dataclasses.RotorsSpeedMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val bluetoothController: BluetoothController,
) : ViewModel() {

    private val _selectedDevice: MutableStateFlow<BluetoothDevice?> = MutableStateFlow(null)
    val selectedDevice: StateFlow<BluetoothDevice?>
        get() = _selectedDevice.asStateFlow()

    private val _state: MutableStateFlow<BluetoothUiState> = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> =
        combine(bluetoothController.scannedDevices, bluetoothController.pairedDevices, _state)
        { scannedDevices, pairedDevices, state ->
            state.copy(
                scannedBluetoothDevices = scannedDevices.toList(),
                pairedBluetoothDevices = pairedDevices
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val rotorsStateMessegeAdapter = moshi.adapter(RotorsSpeedMessage::class.java)
    private val alarmStateMessageAdapter = moshi.adapter(AlarmStateMessage::class.java)
    private val emergStopMessageAdapter = moshi.adapter(EmergStopMessage::class.java)

    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
        bluetoothController.isScanning.onEach { isDiscovering ->
            _state.update {
                it.copy(
                    isDiscovering = isDiscovering,
                )
            }
        }.launchIn(viewModelScope)
        bluetoothController.isEnabled.onEach { isEnabled ->
            _state.update {
                it.copy(
                    isEnabled = isEnabled,
                )
            }
        }.launchIn(viewModelScope)
        bluetoothController.isLocationEnabled.onEach { isLocationEnabled ->
            _state.update {
                it.copy(
                    isLocationEnabled = isLocationEnabled
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.TransferSucceded -> {
                    _state.update { it.copy(

                    ) }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update { it.copy(
                    isConnected = false,
                    isConnecting = false
                ) }
            }
            .launchIn(viewModelScope)
    }

    private var deviceConnectionJob: Job? = null

    fun connectToDevice(device: String) {
        if (_state.value.isConnected and _state.value.isConnecting) {
            return
        }
        _state.update { it.copy(isConnecting = true) }
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

    fun selectDevice(selectedDevice: BluetoothDevice?) {
        _selectedDevice.update { selectedDevice }
    }

    fun startScan() {
        selectDevice(null)
        bluetoothController.startDiscovery()
    }

    fun cancelScan() {
        bluetoothController.cancelDiscovery()
    }

    override fun onCleared() {
        cancelScan()
        bluetoothController.onDestroy()
        super.onCleared()
    }

    fun sendHelloWorld() {
        viewModelScope.launch {
            bluetoothController.trySendMessage(
                "{\"p1\": {\"p\": 1.5, \"i\": 1.5, \"d\": 1.5}}\n\r".toByteArray()
                //"{\"p1\": [1.5, 1.5, 1.5]}\n\r".toByteArray()
            )
        }
    }
}