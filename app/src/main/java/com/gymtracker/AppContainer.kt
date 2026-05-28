package com.gymtracker

import android.content.Context
import com.gymtracker.data.db.AppDatabase
import com.gymtracker.data.repository.BodyPhotoRepository
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)
    val exerciseRepository = ExerciseRepository(db.muscleGroupDao(), db.exerciseDao())
    val sessionRepository = SessionRepository(db.sessionDao())
    val bodyPhotoRepository = BodyPhotoRepository(db.bodyPhotoDao())
}
