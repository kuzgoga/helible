package com.helible.pilot.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlankPage(title: String, navigateBack: () -> Unit, block: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
        block()
    }
}

@Preview
@Composable
fun BlankPagePreview() {
    Surface {
        BlankPage(title = "Blank page", navigateBack = {}) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)) {
                Text(text = "This page in development")
            }
        }
    }
}
