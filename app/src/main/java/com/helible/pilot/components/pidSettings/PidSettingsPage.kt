package com.helible.pilot.components.pidSettings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.helible.pilot.R
import com.helible.pilot.components.BlankPage
import com.helible.pilot.dataclasses.DeviceState
import com.helible.pilot.dataclasses.DeviceStatus
import com.helible.pilot.dataclasses.PidParams
import com.helible.pilot.dataclasses.PidSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PidSettingsPage(
    title: String,
    navigateBack: () -> Unit,
    requestPidSettings: () -> Unit,
    setPidSettings: (PidSettings) -> Unit,
    deviceState: DeviceState?,
) {
    BlankPage(title = title, navigateBack = navigateBack) {
        var pValue by remember { mutableStateOf("") }
        var iValue by remember { mutableStateOf("") }
        var dValue by remember { mutableStateOf("") }
        var selectedRegulator by remember { mutableStateOf("") }
        val dropdownMenuItems =
            listOf("Контроллер высоты", "Контроллер крена", "Контроллер рысканья")

        LaunchedEffect(null) {
            requestPidSettings()
        }
        LaunchedEffect(deviceState?.pidSettings) {
            if (deviceState?.pidSettings != null) {
                val pidSettings = deviceState.pidSettings
                when (selectedRegulator) {
                    dropdownMenuItems[0] -> {
                        pidSettings.heightControllerParams.p.toString().also { pValue = it }
                        pidSettings.heightControllerParams.i.toString().also { iValue = it }
                        pidSettings.heightControllerParams.d.toString().also { dValue = it }
                    }

                    dropdownMenuItems[1] -> {
                        pidSettings.yawControllerParams.p.toString().also { pValue = it }
                        pidSettings.yawControllerParams.i.toString().also { iValue = it }
                        pidSettings.yawControllerParams.d.toString().also { dValue = it }
                    }

                    dropdownMenuItems[2] -> {
                        pidSettings.pitchControllerParams.p.toString().also { pValue = it }
                        pidSettings.pitchControllerParams.i.toString().also { iValue = it }
                        pidSettings.pitchControllerParams.d.toString().also { dValue = it }
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        )
        {
            if (deviceState?.status != DeviceStatus.Idle) {
                Text(
                    text = "Этот раздел доступен только с подключенным заряженным бездействующим устройством.",
                    textAlign = TextAlign.Center
                )
            } else if (deviceState.pidSettings == null) {
                CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                Text(text = "Синхронизация...")
            } else {
                val pidSettings = deviceState.pidSettings

                Column(modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 10.dp)) {
                    Text(
                        "Рекомендации по настройке ПИД регуляторов",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    )
                    Text(
                        text = LocalContext.current.getString(R.string.p_pid_value_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = LocalContext.current.getString(R.string.i_pid_value_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = LocalContext.current.getString(R.string.d_pid_value_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedDropdownMenu(
                    label = "ПИД регулятор",
                    suggestions = dropdownMenuItems,
                    onChange = { selected ->
                        Log.i("BluetoothVM", selected)
                        when (dropdownMenuItems.indexOf(selected)) {
                            0 -> {
                                selectedRegulator = dropdownMenuItems[0]
                                pidSettings.heightControllerParams.p.toString().also { pValue = it }
                                pidSettings.heightControllerParams.i.toString().also { iValue = it }
                                pidSettings.heightControllerParams.d.toString().also { dValue = it }
                            }

                            1 -> {
                                selectedRegulator = dropdownMenuItems[1]
                                pidSettings.yawControllerParams.p.toString().also { pValue = it }
                                pidSettings.yawControllerParams.i.toString().also { iValue = it }
                                pidSettings.yawControllerParams.d.toString().also { dValue = it }
                            }

                            2 -> {
                                selectedRegulator = dropdownMenuItems[2]
                                pidSettings.pitchControllerParams.p.toString().also { pValue = it }
                                pidSettings.pitchControllerParams.i.toString().also { iValue = it }
                                pidSettings.pitchControllerParams.d.toString().also { dValue = it }
                            }
                        }
                    },
                    modifier = Modifier.padding(10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    OutlinedTextField(
                        value = pValue,
                        onValueChange = { pValue = it },
                        label = { Text(text = "P") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = iValue,
                        onValueChange = { iValue = it },
                        label = { Text(text = "I") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = dValue,
                        onValueChange = { dValue = it },
                        label = { Text(text = "D") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                }
                val p = pValue
                val i = iValue
                val d = dValue
                Button(
                    onClick = {
                        when (selectedRegulator) {
                            dropdownMenuItems[0] -> {
                                val newPidSettings = pidSettings.copy(
                                    heightControllerParams = PidParams(
                                        p.toFloat(),
                                        i.toFloat(),
                                        d.toFloat()
                                    )
                                )
                                setPidSettings(newPidSettings)
                            }

                            dropdownMenuItems[1] -> {
                                val newPidSettings = pidSettings.copy(
                                    yawControllerParams = PidParams(
                                        p.toFloat(),
                                        i.toFloat(),
                                        d.toFloat()
                                    )
                                )
                                setPidSettings(newPidSettings)
                            }

                            dropdownMenuItems[2] -> {
                                val newPidSettings = pidSettings.copy(
                                    pitchControllerParams = PidParams(
                                        p.toFloat(),
                                        i.toFloat(),
                                        d.toFloat()
                                    )
                                )
                                setPidSettings(newPidSettings)
                            }
                        }
                    },
                    enabled = isValidValue(p) && isValidValue(i) && isValidValue(d)
                ) {
                    Text(text = "Применить")
                }
            }
        }
    }
}

private fun isValidValue(k: String): Boolean {
    return k.toFloatOrNull() != null && k.toFloat() >= 0f && k.toFloat() <= 15f
}

@Preview(showBackground = true)
@Composable
fun PidSettingsPreview() {
    PidSettingsPage(
        title = "Настройки ПИД регуляторов",
        navigateBack = { },
        requestPidSettings = { },
        setPidSettings = {},
        deviceState = DeviceState(
            status = DeviceStatus.Idle,
            pidSettings = PidSettings(
                PidParams(1f, 1f, 1f),
                PidParams(1f, 1f, 1f),
                PidParams(1f, 1f, 1f)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DropdownDemo() {
    OutlinedDropdownMenu(label = "", suggestions = listOf("A", "B"), onChange = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedDropdownMenu(
    label: String,
    suggestions: List<String>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(suggestions.first()) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown


    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                selectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to the DropDown the same width
                    textfieldSize = coordinates.size.toSize()
                },
            label = { Text(label) },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
        ) {
            suggestions.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedText = label
                    expanded = false
                    onChange(selectedText)
                },
                    text = { Text(label) }
                )
            }
        }
    }
}