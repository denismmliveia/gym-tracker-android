package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseProgressSummary(
    val exercise: Exercise,
    val sessions: List<Session>,
    val improvementPct: Float?   // null if <2 sessions
)

data class GroupProgressUi(
    val group: MuscleGroup,
    val exercises: List<ExerciseProgressSummary>
)

class ProgressViewModel(
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val groupProgress: StateFlow<List<GroupProgressUi>> =
        exerciseRepo.getAllGroups()
            .map { groups ->
                groups.map { group ->
                    val exercises = exerciseRepo.getExercisesForGroup(group.id).first()
                    val summaries = exercises.map { exercise ->
                        val sessions = sessionRepo.getSessionsForExercise(exercise.id).first()
                        val improvementPct = if (sessions.size >= 2) {
                            val first = sessions.last().weightKg
                            val last = sessions.first().weightKg
                            if (first > 0f) ((last - first) / first) * 100f else null
                        } else null
                        ExerciseProgressSummary(exercise, sessions.takeLast(8), improvementPct)
                    }.filter { it.sessions.isNotEmpty() }
                    GroupProgressUi(group, summaries)
                }.filter { it.exercises.isNotEmpty() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(exerciseRepo, sessionRepo) as T
        }
    }
}
