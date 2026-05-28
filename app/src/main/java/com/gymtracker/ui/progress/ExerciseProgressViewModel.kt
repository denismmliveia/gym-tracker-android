package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ProgressMetric { MAX_WEIGHT, VOLUME, ONE_RM }

data class ExerciseProgressState(
    val exercise: Exercise? = null,
    val sessions: List<Session> = emptyList(),
    val metric: ProgressMetric = ProgressMetric.MAX_WEIGHT
)

class ExerciseProgressViewModel(
    private val exerciseId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _metric = MutableStateFlow(ProgressMetric.MAX_WEIGHT)
    private val _exercise = MutableStateFlow<Exercise?>(null)

    init {
        viewModelScope.launch {
            _exercise.value = exerciseRepo.getExerciseById(exerciseId)
        }
    }

    val state: StateFlow<ExerciseProgressState> = combine(
        sessionRepo.getSessionsForExercise(exerciseId),
        _metric,
        _exercise
    ) { sessions, metric, exercise ->
        ExerciseProgressState(exercise = exercise, sessions = sessions, metric = metric)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseProgressState())

    fun setMetric(metric: ProgressMetric) { _metric.value = metric }

    class Factory(
        private val exerciseId: Long,
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExerciseProgressViewModel(exerciseId, exerciseRepo, sessionRepo) as T
        }
    }
}

fun Session.metricValue(metric: ProgressMetric): Float = when (metric) {
    ProgressMetric.MAX_WEIGHT -> weightKg
    ProgressMetric.VOLUME -> sets * reps * weightKg
    ProgressMetric.ONE_RM -> if (reps > 0) weightKg * (1 + reps / 30f) else weightKg
}
