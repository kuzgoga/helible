package com.helible.pilot.components

import android.widget.Spinner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.R
import com.helible.pilot.dataclasses.BluetoothUiState
import com.helible.pilot.dataclasses.ChangedDeviceStatus
import com.helible.pilot.dataclasses.DeviceState
import com.helible.pilot.dataclasses.DeviceStatus

@Composable
fun CalibrationPage(
    deviceStatus: DeviceStatus?,
    title: String,
    startCalibration: () -> Unit,
    navigateBack: () -> Unit
) {
    BlankPage(title = title, navigateBack = navigateBack) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = LocalContext.current.getString(R.string.calibration_description),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            Button(
                enabled = deviceStatus != DeviceStatus.IsImuCalibration,
                onClick = startCalibration,
                modifier = Modifier.padding(10.dp)
            ) {
                if(deviceStatus != DeviceStatus.IsImuCalibration) {
                    Icon(
                        painter = painterResource(id = R.drawable.tune),
                        contentDescription = null,
                        modifier = Modifier.padding(3.dp)
                    )
                    Text(
                        text = "Начать калибровку"
                    )
                } else {
                    CircularProgressIndicator ()
                    Text(
                        text = "Калибровка...",
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CalibrationPagePreview() {
    Surface {
        CalibrationPage(
            DeviceStatus.IsImuCalibration,
            title = "Калибровка гироскопа и акселерометра",
            startCalibration = {},
            navigateBack = {}
        )
    }
}