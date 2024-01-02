package com.helible.pilot.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RequiredHardwareFeatures(
    title: String,
    description: String,
    confirmButtonText: String,
    featureState: Boolean,
    requestFeature: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!featureState) {
        AlertDialog(
            confirmButton = {
                Divider()
                TextButton(onClick = requestFeature, modifier = Modifier.fillMaxWidth()) {
                    Text(text = confirmButtonText)
                }
            },
            onDismissRequest = onDismissRequest,
            text = {
                Text(
                    text = description
                )
            },
            title = { Text(text = title) }
        )
    }
}

@Preview
@Composable
fun RequiredHardwareFeaturesPreview() {
    RequiredHardwareFeatures(
        title = "Turn on Bluetooth",
        description = "App requires Bluetooth turned on to continue",
        confirmButtonText = "Turn on",
        featureState = false,
        requestFeature = {},
        onDismissRequest = {}
    )
}