package com.helible.pilot.components.console

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.helible.pilot.components.BlankPage
import com.helible.pilot.dataclasses.DeviceStatus
import com.manalkaff.jetstick.JoyStick

@Composable
fun ConsolePage(
    title: String,
    navigateBack: () -> Unit
) {
    BlankPage(title = title, navigateBack = navigateBack) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            JoyStick(
                Modifier.padding(30.dp),
                size = 150.dp,
                dotSize = 30.dp
            ){ x: Float, y: Float ->
                Log.d("JoyStick", "$x, $y")
            }

        }
    }
}
