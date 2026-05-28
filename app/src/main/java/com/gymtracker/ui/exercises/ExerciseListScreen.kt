package com.gymtracker.ui.exercises

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExerciseListScreen(padding: PaddingValues, groupId: Long, onExerciseClick: (Long) -> Unit) {
    Box(Modifier.padding(padding)) { Text("Exercises for $groupId") }
}
