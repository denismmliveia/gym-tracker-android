package com.gymtracker.data.repository

import com.gymtracker.data.db.dao.SessionDao
import com.gymtracker.data.db.entity.Session
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    fun getSessionsForExercise(exerciseId: Long): Flow<List<Session>> =
        sessionDao.getByExercise(exerciseId)

    suspend fun getLatestSession(exerciseId: Long): Session? =
        sessionDao.getLatest(exerciseId)

    suspend fun getMaxWeight(exerciseId: Long): Float? =
        sessionDao.getMaxWeight(exerciseId)

    suspend fun getLatestSessionForGroup(groupId: Long): Session? =
        sessionDao.getLatestForGroup(groupId)

    suspend fun insertSession(session: Session): Long =
        sessionDao.insert(session)
}
