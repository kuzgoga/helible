package com.helible.pilot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.IOException
import java.util.UUID

sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult
    data class Error(val message: String) : ConnectionResult
}

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val errors: SharedFlow<String>

    fun connectToDevice(device: Device?): Flow<ConnectionResult>
    fun closeConnection()
}

class AndroidBluetoothController : BluetoothController {

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private var currentClientSocket: BluetoothSocket? = null

    companion object {
        const val SERVICE_UUID = "af7cc14b-cffa-4a3d-b677-01b0ff0a93d7"
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: Device?): Flow<ConnectionResult> {
        return flow {
            currentClientSocket = device?.bluetoothDevice?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                } catch (e: IOException) {
                    closeConnection()
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion { closeConnection() }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentClientSocket = null
    }
}
