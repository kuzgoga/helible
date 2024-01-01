package com.helible.pilot

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helible.pilot.components.scannerScreen.BluetoothScannerScreen
import com.helible.pilot.components.deviceScreen.DeviceControlScreen
import com.helible.pilot.viewmodels.AppPreferences
import com.helible.pilot.viewmodels.SavedPreferencesImpl
import com.helible.pilot.components.deviceScreen.defaultDeviceActionsList
import com.helible.pilot.permissions.PermissionsLauncher
import com.helible.pilot.permissions.PermissionsRequest
import com.helible.pilot.permissions.RequestHardwareFeatures
import com.helible.pilot.ui.theme.TestblueTheme
import com.helible.pilot.viewmodels.BluetoothViewModel
import com.helible.pilot.viewmodels.BluetoothViewModelFactory
import com.helible.pilot.viewmodels.PermissionDialogViewModel
import com.helible.pilot.viewmodels.PreferencesViewModel


class MainActivity : ComponentActivity() {
    // TODO: device screen logic
    // TODO: constrain text size
    // TODO: add Bluetooth telemetry...
    // TODO: move text strings to resources
    // TODO: review permissions logic

    private val preferences by lazy {
        SavedPreferencesImpl(getSharedPreferences(packageName, MODE_PRIVATE))
    }
    private val preferencesViewModel by lazy {
        PreferencesViewModel(preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val bluetoothViewModel =
                viewModel<BluetoothViewModel>(factory = BluetoothViewModelFactory(applicationContext))

            val permissionsViewModel = viewModel<PermissionDialogViewModel>()
            val permissionLauncher = PermissionsLauncher()
            permissionLauncher.setup(
                onPermissionResult = { perm, isGranted ->
                    permissionsViewModel.onPermissionResult(perm, isGranted)
                },
                onGranted = { bluetoothViewModel.startScan() }
            )

            val bluetoothState by bluetoothViewModel.state.collectAsState()
            val selectedDevice by bluetoothViewModel.selectedDevice.collectAsState()

            LaunchedEffect(key1 = null) {
                permissionLauncher.launch()
            }


            LaunchedEffect(key1 = bluetoothState) {
                if (bluetoothState.isConnected) {
                    Toast.makeText(applicationContext, "Подключение завершено", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val navController = rememberNavController()

            LaunchedEffect(key1 = bluetoothState.errorMessage) {
                bluetoothState.errorMessage?.let { message ->
                    Toast.makeText(applicationContext, "Ошибка: $message", Toast.LENGTH_LONG).show()
                }
            }

            TestblueTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PermissionsRequest(
                        dismissCurrentDialog = { permissionsViewModel.dismissDialog() },
                        visiblePermissionDialogQueue = permissionsViewModel.visiblePermissionDialogQueue,
                        activity = this,
                        permissionLaunch = { perms -> permissionLauncher.launch(perms) }
                    )

                    RequestHardwareFeatures(
                        activity = this,
                        bluetoothUiState = bluetoothState
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "device"
                    ) {
                        composable("scanner") {
                            BluetoothScannerScreen(
                                bluetoothState = bluetoothState,
                                selectedDevice = selectedDevice,
                                startScan = { bluetoothViewModel.startScan() },
                                cancelScan = { bluetoothViewModel.cancelScan() },
                                choiceDevice = { device -> bluetoothViewModel.selectDevice(device) },
                                onScreenChanged = {
                                    bluetoothViewModel.cancelScan()
                                    val device = selectedDevice
                                    if (device == null) {
                                        preferencesViewModel.clearPreferences()
                                    } else {
                                        preferencesViewModel.savePreferences(
                                            AppPreferences(
                                                deviceName = device.name,
                                                deviceAddress = device.macAddress
                                            )
                                        )
                                    }
                                    navController.navigate("device")
                                    Log.i(
                                        "ScanActivity",
                                        "Preferences: ${preferencesViewModel.preferences}"
                                    )
                                }
                            )
                        }
                        composable("device")
                        {
                            DeviceControlScreen(
                                bluetoothUiState = bluetoothState,
                                getPreferences = { preferencesViewModel.preferences },
                                navigateToPage = { page -> navController.navigate(page) },
                                connectToDevice = { device ->
                                    bluetoothViewModel.connectToDevice(
                                        device
                                    )
                                },
                                disconnectFromDevice = { bluetoothViewModel.disconnectFromDevice() },
                                deviceActionsList = defaultDeviceActionsList()
                            )
                            if (preferencesViewModel.preferences != null) BackHandler {}
                        }
                        composable("console")
                        {

                        }
                        composable("codeblocks")
                        {

                        }
                        composable("imu_calibration")
                        {

                        }
                        composable("motor_test")
                        {

                        }
                        composable("pid_settings")
                        {

                        }
                        composable("reports")
                        {

                        }
                    }
                }
            }
        }
    }
}


