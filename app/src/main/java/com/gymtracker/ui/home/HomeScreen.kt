package com.gymtracker.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(padding: PaddingValues, onGroupClick: (Long) -> Unit) {
    Box(Modifier.padding(padding)) { Text("Home") }
}
