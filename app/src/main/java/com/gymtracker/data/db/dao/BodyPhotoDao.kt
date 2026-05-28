package com.gymtracker.data.db.dao

import androidx.room.*
import com.gymtracker.data.db.entity.BodyPhoto
import com.gymtracker.data.db.entity.BodyZone
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyPhotoDao {
    @Query("SELECT * FROM body_photos ORDER BY date DESC")
    fun getAll(): Flow<List<BodyPhoto>>

    @Query("SELECT * FROM body_photos WHERE zone = :zone ORDER BY date DESC")
    fun getByZone(zone: BodyZone): Flow<List<BodyPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: BodyPhoto): Long

    @Delete
    suspend fun delete(photo: BodyPhoto)
}
