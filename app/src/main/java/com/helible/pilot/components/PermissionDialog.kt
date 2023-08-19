package com.helible.pilot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onContinueClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Требуется разрешение") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider()
                Text(
                    text = if (isPermanentDeclined) {
                        "Выдать разрешение"
                    } else {
                        "OK"
                    },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isPermanentDeclined) {
                            onGoToAppSettingsClick()
                        } else {
                            onOkClick()
                        }
                    }
                    .padding(16.dp)
                )
            }
        },
        dismissButton = {if(isPermanentDeclined)
                Box(modifier=Modifier.fillMaxWidth()){
                    Divider()
                    Text(
                    text = "Снова проверить наличие разрешения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues = PaddingValues(top=10.dp))
                        .clickable {onContinueClick()},
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )}
            else
                Unit},
        text = {
            Text(text = permissionTextProvider.getDescription(
                isPermanentDeclined = isPermanentDeclined
            ))
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getDescription(isPermanentDeclined: Boolean): String
}

class LocationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentDeclined: Boolean): String {
        return if (isPermanentDeclined){
            "Похоже вы навсегда запретили приложению доступ к геолокации. " +
                    "Вы можете зайти в настройки, чтобы выдать это разрешение."
        } else {
            "Приложению необходимо разрешение для определения местоположения " +
             "для работы с Bluetooth на устройствах с Android 11 и ниже."
        }
    }
}

class BluetoothScanPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentDeclined: Boolean): String {
        return if (isPermanentDeclined){
        "Похоже вы навсегда запретили приложению доступ к сканированию по Bluetooth. " +
                "Вы можете зайти в настройки, чтобы выдать это разрешение."
        } else {
            "Приложению необходимо разрешение для к сканированию по Bluetooth " +
                    "для работы с Bluetooth на устройствах с Android 11 и ниже"
        }
    }
}

class BluetoothConnectPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentDeclined: Boolean): String {
        return if (isPermanentDeclined){
            "Похоже вы навсегда запретили приложению доступ к подключению по Bluetooth." +
                    "Вы можете зайти в настройки, чтобы выдать это разрешение."
        } else {
            "Приложению необходимо разрешение для к подключению по Bluetooth." +
                    "для работы с Bluetooth на устройствах с Android 11 и ниже"
        }
    }
}

class BluetoothAdminPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentDeclined: Boolean): String {
        return if (isPermanentDeclined){
            "Похоже вы навсегда запретили приложению доступ к управлению настройками Bluetooth. " +
                    "Вы можете зайти в настройки, чтобы выдать это разрешение."
        } else {
            "Приложению необходимо разрешение для к управлению настройками Bluetooth " +
                    "для работы с Bluetooth на устройствах с Android 11 и ниже"
        }
    }
}