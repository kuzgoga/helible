package com.helible.pilot

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.helible.pilot.components.RequiredHardwareFeatures

@SuppressLint("MissingPermission")
@Composable
fun RequestHardwareFeatures(
    activity: Activity,
    turnOnLocation: Boolean,
    bluetoothTurnOnState: MutableState<Boolean?>,
    locationTurnOnState: MutableState<Boolean?>
)
{
    RequiredHardwareFeatures(
        title = "Включите Bluetooth",
        description = "Для работы приложения требуется Bluetooth",
        confirmButtonText = "Включить Bluetooth",
        featureState = bluetoothTurnOnState,
        requestFeature = {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivity(intent)
        },
        onDismissRequest = {}
    )

    if (turnOnLocation) {
        RequiredHardwareFeatures(
            title = "Пожалуйста, включите геолокацию",
            description = "Для работы с Bluetooth на устройствах с Android 11 и более ранних версиях, " +
                    "требуется геолокация.",
            confirmButtonText = "Включить геолокацию",
            featureState = locationTurnOnState,
            requestFeature = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        ) {}
    }
}