package com.gymtracker.data.db.dao

import androidx.room.*
import com.gymtracker.data.db.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {
    @Query("SELECT * FROM muscle_groups ORDER BY name ASC")
    fun getAll(): Flow<List<MuscleGroup>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(groups: List<MuscleGroup>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: MuscleGroup): Long

    @Delete
    suspend fun delete(group: MuscleGroup)

    @Query("SELECT * FROM muscle_groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MuscleGroup?
}
