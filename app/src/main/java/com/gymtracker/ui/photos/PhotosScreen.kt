package com.gymtracker.ui.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.db.entity.BodyZone
import java.io.File

@Composable
fun PhotosScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: PhotosViewModel = viewModel(
        factory = PhotosViewModel.Factory(app.container.bodyPhotoRepository)
    )
    val photos by vm.photos.collectAsStateWithLifecycle()
    val selectedZone by vm.selectedZone.collectAsStateWithLifecycle()
    var showZonePicker by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pendingUri = it; showZonePicker = true }
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Default.Add, "Añadir foto")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Zone filter chips (horizontal scrollable row)
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(selected = selectedZone == null, onClick = { vm.selectZone(null) }, label = { Text("Todo") })
                BodyZone.entries.forEach { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick = { vm.selectZone(if (selectedZone == zone) null else zone) },
                        label = { Text(zone.displayName()) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    AsyncImage(
                        model = File(photo.photoPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }

    if (showZonePicker && pendingUri != null) {
        var chosenZone by remember { mutableStateOf(BodyZone.FULL_BODY) }
        AlertDialog(
            onDismissRequest = { showZonePicker = false },
            title = { Text("Zona corporal") },
            text = {
                Column {
                    BodyZone.entries.forEach { zone ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected = chosenZone == zone, onClick = { chosenZone = zone })
                            Text(zone.displayName())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingUri?.let { vm.savePhoto(context, it, chosenZone) }
                    showZonePicker = false
                    pendingUri = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showZonePicker = false }) { Text("Cancelar") } }
        )
    }
}

fun BodyZone.displayName(): String = when (this) {
    BodyZone.FULL_BODY -> "Cuerpo entero"
    BodyZone.CHEST -> "Pecho"
    BodyZone.BACK -> "Espalda"
    BodyZone.ARMS -> "Brazos"
    BodyZone.LEGS -> "Piernas"
    BodyZone.SHOULDERS -> "Hombros"
}
