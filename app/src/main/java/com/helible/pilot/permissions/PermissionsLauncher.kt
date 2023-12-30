package com.helible.pilot.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable


class PermissionsLauncher {
    private val permissionsToRequest: Array<String> by lazy {
        if (Build.VERSION.SDK_INT <= 30) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
    }

    private lateinit var launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    @SuppressLint("ComposableNaming")
    @Composable
    fun setup(
        onPermissionResult: (permission: String, isGranted: Boolean) -> Unit,
        onGranted: () -> Unit,
    ) {
        launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                permissionsToRequest.forEach { permission ->
                    onPermissionResult(permission, perms[permission] == true)
                }
                if (perms.values.all { it }) {
                    onGranted()
                }
            }
        )
    }

    fun launch(permissions: Array<String> = permissionsToRequest) {
        launcher.launch(permissions)
    }
}