package com.helible.pilot.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helible.pilot.dataclasses.RotorsDuty
import com.helible.pilot.ui.theme.TestblueTheme

@Composable
fun RotorsTestPage(
    title: String,
    rotorsDuty: RotorsDuty,
    setRotorsDuty: (duty: RotorsDuty) -> Unit,
    startTelemetrySending: () -> Unit,
    stopRotors: () -> Unit,
    navigateBack: () -> Unit,
) {
    BlankPage(title = title, navigateBack = navigateBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LaunchedEffect(null) {
                startTelemetrySending()
            }
            Text(
                text = "R1",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
            Slider(
                value = rotorsDuty.r1.toFloat(),
                onValueChange = { setRotorsDuty(rotorsDuty.copy(r1 = it.toInt().toShort())) },
                valueRange = 0f..5000f,
                steps = 10
            )
            Text(
                text = "При перемещении слайдера вправо ротор 1 должен вращаться против часовой стрелки, если смотреть сверху.",
                textAlign = TextAlign.Center
            )

            Text(
                text = "R2",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
            Slider(
                value = rotorsDuty.r2.toFloat(),
                onValueChange = { setRotorsDuty(rotorsDuty.copy(r2 = it.toInt().toShort())) },
                valueRange = -5000f..5000f,
                steps = 10
            )
            Text(
                text = "При перемещении слайдера вправо ротор 1 должен вращаться по часовой стрелке, если смотреть сверху.",
                textAlign = TextAlign.Center
            )

            Text(
                text = "R3",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
            Slider(
                value = rotorsDuty.r3.toFloat(),
                onValueChange = { setRotorsDuty(rotorsDuty.copy(r3 = it.toInt().toShort())) },
                valueRange = 0f..5000f,
                steps = 10
            )
            Text(
                text = "При отклонении слайдера вправо от центра ротор 1 должен вращаться по часовой стрелке, а при отклонении влево - против часовой, если смотреть сверху.",
                textAlign = TextAlign.Center
            )
            FloatingActionButton(
                onClick = { stopRotors() },
                containerColor = Color(245, 47, 7),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "СТОП",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RotorsTestPagePreview() {
    TestblueTheme {
        RotorsTestPage(
            title = "Тестирование моторов",
            rotorsDuty = RotorsDuty(5, 5, 5),
            setRotorsDuty = { _ -> },
            startTelemetrySending = {},
            stopRotors = {},
            navigateBack = { /*TODO*/ }
        )
    }
}