package com.helible.pilot.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helible.pilot.controllers.BluetoothController
import com.helible.pilot.controllers.ConnectionResult
import com.helible.pilot.dataclasses.BluetoothDevice
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.ChangedDeviceStatus
import com.helible.pilot.dataclasses.DeviceState
import com.helible.pilot.dataclasses.DeviceStatus
import com.helible.pilot.dataclasses.DeviceStatusJsonAdapter
import com.helible.pilot.dataclasses.MessageType
import com.helible.pilot.dataclasses.PidSettingRequiredMessage
import com.helible.pilot.dataclasses.PidSettings
import com.helible.pilot.dataclasses.RotorsDuty
import com.helible.pilot.dataclasses.SticksPosition
import com.helible.pilot.dataclasses.StopMessage
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
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
import kotlinx.coroutines.flow.flow
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
    private val _rotorsDuty: MutableStateFlow<RotorsDuty> = MutableStateFlow(RotorsDuty(0, 0, 0))
    private val _isRotorsTelemetryEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isConsoleTelemetryEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _sticksPosition: MutableStateFlow<SticksPosition> = MutableStateFlow(SticksPosition(0, 0, 0))

    val rotorsDuty: StateFlow<RotorsDuty>
        get() = _rotorsDuty.asStateFlow()

    private var deviceConnectionJob: Job? = null

    private val moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).add(DeviceStatusJsonAdapter()).build()
    private val statusMessageAdapter = moshi.adapter(ChangedDeviceStatus::class.java)
    private val deviceStateMessageAdapter = moshi.adapter(DeviceState::class.java)
    private val pidSittingsMessageAdapter = moshi.adapter(PidSettings::class.java)
    private val pidSittingsRequiredMessageAdapter =
        moshi.adapter(PidSettingRequiredMessage::class.java)
    private val rotorDutyMessageAdapter = moshi.adapter(RotorsDuty::class.java)
    private val stopAllRotorsMessageAdapter = moshi.adapter(StopMessage::class.java)
    private val consoleStateMessageAdapter = moshi.adapter(SticksPosition::class.java)

    companion object {
        const val messageDelimiter = "\n"
        const val telemetryPauseDuractionMs: Long = 100
    }

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
                    try {
                        when (result.message.type) {
                            MessageType.PidSettings -> {
                                val newPidSettings =
                                    pidSittingsMessageAdapter.fromJson(result.message.data)
                                _state.update {
                                    it.copy(
                                        deviceState = it.deviceState?.copy(pidSettings = newPidSettings)
                                    )
                                }
                            }

                            MessageType.UpdateMessage -> {
                                val newDeviceState =
                                    deviceStateMessageAdapter.fromJson(result.message.data)
                                if (newDeviceState != null) {
                                    _state.update {
                                        it.copy(
                                            deviceState = newDeviceState.copy(pidSettings = it.deviceState?.pidSettings)
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: JsonDataException) {
                        Log.e("BluetoothVM", "Failed to parse message: ${result.message.data}")
                    } catch (e: JsonEncodingException) {
                        Log.e("BluetoothVM", "Failed to decode message: ${result.message.data}")
                    } catch (e: Exception) {
                        Log.e("BluetoothVM", "Unknown error on message: ${result.message.data}")
                    }
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
                Log.e(
                    "BluetoothVM",
                    "Error occured while data transfer: ${throwable.localizedMessage}"
                )
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                        deviceState = null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun connectToDevice(device: String) {
        if (_state.value.isConnected or _state.value.isConnecting) {
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
        _isConsoleTelemetryEnabled.update { false }
        _isRotorsTelemetryEnabled.update { false }
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
                deviceState = null
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

    fun startImuCalibration() {
        viewModelScope.launch {
            val message = statusMessageAdapter.toJson(
                ChangedDeviceStatus(DeviceStatus.IsImuCalibration)
            ) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(
                message.toByteArray()
            )
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to start IMU calibration: $message")
            } else {
                _state.update {
                    it.copy(
                        deviceState = it.deviceState?.copy(status = DeviceStatus.IsImuCalibration)
                    )
                }
            }
        }
    }

    fun requestPidSettings() {
        viewModelScope.launch {
            val message =
                pidSittingsRequiredMessageAdapter.toJson(PidSettingRequiredMessage(true)) + messageDelimiter
            Log.i("BluetoothVM", "Requested PID settings: $message")
            val isSuccess = bluetoothController.trySendMessage(
                message.toByteArray()
            )
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to request PID settings: $message")
            }
        }
    }

    fun applyPidSettings(pidSettings: PidSettings) {
        viewModelScope.launch {
            val message = pidSittingsMessageAdapter.toJson(pidSettings) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(message.toByteArray())
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to request PID settings: $message")
                _state.update {
                    it.copy(errorMessage = "Не удалось обновить значения PID")
                }
            } else {
                _state.update {
                    it.copy(deviceState = it.deviceState?.copy(pidSettings = pidSettings))
                }
            }
        }
    }

    fun clearPidSettings() {
        Log.i("BluetoothVM", "PidSettings cleared")
        _state.update {
            it.copy(deviceState = it.deviceState?.copy(pidSettings = null))
        }
        Log.i("BluetoothVM", "PidSettings: ${_state.value.deviceState?.pidSettings}")
    }

    private fun sendRotorsDuty() {
        viewModelScope.launch {
            val message = rotorDutyMessageAdapter.toJson(
                _rotorsDuty.value
            ) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(
                message.toByteArray()
            )
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to send rotors telemetry: $message")
            }
        }
    }

    fun startRotorsConfigurationTelemetry() {
        Log.i("BluetoothVM", "Start send rotors configuration telemetry...")
        if(_isRotorsTelemetryEnabled.value) return
        _isRotorsTelemetryEnabled.update { true }
        flow {
            while(_isRotorsTelemetryEnabled.value) {
                emit(Unit)
                delay(telemetryPauseDuractionMs)
            }
        }.onEach{
            sendRotorsDuty()
            Log.d("BluetoothVM", "Sended rotors telemetry")
        }.launchIn(viewModelScope)
    }

    fun stopRotorsConfigurationTelemetry() {
        Log.i("BluetoothVM", "Stop send rotors configuration periodically...")
        _isRotorsTelemetryEnabled.update { false }
    }

    fun setRotorsDuty(newRotorsDuty: RotorsDuty) {
        _rotorsDuty.update { newRotorsDuty }
    }

    fun stopRotors() {
        viewModelScope.launch {
            val message = stopAllRotorsMessageAdapter.toJson(StopMessage()) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(message.toByteArray())
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to stop all rotors: $message")
                _state.update {
                    it.copy(errorMessage = "Не удалось остановить моторы!")
                }
            } else {
                _rotorsDuty.update { RotorsDuty(0, 0, 0) }
                _isConsoleTelemetryEnabled.update { false }
            }
        }
    }
    fun startTakeoff() {
        viewModelScope.launch {
            val message = statusMessageAdapter.toJson(ChangedDeviceStatus(DeviceStatus.IsFlying)) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(message.toByteArray())
            if(!isSuccess) {
                Log.e("BluetoothVM", "Failed to start takeoff: $message")
                _state.update {
                    it.copy(errorMessage = "Не удалось начать полёт!")
                }
            } else {
                _state.update { it.copy(deviceState = it.deviceState?.copy(status = DeviceStatus.IsFlying)) }
                startConsoleTelemetrySending()
            }
        }
    }

    fun startOnboarding() {
        viewModelScope.launch {
            val message = statusMessageAdapter.toJson(ChangedDeviceStatus(DeviceStatus.IsBoarding)) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(message.toByteArray())
            if(!isSuccess) {
                Log.e("BluetoothVM", "Failed to start onboarding: $message")
            } else {
                _state.update { it.copy(deviceState = it.deviceState?.copy(status = DeviceStatus.IsBoarding)) }
                stopConsoleTelemetry()
            }
        }
    }

    fun changeHeightStickPosition(newHeightStickPosition: Int) {
        _sticksPosition.update {
            it.copy(heightStick = newHeightStickPosition)
        }
    }

    fun changeYawStickPosition(newYawStickPosition: Int) {
        _sticksPosition.update {
            it.copy(yawStick = newYawStickPosition)
        }
    }

    fun changePitchStickPosition(newPitchStickPosition: Int) {
        _sticksPosition.update {
            it.copy(pitchStick = newPitchStickPosition)
        }
    }

    private fun sendConsoleState() {
        viewModelScope.launch {
            val message = consoleStateMessageAdapter.toJson(
                _sticksPosition.value
            ) + messageDelimiter
            val isSuccess = bluetoothController.trySendMessage(
                message.toByteArray()
            )
            Log.i("BluetoothVM", "Sended telemetry message: $message")
            if (!isSuccess) {
                Log.e("BluetoothVM", "Failed to send console telemetry: $message")
            }
        }
    }

    private fun startConsoleTelemetrySending() {
        if(_isConsoleTelemetryEnabled.value) {
            return
        }
        _isConsoleTelemetryEnabled.update { true }
        flow {
            while (_isConsoleTelemetryEnabled.value) {
                emit(Unit)
                delay(telemetryPauseDuractionMs)
            }
        }.onEach {
            sendConsoleState()
        }.launchIn(viewModelScope)
    }

    private fun stopConsoleTelemetry() {
        _isConsoleTelemetryEnabled.update { false }
    }
}