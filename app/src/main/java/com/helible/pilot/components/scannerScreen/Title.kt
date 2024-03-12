package com.helible.pilot.components.scannerScreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 23.sp,
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold
    )
}