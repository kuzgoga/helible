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
import com.helible.pilot.components.CalibrationPage
import com.helible.pilot.components.NotImplementedPage
import com.helible.pilot.components.RotorsTestPage
import com.helible.pilot.components.console.ConsolePage
import com.helible.pilot.components.deviceScreen.DeviceControlScreen
import com.helible.pilot.components.deviceScreen.defaultDeviceActionsList
import com.helible.pilot.components.pidSettings.PidSettingsPage
import com.helible.pilot.components.scannerScreen.ScannerScreen
import com.helible.pilot.permissions.PermissionsLauncher
import com.helible.pilot.permissions.PermissionsRequest
import com.helible.pilot.permissions.RequestHardwareFeatures
import com.helible.pilot.ui.theme.TestblueTheme
import com.helible.pilot.viewmodels.AppPreferences
import com.helible.pilot.viewmodels.BluetoothViewModel
import com.helible.pilot.viewmodels.BluetoothViewModelFactory
import com.helible.pilot.viewmodels.PermissionDialogViewModel
import com.helible.pilot.viewmodels.PreferencesViewModel
import com.helible.pilot.viewmodels.SavedPreferencesImpl


class MainActivity : ComponentActivity() {
    // TODO: move text strings to resources

    private val preferences by lazy {
        SavedPreferencesImpl(getSharedPreferences(packageName, MODE_PRIVATE))
    }
    private val preferencesViewModel by lazy {
        PreferencesViewModel(preferences)
    }

    @ExperimentalStdlibApi
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
            val rotorsDuty by bluetoothViewModel.rotorsDuty.collectAsState()

            LaunchedEffect(key1 = null) {
                permissionLauncher.launch()
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
                            ScannerScreen(
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
                                disconnectFromDevice = {
                                    preferencesViewModel.clearPreferences()
                                    bluetoothViewModel.disconnectFromDevice()
                                },
                                deviceActionsList = defaultDeviceActionsList()
                            )
                            if (preferencesViewModel.preferences != null) BackHandler {}
                        }
                        composable("console/{title}")
                        { backStackEntry ->
                            ConsolePage(
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("codeblocks/{title}")
                        { backStackEntry ->
                            NotImplementedPage(
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("imu_calibration/{title}")
                        { backStackEntry ->
                            CalibrationPage(
                                deviceStatus = bluetoothState.deviceState?.status,
                                startCalibration = { bluetoothViewModel.startImuCalibration() },
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("motor_test/{title}")
                        { backStackEntry ->
                            RotorsTestPage(
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                rotorsDuty = rotorsDuty,
                                setRotorsDuty = { bluetoothViewModel.setRotorsDuty(it) },
                                startTelemetrySending = { bluetoothViewModel.startRotorsConfigurationTelemetry() },
                                stopRotors = { bluetoothViewModel.stopRotors() },
                                navigateBack = {
                                    navController.popBackStack()
                                    bluetoothViewModel.stopRotorsConfigurationTelemetry()
                                    bluetoothViewModel.stopRotors()
                                }
                            )
                            BackHandler {
                                navController.popBackStack()
                                bluetoothViewModel.stopRotorsConfigurationTelemetry()
                                bluetoothViewModel.stopRotors()
                            }
                        }
                        composable("pid_settings/{title}")
                        { backStackEntry ->
                            PidSettingsPage(
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                navigateBack = {
                                    navController.popBackStack()
                                    bluetoothViewModel.clearPidSettings()
                                },
                                requestPidSettings = { bluetoothViewModel.requestPidSettings() },
                                setPidSettings = {settings -> bluetoothViewModel.applyPidSettings(settings)},
                                deviceState = bluetoothState.deviceState
                            )
                            BackHandler {
                                navController.popBackStack()
                                bluetoothViewModel.clearPidSettings()
                            }
                        }
                        composable("reports/{title}")
                        { backStackEntry ->
                            NotImplementedPage(
                                title = backStackEntry.arguments?.getString("title") ?: "null",
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


