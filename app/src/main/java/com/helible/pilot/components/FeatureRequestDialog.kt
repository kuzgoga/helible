package com.helible.pilot.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

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
