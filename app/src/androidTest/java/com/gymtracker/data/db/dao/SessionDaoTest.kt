package com.gymtracker.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.data.db.AppDatabase
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup
import com.gymtracker.data.db.entity.Session
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var sessionDao: SessionDao
    private var exerciseId: Long = 0

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        sessionDao = db.sessionDao()

        val groupId = db.muscleGroupDao().insert(MuscleGroup(name = "Pecho", emoji = "🏋️"))
        exerciseId = db.exerciseDao().insert(Exercise(muscleGroupId = groupId, name = "Press Banca", description = ""))
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndGetLatest() = runTest {
        val session = Session(exerciseId = exerciseId, date = System.currentTimeMillis(), sets = 3, reps = 8, weightKg = 80f)
        sessionDao.insert(session)
        val latest = sessionDao.getLatest(exerciseId)
        assertNotNull(latest)
        assertEquals(80f, latest!!.weightKg)
    }

    @Test
    fun getMaxWeight_returnsHighest() = runTest {
        sessionDao.insert(Session(exerciseId = exerciseId, date = 1000L, sets = 3, reps = 8, weightKg = 60f))
        sessionDao.insert(Session(exerciseId = exerciseId, date = 2000L, sets = 3, reps = 8, weightKg = 80f))
        sessionDao.insert(Session(exerciseId = exerciseId, date = 3000L, sets = 3, reps = 8, weightKg = 75f))
        assertEquals(80f, sessionDao.getMaxWeight(exerciseId))
    }
}
