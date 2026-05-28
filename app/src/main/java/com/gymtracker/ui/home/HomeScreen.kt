package com.gymtracker.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.db.AppDatabase
import com.gymtracker.data.repository.CsvExporter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    onGroupClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as GymTrackerApp
    val vm: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(app.container.exerciseRepository, app.container.sessionRepository)
    )
    val groups by vm.groups.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GymTracker", fontWeight = FontWeight.Bold) },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, "Menú")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Exportar CSV") },
                            onClick = {
                                menuExpanded = false
                                scope.launch {
                                    val intent = CsvExporter.export(context, AppDatabase.getInstance(context))
                                    withContext(Dispatchers.Main) {
                                        context.startActivity(Intent.createChooser(intent, "Exportar datos"))
                                    }
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir grupo muscular")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups, key = { it.group.id }) { item ->
                MuscleGroupCard(item = item, onClick = { onGroupClick(item.group.id) })
            }
        }
    }

    if (showAddDialog) {
        AddMuscleGroupDialog(
            onConfirm = { name, emoji ->
                vm.addGroup(name, emoji)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun MuscleGroupCard(item: MuscleGroupUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isStale)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.group.emoji, fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(item.group.name, style = MaterialTheme.typography.titleMedium)
                if (item.isStale) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sin registros recientes",
                        fontSize = 10.sp,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun AddMuscleGroupDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💪") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo grupo muscular") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, emoji) }) {
                Text("Añadir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
