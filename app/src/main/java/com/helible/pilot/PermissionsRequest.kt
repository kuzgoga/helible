package com.helible.pilot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.helible.pilot.components.BluetoothAdminPermissionTextProvider
import com.helible.pilot.components.BluetoothConnectPermissionTextProvider
import com.helible.pilot.components.BluetoothScanPermissionTextProvider
import com.helible.pilot.components.LocationPermissionTextProvider
import com.helible.pilot.components.PermissionDialog

@Composable
fun PermissionsRequest(
    visiblePermissionDialogQueue: SnapshotStateList<String>,
    dismissCurrentDialog: () -> Unit,
    activity: Activity,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    /* Create Dialog windows, which requests all permissions */
    visiblePermissionDialogQueue.reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        LocationPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_SCAN -> {
                        BluetoothScanPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_CONNECT -> {
                        BluetoothConnectPermissionTextProvider()
                    }

                    Manifest.permission.BLUETOOTH_ADMIN -> {
                        BluetoothAdminPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentDeclined = !activity.shouldShowRequestPermissionRationale(permission),
                onDismiss = {
                    if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                        dismissCurrentDialog()
                },
                onOkClick = {
                    dismissCurrentDialog()
                    permissionLauncher.launch(arrayOf(permission))
                },
                onContinueClick = {
                    if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                        dismissCurrentDialog()
                },
                onGoToAppSettingsClick = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null)
                    )
                    activity.startActivity(intent)
                }
            )
        }
}