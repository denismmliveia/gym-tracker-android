package com.gymtracker.ui.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExerciseProgressScreen(exerciseId: Long, onBack: () -> Unit) {
    Box { Text("Progress for $exerciseId") }
}
