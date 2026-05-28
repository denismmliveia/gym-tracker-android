package com.gymtracker.data.repository

import com.gymtracker.data.db.dao.ExerciseDao
import com.gymtracker.data.db.dao.MuscleGroupDao
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val muscleGroupDao: MuscleGroupDao,
    private val exerciseDao: ExerciseDao
) {
    fun getAllGroups(): Flow<List<MuscleGroup>> = muscleGroupDao.getAll()
    fun getExercisesForGroup(groupId: Long): Flow<List<Exercise>> = exerciseDao.getByGroup(groupId)
    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getById(id)
    suspend fun insertGroup(group: MuscleGroup): Long = muscleGroupDao.insert(group)
    suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insert(exercise)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)
}
