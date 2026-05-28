package com.gymtracker.data.repository

import com.gymtracker.data.db.dao.BodyPhotoDao
import com.gymtracker.data.db.entity.BodyPhoto
import com.gymtracker.data.db.entity.BodyZone
import kotlinx.coroutines.flow.Flow

class BodyPhotoRepository(private val bodyPhotoDao: BodyPhotoDao) {
    fun getAllPhotos(): Flow<List<BodyPhoto>> = bodyPhotoDao.getAll()
    fun getPhotosByZone(zone: BodyZone): Flow<List<BodyPhoto>> = bodyPhotoDao.getByZone(zone)
    suspend fun insertPhoto(photo: BodyPhoto): Long = bodyPhotoDao.insert(photo)
    suspend fun deletePhoto(photo: BodyPhoto) = bodyPhotoDao.delete(photo)
}
