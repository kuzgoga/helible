package com.helible.pilot.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helible.pilot.dataclasses.AlarmStateMessage
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.RotorsSpeedMessage
import kotlin.math.roundToInt


@Composable
fun FlightControlScreen(
    bluetoothUiState: BluetoothUiState,
    getPreferences: () -> AppPreferences?,
    navigateToScanner: () -> Unit,
    connectToDevice: (String) -> Unit,
    disconnectFromDevice: () -> Unit,
    sendRotorsState: (RotorsSpeedMessage) -> Unit,
    sendAlarm: (AlarmStateMessage) -> Unit,
    sendEmergStop: () -> Unit,
    sendR3Duty: (Int) -> Unit,
) {
    LaunchedEffect(Unit) {
        val preferences: AppPreferences? = getPreferences()
        if (preferences == null) {
            navigateToScanner()
        } else {
            connectToDevice(preferences.deviceAddress)
        }
    }

    var rotor1Duty by remember { mutableStateOf(0f) }
    var rotor2Duty by remember { mutableStateOf(0f) }
    var rotor3Duty by remember { mutableStateOf(0f) }

    BackHandler {
        disconnectFromDevice()
        Log.i("FlightScreen", "Disconnected from the device")
        navigateToScanner()
    }
    when {
        bluetoothUiState.isConnecting -> {
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

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Device name: ${getPreferences()?.deviceName ?: "(устройство отключено)"}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(text = "Rotor 1 value: $rotor1Duty", textAlign = TextAlign.Center)
                Slider(
                    value = rotor1Duty,
                    onValueChange = { rotor1Duty = it.roundToInt().toFloat() },
                    valueRange = 0f..1000f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                Text(text = "Rotor 2 value: $rotor2Duty", textAlign = TextAlign.Center)
                Slider(
                    value = rotor2Duty,
                    onValueChange = { rotor2Duty = it.roundToInt().toFloat() },
                    valueRange = 0f..1000f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                Text(text = "Rotor 3 value: $rotor1Duty", textAlign = TextAlign.Center)
                Slider(
                    value = rotor3Duty,
                    onValueChange = { rotor3Duty = it.roundToInt().toFloat() },
                    valueRange = 0f..1000f,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                FilledIconButton(
                    onClick = { sendEmergStop() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.padding(3.dp)
                    )
                    Text(
                        text = "СТОП",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(3.dp)
                    )
                }
            }
        }
    }
}
