package com.gymtracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.MuscleGroup
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MuscleGroupUi(
    val group: MuscleGroup,
    val isStale: Boolean
)

class HomeViewModel(
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val groups: StateFlow<List<MuscleGroupUi>> = exerciseRepo.getAllGroups()
        .map { groups ->
            groups.map { group ->
                val latest = sessionRepo.getLatestSessionForGroup(group.id)
                val isStale = latest == null ||
                    (System.currentTimeMillis() - latest.date) > TimeUnit.DAYS.toMillis(7)
                MuscleGroupUi(group, isStale)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGroup(name: String, emoji: String) {
        viewModelScope.launch {
            exerciseRepo.insertGroup(MuscleGroup(name = name, emoji = emoji))
        }
    }

    class Factory(
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(exerciseRepo, sessionRepo) as T
        }
    }
}
