package com.gymtracker.ui.exercises

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.FileProvider
import com.gymtracker.GymTrackerApp
import java.io.File

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

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.setPendingPhoto(it) }
    }

    var showPhotoSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Stores the URI we give to the camera so we can retrieve it on success
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) pendingCameraUri?.let { vm.setPendingPhoto(it) }
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
            // Exercise photo
            val photoPath = state.exercise?.photoPath
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .clickable { showPhotoSheet = true },
                contentAlignment = Alignment.Center
            ) {
                if (photoPath != null && File(photoPath).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoPath))
                            .memoryCacheKey("ex_${exerciseId}_${state.photoVersion}")
                            .diskCacheKey("ex_${exerciseId}_${state.photoVersion}")
                            .build(),
                        contentDescription = state.exercise?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Overlay icon to indicate it's tappable
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("Añadir foto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

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

    // Photo framing overlay
    state.pendingFrameUri?.let { uri ->
        var panX by remember { mutableStateOf(0f) }
        var panY by remember { mutableStateOf(0f) }
        var userScale by remember { mutableStateOf(1f) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!state.isSavingPhoto) vm.cancelPhotoFrame() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                var viewWidthPx by remember { mutableStateOf(0f) }
                var viewHeightPx by remember { mutableStateOf(0f) }

                // Image + gesture area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.82f)
                        .align(Alignment.TopCenter)
                        .onGloballyPositioned { coords ->
                            viewWidthPx = coords.size.width.toFloat()
                            viewHeightPx = coords.size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                userScale = (userScale * zoom).coerceIn(0.5f, 4f)
                                panX += pan.x
                                panY += pan.y
                            }
                        }
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = userScale
                                scaleY = userScale
                                translationX = panX
                                translationY = panY
                            }
                    )

                    // Rule-of-thirds grid
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
                        // vertical thirds
                        drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width / 3f, size.height), strokeWidth = 1f)
                        drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, size.height), strokeWidth = 1f)
                        // horizontal thirds
                        drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height / 3f), strokeWidth = 1f)
                        drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height * 2f / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height * 2f / 3f), strokeWidth = 1f)
                        // Corner L-markers
                        val m = 16f; val len = 28f
                        // top-left
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(m, m), androidx.compose.ui.geometry.Offset(m + len, m), strokeWidth = 2.5f)
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(m, m), androidx.compose.ui.geometry.Offset(m, m + len), strokeWidth = 2.5f)
                        // top-right
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(size.width - m, m), androidx.compose.ui.geometry.Offset(size.width - m - len, m), strokeWidth = 2.5f)
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(size.width - m, m), androidx.compose.ui.geometry.Offset(size.width - m, m + len), strokeWidth = 2.5f)
                        // bottom-left
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(m, size.height - m), androidx.compose.ui.geometry.Offset(m + len, size.height - m), strokeWidth = 2.5f)
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(m, size.height - m), androidx.compose.ui.geometry.Offset(m, size.height - m - len), strokeWidth = 2.5f)
                        // bottom-right
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(size.width - m, size.height - m), androidx.compose.ui.geometry.Offset(size.width - m - len, size.height - m), strokeWidth = 2.5f)
                        drawLine(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.geometry.Offset(size.width - m, size.height - m), androidx.compose.ui.geometry.Offset(size.width - m, size.height - m - len), strokeWidth = 2.5f)
                    }

                    // Instruction chip
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "Pellizca para zoom · Arrastra para encuadrar",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }

                // Buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.cancelPhotoFrame() },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSavingPhoto,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            vm.savePhotoWithFrame(
                                context, uri, panX, panY, userScale, viewWidthPx, viewHeightPx
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSavingPhoto
                    ) {
                        if (state.isSavingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar encuadre")
                        }
                    }
                }
            }
        }
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

    if (showPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSheet = false },
            sheetState = bottomSheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Cámara") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable {
                        showPhotoSheet = false
                        val tempFile = File(context.cacheDir, "camera_temp_${exerciseId}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", tempFile
                        )
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                )
                ListItem(
                    headlineContent = { Text("Galería") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable {
                        showPhotoSheet = false
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                if (state.exercise?.photoPath != null) {
                    ListItem(
                        headlineContent = { Text("Eliminar foto", color = MaterialTheme.colorScheme.error) },
                        leadingContent = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier.clickable {
                            showPhotoSheet = false
                            vm.deletePhoto()
                        }
                    )
                }
            }
        }
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
