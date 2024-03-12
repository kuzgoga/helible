package com.helible.pilot.components.console

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.R
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.DeviceStatus
import com.manalkaff.jetstick.JoyStick

@Composable
fun ConsolePage(
    startTakeoff: () -> Unit,
    startOnboarding: () -> Unit,
    stop: () -> Unit,
    reconnect: () -> Unit,
    changeStick1Position: (x: Int, y: Int) -> Unit,
    changeStick2Position: (x: Int, y: Int) -> Unit,
    bluetoothUiState: BluetoothUiState,
    navigateBack: () -> Unit,
) {
    val deviceState = bluetoothUiState.deviceState
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    LaunchedEffect(key1 = null) {
        Log.i("Console", "state: ${bluetoothUiState.deviceState}, isConnected: ${bluetoothUiState.isConnected}")
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (bluetoothUiState.isConnected && deviceState != null) {
                Text("Высота полёта: ${deviceState.flightHeight / 100} м; ")
                Text("Рыскание: ${deviceState.yaw}°; ")
                Text("Тангаж: ${deviceState.pitch}°; ")
                Text("Крен: ${deviceState.roll}°; ")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            JoyStick(
                size = 200.dp,
                dotSize = 80.dp,
                backgroundImage = R.drawable.stick_background,
                dotImage = R.drawable.stick_dot,
                modifier = Modifier.padding(30.dp)
            ) { x: Float, y: Float ->
                changeStick1Position(x.toInt(), y.toInt())
                Log.d("JoyStick", "$x, $y")
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bluetoothUiState.isConnected && deviceState != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (deviceState.status == DeviceStatus.IsPreparingForTakeoff) {
                            CircularProgressIndicator(modifier = Modifier.padding(7.dp))
                        }
                        Text(text = describeStatus(deviceState.status))
                    }
                    Text(text = "Заряд батареи: ${deviceState.batteryCharge}%")
                } else {
                    Text(text = "Нет соединения с устройством")
                }

                if (bluetoothUiState.isConnecting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(5.dp))
                        Text(text = "Подключение...")
                    }
                } else if (bluetoothUiState.isConnected) {
                    Button(
                        onClick = startTakeoff,
                        enabled = deviceState?.status == DeviceStatus.Idle
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                            Text(text = "Взлёт")
                        }
                    }
                    Button(onClick = stop, colors = ButtonDefaults.buttonColors(Color.Red)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Text(text = "СТОП")
                        }
                    }
                    Button(
                        onClick = startOnboarding,
                        enabled = deviceState?.status in listOf(
                            DeviceStatus.IsFlying, DeviceStatus.IsPreparingForTakeoff
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            Text(text = "Посадка")
                        }
                    }
                } else {
                    Button(onClick = reconnect) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Text(text = "Попробовать ещё раз")
                        }
                    }
                }

                if(!bluetoothUiState.isEnabled || deviceState?.status in listOf(DeviceStatus.ChargeRequired, DeviceStatus.Idle, null)) {
                    Button(
                        onClick = navigateBack
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                            Text(text = "Выход")
                        }
                    }
                }
            }

            JoyStick(
                size = 200.dp,
                dotSize = 80.dp,
                backgroundImage = R.drawable.stick_background,
                dotImage = R.drawable.stick_dot,
            ) { x: Float, y: Float ->
                changeStick2Position(x.toInt(), y.toInt())
                Log.d("JoyStick", "$x, $y")
            }
        }
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun describeStatus(deviceState: DeviceStatus): String {
    return when (deviceState) {
        DeviceStatus.Idle -> {
            "Устройство заряжено, бездействует"
        }

        DeviceStatus.IsFlying -> {
            "В полёте"
        }

        DeviceStatus.ChargeRequired -> {
            "Устройство разряжено. Дальнейшие полёты невозможны"
        }

        DeviceStatus.IsImuCalibration -> {
            "Калибровка гироскопа и акселерометра. Пожалуйста, не двигайте устройство."
        }

        DeviceStatus.IsPreparingForTakeoff -> {
            "Подготовка ко взлёту..."
        }

        DeviceStatus.IsBoarding -> {
            "Посадка"
        }
    }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun ConsolePreview() {
    ConsolePage(
        startTakeoff = {},
        startOnboarding = {},
        stop = {},
        changeStick1Position = { _: Int, _: Int -> },
        changeStick2Position = { _: Int, _: Int -> },
        bluetoothUiState = BluetoothUiState().copy(
            isConnected = false,
            deviceState = null
        ),
        reconnect = {},
        navigateBack = {}
    )
}