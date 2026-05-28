package com.gymtracker.ui.exercises

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(exerciseId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ExerciseDetailViewModel = viewModel(
        key = "detail_$exerciseId",
        factory = ExerciseDetailViewModel.Factory(exerciseId, app.container.exerciseRepository, app.container.sessionRepository)
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.startVoice(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exercise?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            state.exercise?.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            // +/- controls
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StepperField(label = "SERIES", value = state.sets,
                        onDecrement = { vm.setSets(state.sets - 1) },
                        onIncrement = { vm.setSets(state.sets + 1) })
                    StepperField(label = "REPS", value = state.reps,
                        onDecrement = { vm.setReps(state.reps - 1) },
                        onIncrement = { vm.setReps(state.reps + 1) })
                    StepperField(label = "KG", value = state.weightKg.toInt(),
                        onDecrement = { vm.setWeight(state.weightKg - 2.5f) },
                        onIncrement = { vm.setWeight(state.weightKg + 2.5f) })
                }
            }

            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isListening
                ) {
                    Icon(Icons.Default.Mic, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isListening) "Escuchando..." else "Voz")
                }
                OutlinedButton(onClick = { vm.saveSession() }, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar")
                }
            }
        }
    }

    // Voice confirmation dialog
    state.pendingParsed?.let { parsed ->
        var sets by rememberSaveable { mutableStateOf(parsed.sets?.toString() ?: "") }
        var reps by rememberSaveable { mutableStateOf(parsed.reps?.toString() ?: "") }
        var weight by rememberSaveable { mutableStateOf(parsed.weightKg?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { vm.dismissVoiceDialog() },
            title = { Text("Confirmar sesión") },
            text = {
                Column {
                    Text("\"${state.voiceRawText}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sets, onValueChange = { sets = it },
                            label = { Text("Series") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reps, onValueChange = { reps = it },
                            label = { Text("Reps") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = weight, onValueChange = { weight = it },
                            label = { Text("Kg") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.confirmVoiceSession(
                        sets.toIntOrNull() ?: 0,
                        reps.toIntOrNull() ?: 0,
                        weight.toFloatOrNull() ?: 0f
                    )
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissVoiceDialog() }) { Text("Cancelar") } }
        )
    }

    // Personal record dialog
    if (state.isPersonalRecord && state.justSaved) {
        AlertDialog(
            onDismissRequest = { vm.dismissRecord() },
            title = { Text("\uD83C\uDFC6 ¡Nuevo récord personal!") },
            text = { Text("${state.weightKg}kg — tu mejor marca en este ejercicio.") },
            confirmButton = { TextButton(onClick = { vm.dismissRecord() }) { Text("Genial") } }
        )
    }
}

@Composable
private fun StepperField(label: String, value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(4.dp))
        Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }
            FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
