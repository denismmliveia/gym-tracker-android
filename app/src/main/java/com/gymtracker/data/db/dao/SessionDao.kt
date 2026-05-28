package com.gymtracker.data.db.dao

import androidx.room.*
import com.gymtracker.data.db.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getByExercise(exerciseId: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE exerciseId = :exerciseId ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(exerciseId: Long): Session?

    @Query("SELECT MAX(weightKg) FROM sessions WHERE exerciseId = :exerciseId")
    suspend fun getMaxWeight(exerciseId: Long): Float?

    @Query("""
        SELECT s.* FROM sessions s
        INNER JOIN exercises e ON s.exerciseId = e.id
        WHERE e.muscleGroupId = :groupId
        ORDER BY s.date DESC LIMIT 1
    """)
    suspend fun getLatestForGroup(groupId: Long): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session): Long

    @Delete
    suspend fun delete(session: Session)
}
