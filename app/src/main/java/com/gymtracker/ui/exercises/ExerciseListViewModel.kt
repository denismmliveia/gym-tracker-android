package com.gymtracker.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseUi(
    val exercise: Exercise,
    val lastSets: Int?,
    val lastReps: Int?,
    val lastWeightKg: Float?
)

class ExerciseListViewModel(
    private val groupId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val exercises: StateFlow<List<ExerciseUi>> =
        exerciseRepo.getExercisesForGroup(groupId)
            .map { list ->
                list.map { exercise ->
                    val latest = sessionRepo.getLatestSession(exercise.id)
                    ExerciseUi(exercise, latest?.sets, latest?.reps, latest?.weightKg)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExercise(name: String, description: String) {
        viewModelScope.launch {
            exerciseRepo.insertExercise(
                Exercise(muscleGroupId = groupId, name = name, description = description)
            )
        }
    }

    class Factory(
        private val groupId: Long,
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExerciseListViewModel(groupId, exerciseRepo, sessionRepo) as T
        }
    }
}
