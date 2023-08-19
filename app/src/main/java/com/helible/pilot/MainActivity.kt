package com.helible.pilot

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helible.pilot.components.BluetoothScannerScreen
import com.helible.pilot.components.PreferencesCacheImpl
import com.helible.pilot.components.SavedPreferences
import com.helible.pilot.ui.theme.TestblueTheme
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    // TODO: delegate part of Intent filters logic to BluetoothController
    // TODO: move bluetooth states and stateFlow to new BluetoothViewModel
    // TODO: replace field bluetoothDevice in Device to deviceAddress field
    // TODO: replace some mutableStates to stateFlows
    // TODO: share selected device via PersistentViewModel
    // TODO: check permissions inside other classes (and throw an exception, if one of this isn't granted)
    // TODO: add stub instead of the DevicesList, if there aren't nearby devices
    // TODO: add Bluetooth data transfer...
    // TODO: add text strings to resource
    val mainViewModel: MainViewModel = MainViewModel(AndroidBluetoothController())

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    private var permissionsViewModel = PermissionDialogViewModel()
    private lateinit var permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    private val permissionsToRequest: Array<String> by lazy {
        if (Build.VERSION.SDK_INT <= 30) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    private val locationManager: LocationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }
    private val preferencesCache by lazy {
        PreferencesCacheImpl(getSharedPreferences(packageName, Context.MODE_PRIVATE))
    }
    private val preferencesViewModel by lazy {
        PersistentViewModel(preferencesCache)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.bluetoothTurnOnState.value = bluetoothAdapter?.isEnabled
        mainViewModel.locationTurnOnState.value =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        setContent {
            this.permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { perms ->
                    permissionsToRequest.forEach { permission ->
                        permissionsViewModel.onPermissionResult(
                            permission = permission,
                            isGranted = perms[permission] == true
                        )
                    }
                }
            )

            val state by mainViewModel.state.collectAsState()

            LaunchedEffect(key1 = state.errorMessage) {
                state.errorMessage?.let { message ->
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
            LaunchedEffect(key1 = state) {
                if (state.isConnected) {
                    Toast.makeText(applicationContext, "Подключение завершено", Toast.LENGTH_LONG)
                        .show()
                }
            }

            val navController = rememberNavController()
            TestblueTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PermissionsRequest(
                        dismissCurrentDialog = { permissionsViewModel.dismissDialog() },
                        visiblePermissionDialogQueue = permissionsViewModel.visiblePermissionDialogQueue,
                        activity = this,
                        permissionLauncher = permissionLauncher
                    )

                    RequestHardwareFeatures(
                        activity = this,
                        turnOnLocation = Manifest.permission.ACCESS_FINE_LOCATION in permissionsToRequest,
                        bluetoothTurnOnState = mainViewModel.bluetoothTurnOnState,
                        locationTurnOnState = mainViewModel.locationTurnOnState
                    )

                    NavHost(navController = navController, startDestination = "scanner") {
                        composable("scanner") {
                            BluetoothScannerScreen(
                                devices = mainViewModel.devices,
                                selectedDevice = mainViewModel.selectedDevice,
                                bluetoothIsDiscoveringState = mainViewModel.isBluetoothDiscoveryRunning,
                                bluetoothAdapter = bluetoothAdapter,
                                onScreenChanged = {
                                    bluetoothAdapter?.cancelDiscovery()
                                    preferencesViewModel.savePreferences(
                                        SavedPreferences(
                                            mainViewModel.selectedDevice.value?.bluetoothDevice?.address
                                        )
                                    )
                                    navController.navigate("flight")
                                    Log.i(
                                        "ScanActivity",
                                        "Preferences: ${preferencesViewModel.preferences}"
                                    )
                                }
                            )
                        }
                        composable("flight") {

                            LaunchedEffect(Unit) {
                                // TODO: refactor
                                val device: Device = mainViewModel.selectedDevice.value!!
                                mainViewModel.connectToDevice(device)
                            }
                            when {
                                state.isConnecting -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator()
                                        Text(text = "Подключение...", textAlign = TextAlign.Center)
                                    }
                                }

                                else -> {
                                    Text(
                                        text = "Device name: ${mainViewModel.selectedDevice.value?.bluetoothDevice?.name}",
                                        modifier = Modifier.fillMaxSize(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        registerIntentFilters(this, receiver)
        requestPermissions()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            receiveIntentChanges(
                intent,
                mainViewModel,
                bluetoothAdapter,
                locationManager
            )
        }
    }

    private fun requestPermissions() {
        Executors.newSingleThreadExecutor().execute {
            Handler(Looper.getMainLooper()).post {
                permissionLauncher.launch(permissionsToRequest)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
        Log.i("ScanActivity", "ACTIVITY DESTROYED")
        bluetoothAdapter?.cancelDiscovery()
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.e(
                "ScanActivity",
                "Receiver wasn't registered ${e.localizedMessage}\nStackTrace: ${e.stackTrace}"
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (bluetoothAdapter?.isDiscovering != true)
            bluetoothAdapter?.startDiscovery()
        Log.i("ScanActivity", "ACTIVITY STARTED")
    }

    override fun onStop() {
        super.onStop()
        bluetoothAdapter?.cancelDiscovery()
        mainViewModel.devices.clear()
        mainViewModel.selectedDevice.value = null
        Log.i("ScanActivity", "ACTIVITY STOPPED")
    }
}


