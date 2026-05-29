package com.gymtracker.ui.exercises

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    padding: PaddingValues,
    groupId: Long,
    onExerciseClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ExerciseListViewModel = viewModel(
        key = "group_$groupId",
        factory = ExerciseListViewModel.Factory(
            groupId,
            app.container.exerciseRepository,
            app.container.sessionRepository
        )
    )
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseUi?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 80.dp  // espacio para el FAB
            )
        ) {
            items(exercises, key = { it.exercise.id }) { item ->
                ExerciseRow(
                    item = item,
                    onClick = { onExerciseClick(item.exercise.id) },
                    onLongClick = { exerciseToDelete = item }
                )
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
        ) {
            Icon(Icons.Default.Add, "Añadir ejercicio")
        }
    }

    exerciseToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("¿Eliminar ejercicio?") },
            text = { Text("Se eliminará \"${item.exercise.name}\" y todo su historial.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteExercise(item.exercise)
                    exerciseToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onConfirm = { name, description ->
                vm.addExercise(name, description)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseRow(item: ExerciseUi, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                item.exercise.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (item.lastWeightKg != null) {
                Text(
                    "${item.lastSets}×${item.lastReps} · ${"%.1f".format(item.lastWeightKg)}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ejercicio") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, description) }) {
                Text("Añadir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
