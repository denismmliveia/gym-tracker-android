# Photo UX Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix three bugs in the exercise photo feature (wrong crop, stale cache, gallery pollution) and upgrade the UX with a camera+gallery bottom sheet, rule-of-thirds framing overlay, and loading feedback.

**Architecture:** All changes are confined to `ExerciseDetailViewModel.kt` and `ExerciseDetailScreen.kt` plus a one-line XML tweak. The ViewModel gains `isSavingPhoto` state and a corrected crop algorithm; the Screen gains a `ModalBottomSheet` picker, a camera launcher, and an enhanced framing dialog with grid overlay.

**Tech Stack:** Kotlin, Jetpack Compose + Material3, Coil, ActivityResultContracts.TakePicture, FileProvider, android.graphics.Bitmap

---

## File Map

| File | Change |
|------|--------|
| `app/src/main/res/xml/file_paths.xml` | Add `<cache-path>` so FileProvider can serve the camera temp URI |
| `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailViewModel.kt` | Add `isSavingPhoto`, `deletePhoto()`, fix crop math, switch state update to Main dispatcher |
| `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt` | Camera launcher, bottom sheet, grid overlay in framing dialog, loading spinner |

---

### Task 1: Add cache-path to FileProvider

**Files:**
- Modify: `app/src/main/res/xml/file_paths.xml`

`TakePicture` writes the camera photo to a URI we provide. That URI must be served by FileProvider. The current `file_paths.xml` only exposes `external-files-path` (for CSV export). We need to add `cache-path` so the camera can write to `cacheDir`.

- [ ] **Step 1: Open `file_paths.xml`**

Current content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="documents" path="Documents/" />
</paths>
```

- [ ] **Step 2: Add cache-path entry**

Replace the full file with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="documents" path="Documents/" />
    <cache-path name="camera_photos" path="." />
</paths>
```

`path="."` exposes the entire `cacheDir` root — safe because cacheDir is app-private.

- [ ] **Step 3: Build to verify no XML errors**

```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/xml/file_paths.xml
git commit -m "feat: expose cache-path in FileProvider for camera temp files"
```

---

### Task 2: Fix ViewModel — crop math, isSavingPhoto, deletePhoto

**Files:**
- Modify: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailViewModel.kt`

Three changes in one commit:
1. Add `isSavingPhoto: Boolean` to `DetailUiState` so the UI can show a spinner
2. Fix the crop math: the current formula uses `viewWidthPx / (2 * baseScale)` for the center offset, which is only correct when `userScale == 1`. With zoom the formula must use `totalScale`.
3. Add `deletePhoto()` function
4. Switch the final `_state.update` inside `savePhotoWithFrame` to the Main dispatcher to guarantee immediate recomposition.

- [ ] **Step 1: Add `isSavingPhoto` to `DetailUiState`**

In `ExerciseDetailViewModel.kt`, change:
```kotlin
data class DetailUiState(
    val exercise: Exercise? = null,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isPersonalRecord: Boolean = false,
    val isListening: Boolean = false,
    val pendingParsed: ParsedSession? = null,
    val voiceRawText: String = "",
    val justSaved: Boolean = false,
    val isSaving: Boolean = false,
    val photoVersion: Long = 0,
    val pendingFrameUri: android.net.Uri? = null,
)
```
to:
```kotlin
data class DetailUiState(
    val exercise: Exercise? = null,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isPersonalRecord: Boolean = false,
    val isListening: Boolean = false,
    val pendingParsed: ParsedSession? = null,
    val voiceRawText: String = "",
    val justSaved: Boolean = false,
    val isSaving: Boolean = false,
    val photoVersion: Long = 0,
    val pendingFrameUri: android.net.Uri? = null,
    val isSavingPhoto: Boolean = false,
)
```

- [ ] **Step 2: Fix `savePhotoWithFrame` — crop math + isSavingPhoto + Main dispatcher**

Replace the entire `savePhotoWithFrame` function with:

```kotlin
fun savePhotoWithFrame(
    context: Context,
    uri: android.net.Uri,
    panX: Float,
    panY: Float,
    userScale: Float,
    viewWidthPx: Float,
    viewHeightPx: Float
) {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            _state.update { it.copy(isSavingPhoto = true) }
        }

        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            android.graphics.BitmapFactory.decodeStream(it)
        } ?: run {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _state.update { it.copy(isSavingPhoto = false) }
            }
            return@launch
        }

        val bmpW = bitmap.width.toFloat()
        val bmpH = bitmap.height.toFloat()

        // Scale that ContentScale.Crop applies to fill the view
        val baseScale = maxOf(viewWidthPx / bmpW, viewHeightPx / bmpH)
        val totalScale = baseScale * userScale

        // Visible bitmap region size (in bitmap pixels)
        val cropW = (viewWidthPx / totalScale).coerceAtMost(bmpW)
        val cropH = (viewHeightPx / totalScale).coerceAtMost(bmpH)

        // The view center maps to the bitmap center (bmpW/2, bmpH/2).
        // graphicsLayer translationX=panX moves the image right on screen,
        // so the visible region shifts left in bitmap space by panX/totalScale.
        val left = (bmpW / 2f - viewWidthPx / (2f * totalScale) - panX / totalScale)
            .coerceIn(0f, (bmpW - cropW).coerceAtLeast(0f))
        val top = (bmpH / 2f - viewHeightPx / (2f * totalScale) - panY / totalScale)
            .coerceIn(0f, (bmpH - cropH).coerceAtLeast(0f))

        val cropped = android.graphics.Bitmap.createBitmap(
            bitmap,
            left.toInt(),
            top.toInt(),
            cropW.toInt().coerceAtMost(bitmap.width - left.toInt()),
            cropH.toInt().coerceAtMost(bitmap.height - top.toInt())
        )
        bitmap.recycle()

        val destFile = java.io.File(context.filesDir, "exercise_${exerciseId}.jpg")
        java.io.FileOutputStream(destFile).use { out ->
            cropped.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }
        cropped.recycle()

        val updated = (_state.value.exercise ?: run {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _state.update { it.copy(isSavingPhoto = false) }
            }
            return@launch
        }).copy(photoPath = destFile.absolutePath)

        exerciseRepo.updateExercise(updated)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            _state.update {
                it.copy(
                    exercise = updated,
                    photoVersion = System.currentTimeMillis(),
                    pendingFrameUri = null,
                    isSavingPhoto = false
                )
            }
        }
    }
}
```

Key fix: `left = bmpW/2 - viewWidthPx/(2*totalScale) - panX/totalScale`. The old code used `baseScale` instead of `totalScale` for the center offset, causing the crop to be wrong whenever `userScale != 1`.

- [ ] **Step 3: Add `deletePhoto()` function**

After `cancelPhotoFrame()`, add:

```kotlin
fun deletePhoto() {
    viewModelScope.launch {
        val exercise = _state.value.exercise ?: return@launch
        exercise.photoPath?.let { java.io.File(it).delete() }
        val updated = exercise.copy(photoPath = null)
        exerciseRepo.updateExercise(updated)
        _state.update { it.copy(exercise = updated, photoVersion = System.currentTimeMillis()) }
    }
}
```

- [ ] **Step 4: Build**

```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailViewModel.kt
git commit -m "fix: correct crop math, add isSavingPhoto state and deletePhoto"
```

---

### Task 3: Bottom sheet — camera + gallery + delete

**Files:**
- Modify: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt`

Replace the current single-tap-to-PickVisualMedia pattern with a `ModalBottomSheet` that offers three actions: take a photo with the camera (no gallery pollution), pick from gallery, or delete the current photo.

- [ ] **Step 1: Add required imports at the top of `ExerciseDetailScreen.kt`**

Add these imports (keep existing ones):
```kotlin
import androidx.core.content.FileProvider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import java.io.File
```

- [ ] **Step 2: Add launchers and sheet state inside `ExerciseDetailScreen`**

Below the existing `photoPicker` launcher declaration, add:

```kotlin
var showPhotoSheet by remember { mutableStateOf(false) }
val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

// Stores the URI we give to the camera so we can retrieve it on success
var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

val cameraLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.TakePicture()
) { success ->
    if (success) pendingCameraUri?.let { vm.setPendingPhoto(it) }
}
```

- [ ] **Step 3: Change the photo Box to open the sheet instead of the picker**

Find the current photo `Box` that has:
```kotlin
.clickable {
    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
},
```

Change `.clickable { ... }` to:
```kotlin
.clickable { showPhotoSheet = true },
```

- [ ] **Step 4: Add ModalBottomSheet after the PR dialog (end of the composable)**

After the personal record dialog block, add:

```kotlin
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
```

- [ ] **Step 5: Build**

```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt
git commit -m "feat: bottom sheet with camera/gallery/delete for exercise photo"
```

---

### Task 4: Framing dialog — grid overlay + instruction chip + loading spinner

**Files:**
- Modify: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt` (framing dialog section only)

Enhance the framing `Dialog` with:
- Rule-of-thirds grid (2 vertical + 2 horizontal lines)
- L-shaped corner markers
- Instruction chip at the top
- "Guardar encuadre" button replaced by `CircularProgressIndicator` while `isSavingPhoto == true`

- [ ] **Step 1: Replace the framing dialog block**

Find the full `state.pendingFrameUri?.let { uri -> ... }` block and replace it entirely with:

```kotlin
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
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
                    // vertical thirds
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width / 3f, size.height), strokeWidth = 1f)
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, 0f), androidx.compose.ui.geometry.Offset(size.width * 2f / 3f, size.height), strokeWidth = 1f)
                    // horizontal thirds
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height / 3f), strokeWidth = 1f)
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, size.height * 2f / 3f), androidx.compose.ui.geometry.Offset(size.width, size.height * 2f / 3f), strokeWidth = 1f)
                    // Corner L-markers (top-left)
                    val m = 16f; val len = 28f
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
```

Note: `androidx.compose.foundation.Canvas` requires adding `import androidx.compose.foundation.Canvas` if not already present via the wildcard `foundation.*` import. The existing `import androidx.compose.foundation.layout.*` does NOT cover Canvas — add `import androidx.compose.foundation.Canvas` explicitly.

- [ ] **Step 2: Add missing import for Canvas**

At the top of `ExerciseDetailScreen.kt`, add:
```kotlin
import androidx.compose.foundation.Canvas
```

- [ ] **Step 3: Build**

```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Install and manually verify on device**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Manual test checklist:
- Tap exercise photo area → bottom sheet appears with Cámara / Galería
- Tap Cámara → camera app opens, take photo → framing dialog opens
- Grid and corner markers are visible in the framing dialog
- Pan and zoom the image, tap "Guardar encuadre" → spinner appears → photo updates immediately in the detail screen
- Tap photo area again → "Eliminar foto" option is now visible → tap it → photo is removed
- Tap Galería → pick from gallery → framing dialog opens → same flow

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt
git commit -m "feat: framing dialog with rule-of-thirds grid, instruction chip, and loading spinner"
```
