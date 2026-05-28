package com.gymtracker.ui.exercises

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExerciseDetailScreen(exerciseId: Long, onBack: () -> Unit) {
    Box { Text("Exercise $exerciseId") }
}
