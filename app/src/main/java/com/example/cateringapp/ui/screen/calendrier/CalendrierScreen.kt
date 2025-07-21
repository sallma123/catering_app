package com.example.cateringapp.ui.screen.calendrier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendrierScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Écran Calendrier",
            fontSize = 20.sp,
            color = Color(0xFF03DAC5)
        )
    }
}
