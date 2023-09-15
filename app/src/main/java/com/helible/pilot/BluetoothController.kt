package com.helible.pilot

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
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID

sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult
    data class Error(val message: String) : ConnectionResult
}

interface BluetoothController {
    val isEnabled: StateFlow<Boolean>
    val isLocationEnabled: StateFlow<Boolean>
    val isConnected: StateFlow<Boolean>
    val isScanning: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<Device>>
    val pairedDevices: StateFlow<Set<BluetoothDevice>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun cancelDiscovery()
    fun connectToDevice(device: Device?): Flow<ConnectionResult>
    fun closeConnection()
    fun onDestroy()
}

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

    private val _pairedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    override val pairedDevices: StateFlow<Set<BluetoothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _scannedDevices: MutableStateFlow<List<Device>> = MutableStateFlow(emptyList())
    override val scannedDevices: StateFlow<List<Device>>
        get() = _scannedDevices.asStateFlow()

    private var currentClientSocket: BluetoothSocket? = null


    @SuppressLint("MissingPermission")
    private val bluetoothIntentReceiver = BluetoothIntentReceiver(
        onDeviceFound = {device, rssi ->
            if(!hasAllPermissions()) return@BluetoothIntentReceiver
            val newDevice = Device(device, rssi)
            _scannedDevices.update { devices ->
                if(newDevice in devices) devices else devices + newDevice
            }
            Log.i(
                "ScanActivity",
                "Found new device: ${device.name} ${device.address} $rssi"
            )
        },
        onBluetoothEnabledChanged = { isEnabled ->
            _isEnabled.update { _ -> isEnabled }
            startDiscovery()
            Log.i("ScanActivity", "Bluetooth enabled status: $isEnabled")
        },
        onDiscoveryRunningChanged = { isDiscovering ->
            _isScanning.update { isDiscovering }
        },
        onLocationEnabledChanged = {
            if(locationManager?.isLocationEnabled == true){
                _isLocationEnabled.update { true }
            } else {
                _isLocationEnabled.update { false }
            }
        }
    )

    companion object {
        const val SERVICE_UUID = "af7cc14b-cffa-4a3d-b677-01b0ff0a93d7"
    }

    init {
        updatePairedDevices()
        _isEnabled.update { bluetoothAdapter.isEnabled }
        _isLocationEnabled.update { locationManager?.isLocationEnabled == true }
        context.registerReceiver(bluetoothIntentReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(bluetoothIntentReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context.registerReceiver(bluetoothIntentReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        context.registerReceiver(bluetoothIntentReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            context.registerReceiver(bluetoothIntentReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        }
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if(!hasAllPermissions()) {
            Toast.makeText(context, "Ошибка: недостаточно разрешений", Toast.LENGTH_SHORT).show()
            return
        }
        if(!_isEnabled.value) {
            return
        }
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if(locationManager?.isLocationEnabled != true) return
        }

        updatePairedDevices()
        _scannedDevices.update { emptyList() }

        if(!bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancelDiscovery() {
        if(!hasAllPermissions()) return
        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: Device?): Flow<ConnectionResult> {
        if(!hasAllPermissions()){
            Toast.makeText(context, "Ошибка: нет разрешений", Toast.LENGTH_SHORT).show()
            return flow {}
        }
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

    override fun onDestroy() {
        context.unregisterReceiver(bluetoothIntentReceiver)
        closeConnection()
    }

    @SuppressLint("MissingPermission")
    private fun updatePairedDevices() {
        if(!hasAllPermissions()) return
        bluetoothAdapter?.bondedDevices.also { devices ->
            if(devices != null) {
                _pairedDevices.update { devices }
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
            if(context.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }
}
