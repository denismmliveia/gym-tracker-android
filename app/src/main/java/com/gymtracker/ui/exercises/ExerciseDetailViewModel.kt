package com.gymtracker.ui.exercises

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.domain.voice.ParsedSession
import com.gymtracker.domain.voice.SessionParser
import com.gymtracker.domain.voice.VoiceRecognizer
import com.gymtracker.domain.voice.VoiceResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val exercise: Exercise? = null,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isPersonalRecord: Boolean = false,
    val isListening: Boolean = false,
    val pendingParsed: ParsedSession? = null,   // waiting for user confirmation
    val voiceRawText: String = "",
    val justSaved: Boolean = false,
    val isSaving: Boolean = false,
    val photoVersion: Long = 0,
    val pendingFrameUri: android.net.Uri? = null,
)

class ExerciseDetailViewModel(
    private val exerciseId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepo.getExerciseById(exerciseId)
            val latest = sessionRepo.getLatestSession(exerciseId)
            _state.update {
                it.copy(
                    exercise = exercise,
                    sets = latest?.sets ?: 3,
                    reps = latest?.reps ?: 10,
                    weightKg = latest?.weightKg ?: 0f
                )
            }
        }
    }

    fun setSets(value: Int) = _state.update { it.copy(sets = maxOf(1, value)) }
    fun setReps(value: Int) = _state.update { it.copy(reps = maxOf(1, value)) }
    fun setWeight(value: Float) = _state.update { it.copy(weightKg = maxOf(0f, value)) }

    fun saveSession() {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val maxWeight = sessionRepo.getMaxWeight(exerciseId) ?: 0f
            val isRecord = s.weightKg > maxWeight
            sessionRepo.insertSession(
                Session(exerciseId = exerciseId, date = System.currentTimeMillis(),
                    sets = s.sets, reps = s.reps, weightKg = s.weightKg)
            )
            _state.update { it.copy(isSaving = false, isPersonalRecord = isRecord, justSaved = isRecord) }
        }
    }

    fun startVoice(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isListening = true) }
            VoiceRecognizer(context).listen().collect { result ->
                when (result) {
                    is VoiceResult.Success -> {
                        val parsed = SessionParser().parse(result.text)
                        _state.update {
                            it.copy(isListening = false, pendingParsed = parsed, voiceRawText = result.text)
                        }
                    }
                    is VoiceResult.Error -> _state.update { it.copy(isListening = false) }
                }
            }
        }
    }

    fun confirmVoiceSession(sets: Int, reps: Int, weightKg: Float) {
        setSets(sets)
        setReps(reps)
        setWeight(weightKg)
        _state.update { it.copy(pendingParsed = null) }
        saveSession()
    }

    fun dismissVoiceDialog() = _state.update { it.copy(pendingParsed = null) }
    fun dismissRecord() = _state.update { it.copy(isPersonalRecord = false, justSaved = false) }

    fun setPendingPhoto(uri: android.net.Uri) {
        _state.update { it.copy(pendingFrameUri = uri) }
    }

    fun cancelPhotoFrame() {
        _state.update { it.copy(pendingFrameUri = null) }
    }

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
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            } ?: return@launch

            val bmpW = bitmap.width.toFloat()
            val bmpH = bitmap.height.toFloat()

            // ContentScale.Crop base scale
            val baseScale = maxOf(viewWidthPx / bmpW, viewHeightPx / bmpH)
            val totalScale = baseScale * userScale

            // Visible region size in bitmap pixels
            val cropW = (viewWidthPx / totalScale).coerceAtMost(bmpW)
            val cropH = (viewHeightPx / totalScale).coerceAtMost(bmpH)

            // Center crop starting position (what ContentScale.Crop shows without pan)
            val centerBmpX = (bmpW - viewWidthPx / baseScale) / 2f
            val centerBmpY = (bmpH - viewHeightPx / baseScale) / 2f

            // Pan in bitmap coordinates (panX/panY are in screen px, positive = image moved right/down)
            val panBmpX = panX / totalScale
            val panBmpY = panY / totalScale

            val left = (centerBmpX - panBmpX).coerceIn(0f, (bmpW - cropW).coerceAtLeast(0f))
            val top = (centerBmpY - panBmpY).coerceIn(0f, (bmpH - cropH).coerceAtLeast(0f))

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

            val updated = (_state.value.exercise ?: return@launch).copy(photoPath = destFile.absolutePath)
            exerciseRepo.updateExercise(updated)
            _state.update {
                it.copy(
                    exercise = updated,
                    photoVersion = System.currentTimeMillis(),
                    pendingFrameUri = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(isListening = false) }
    }

    class Factory(
        private val exerciseId: Long,
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExerciseDetailViewModel(exerciseId, exerciseRepo, sessionRepo) as T
        }
    }
}
