package com.helible.pilot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.helible.pilot.components.BluetoothScannerScreen
import com.helible.pilot.components.PreferencesCacheImpl
import com.helible.pilot.components.SavedPreferences
import com.helible.pilot.ui.theme.TestblueTheme


class MainActivity : ComponentActivity() {
    // TODO: replace field bluetoothDevice in Device to deviceAddress field
    // TODO: share selected device via PersistentViewModel
    // TODO: add stub instead of the DevicesList, if there aren't nearby devices
    // TODO: add Bluetooth data transfer...
    // TODO: add text strings to resource

    private val bluetoothViewModel by lazy {
        BluetoothViewModel(AndroidBluetoothController(applicationContext))
    }

    private var permissionsViewModel = PermissionDialogViewModel()

    private val permissionsToRequest: Array<String> by lazy {
        if (Build.VERSION.SDK_INT <= 30) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
    }

    private val preferencesCache by lazy {
        PreferencesCacheImpl(getSharedPreferences(packageName, Context.MODE_PRIVATE))
    }
    private val preferencesViewModel by lazy {
        PersistentViewModel(preferencesCache)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { perms ->
                    permissionsToRequest.forEach { permission ->
                        permissionsViewModel.onPermissionResult(
                            permission = permission,
                            isGranted = perms[permission] == true
                        )
                    }
                    if(hasAllPermissions() && !bluetoothViewModel.state.value.isDiscovering)
                        bluetoothViewModel.startScan()
                }
            )

            val bluetoothState by bluetoothViewModel.state.collectAsState()
            val selectedDevice by bluetoothViewModel.selectedDevice.collectAsState()

            LaunchedEffect(key1 = null) {
                permissionLauncher.launch(permissionsToRequest)
            }

            LaunchedEffect(key1 = bluetoothState.errorMessage) {
                bluetoothState.errorMessage?.let { message ->
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
            LaunchedEffect(key1 = bluetoothState) {
                if (bluetoothState.isConnected) {
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
                        bluetoothUiState = bluetoothState
                    )

                    NavHost(navController = navController, startDestination = "scanner") {
                        composable("scanner") {
                            BluetoothScannerScreen(
                                bluetoothState = bluetoothState,
                                selectedDevice = selectedDevice,
                                startScan = { bluetoothViewModel.startScan() },
                                cancelScan = { bluetoothViewModel.cancelScan() },
                                choiceDevice = {device -> bluetoothViewModel.selectDevice(device)},
                                onScreenChanged = {
                                    bluetoothViewModel.cancelScan()
                                    val deviceAddress = selectedDevice?.bluetoothDevice?.address
                                    preferencesViewModel.savePreferences(
                                        SavedPreferences(
                                            deviceAddress
                                        )
                                    )
                                    navController.navigate("flight/$deviceAddress")
                                    Log.i(
                                        "ScanActivity",
                                        "Preferences: ${preferencesViewModel.preferences}"
                                    )
                                }
                            )
                        }
                        composable(
                            "flight/{device_address}",
                            arguments = listOf(navArgument("device_address"){type = NavType.StringType})
                        ) {
                            backstackEntry ->
                            LaunchedEffect(Unit) {
                                val device: Device? = selectedDevice
                                if(device == null){
                                    navController.navigate("scanner")
                                } else {
                                    bluetoothViewModel.connectToDevice(device)
                                }
                            }
                            when {
                                bluetoothState.isConnecting -> {
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
                                        text = "Device name: ${backstackEntry.arguments?.getString("device_address")}",
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

    }
    override fun onDestroy() {
        super.onDestroy()
        bluetoothViewModel.onDestroy()
    }


    override fun onStart() {
        super.onStart()
        bluetoothViewModel.startScan()
    }

    override fun onStop() {
        super.onStop()
        if(!hasAllPermissions()) return
        bluetoothViewModel.cancelScan()
        bluetoothViewModel.selectDevice(null)
    }

    private fun hasAllPermissions(): Boolean {
        permissionsToRequest.forEach { perm ->
            if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

}


