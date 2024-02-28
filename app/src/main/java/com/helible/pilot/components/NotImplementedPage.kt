package com.helible.pilot.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helible.pilot.components.BlankPage

@Composable
fun NotImplementedPage(title: String, navigateBack: () -> Unit) {
    BlankPage(title = title, navigateBack = navigateBack) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {
            Text(text = "Эта страница пока не готова и находится на стадии разработки")
        }
    }
}