package com.helible.pilot.controllers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.helible.pilot.viewmodels.BluetoothDataTransferService
import com.helible.pilot.dataclasses.BluetoothDeviceDomain
import com.helible.pilot.dataclasses.GeneralMessage
import com.helible.pilot.receivers.BluetoothAdapterStateReceiver
import com.helible.pilot.receivers.BluetoothStateReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

sealed interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    data class TransferSucceded(val message: GeneralMessage) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}

interface BluetoothController {
    val isEnabled: StateFlow<Boolean>
    val isLocationEnabled: StateFlow<Boolean>
    val isConnected: StateFlow<Boolean>
    val isScanning: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun cancelDiscovery()
    fun connectToDevice(device: String): Flow<ConnectionResult>
    suspend fun trySendMessage(message: ByteArray): Boolean
    fun closeConnection()
    fun onDestroy()
}

@ExperimentalStdlibApi
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val locationManager: LocationManager? by lazy {
        context.getSystemService(ComponentActivity.LOCATION_SERVICE) as LocationManager
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val _isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean>
        get() = _isScanning.asStateFlow()

    private val _isEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isEnabled: StateFlow<Boolean>
        get() = _isEnabled.asStateFlow()

    private val _isLocationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLocationEnabled: StateFlow<Boolean>
        get() = _isLocationEnabled.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _scannedDevices: MutableStateFlow<List<BluetoothDeviceDomain>> =
        MutableStateFlow(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private var currentClientSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    private val bluetoothAdapterStateReceiver = BluetoothAdapterStateReceiver(
        onBluetoothEnabledChanged = { isEnabled ->
            _isEnabled.update { _ -> isEnabled }
            startDiscovery()
            Log.i("ScanActivity", "Bluetooth enabled status: $isEnabled")
        },
        onDiscoveryRunningChanged = { isDiscovering ->
            _isScanning.update { isDiscovering }
        },
        onLocationEnabledChanged = {
            if (locationManager?.isLocationEnabled == true) {
                _isLocationEnabled.update { true }
            } else {
                _isLocationEnabled.update { false }
            }
        }
    )

    @SuppressLint("MissingPermission")
    private val bluetoothStateReceiver = BluetoothStateReceiver(
        onDeviceFound = { device, rssi ->
            if (!hasAllPermissions()) return@BluetoothStateReceiver
            val newDevice =
                BluetoothDeviceDomain(device.name ?: "null", device.address, rssi, isScanned = true)
            _scannedDevices.update { devices ->
                if (newDevice in devices) devices else devices + newDevice
            }
            Log.i(
                "ScanActivity",
                "Found new device: ${device.name} ${device.address} $rssi"
            )
        },
        onConnectedStateChanged = { isConnected, device ->
            if (bluetoothAdapter?.bondedDevices?.contains(device) == true) {
                _isConnected.update { isConnected }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    _errors.emit("Can't connect to a non-paired device.")
                }
            }
        }
    )

    companion object {
        // SPP service UUID
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    init {
        updatePairedDevices()
        _isEnabled.update { bluetoothAdapter.isEnabled }
        _isLocationEnabled.update { locationManager?.isLocationEnabled == true }
        context.registerReceiver(
            bluetoothAdapterStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
                }
            }
        )
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_FOUND)
            }
        )

    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (!hasAllPermissions()) {
            Toast.makeText(context, "Ошибка: недостаточно разрешений", Toast.LENGTH_SHORT).show()
            return
        }
        if (!_isEnabled.value) {
            return
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (locationManager?.isLocationEnabled != true) return
        }

        updatePairedDevices()
        _scannedDevices.update { emptyList() }

        if (!bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancelDiscovery() {
        if (!hasAllPermissions()) return
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: String): Flow<ConnectionResult> {
        if (!hasAllPermissions()) {
            Toast.makeText(context, "Ошибка: нет разрешений", Toast.LENGTH_SHORT).show()
            return flow {}
        }
        return flow {
            Log.i("BluetoothController", "Connecting to device...")
            currentClientSocket =
                bluetoothAdapter.getRemoteDevice(device).createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { deviceState ->
                                    ConnectionResult.TransferSucceded(deviceState)
                                }
                        )
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    Log.e("BluetoothController", "I/O exception: ${e.message}")
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            Log.i("BluetoothController", "Connection closed on flow completion.")
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: ByteArray): Boolean {
        if (!hasAllPermissions()) {
            return false
        }
        if (dataTransferService == null) {
            return false
        }
        dataTransferService?.sendMessage(message)
        return true
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentClientSocket = null
        Log.i("BluetoothController", "Connection closed")
    }

    override fun onDestroy() {
        context.unregisterReceiver(bluetoothAdapterStateReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    @SuppressLint("MissingPermission")
    private fun updatePairedDevices() {
        if (!hasAllPermissions()) return
        Log.i("ScanActivity", "${bluetoothAdapter?.bondedDevices}")
        bluetoothAdapter?.bondedDevices?.onEach { device ->
            _pairedDevices.update {
                val currentDevice = BluetoothDeviceDomain(
                    name = device.name ?: "null",
                    macAddress = device.address,
                    rssi = 0,
                    isScanned = false
                )
                if (currentDevice in pairedDevices.value) {
                    pairedDevices.value
                } else {
                    _pairedDevices.value + currentDevice
                }
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        val perms = if (Build.VERSION.SDK_INT <= 30) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
        perms.forEach { perm ->
            if (context.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
