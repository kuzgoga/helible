package com.helible.pilot

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.helible.pilot.components.RequiredHardwareFeatures

@SuppressLint("MissingPermission")
@Composable
fun RequestHardwareFeatures(
    activity: Activity,
    bluetoothUiState: BluetoothUiState
)
{
    RequiredHardwareFeatures(
        title = "Включите Bluetooth",
        description = "Для работы приложения требуется Bluetooth",
        confirmButtonText = "Включить Bluetooth",
        featureState = bluetoothUiState.isEnabled,
        requestFeature = {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivity(intent)
        },
        onDismissRequest = {}
    )

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
        RequiredHardwareFeatures(
            title = "Пожалуйста, включите геолокацию",
            description = "Для работы с Bluetooth на устройствах с Android 11 и более ранних версиях, " +
                    "требуется геолокация.",
            confirmButtonText = "Включить геолокацию",
            featureState = bluetoothUiState.isLocationEnabled,
            requestFeature = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        ) {}
    }
}