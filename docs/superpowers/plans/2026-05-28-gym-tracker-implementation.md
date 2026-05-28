# GymTracker — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a personal Android gym tracker app with exercise management, voice input, progress charts, and a body photo gallery.

**Architecture:** MVVM with Jetpack Compose UI, Room for local SQLite storage, and a manual dependency container via a custom Application class. No Hilt — a simple `AppContainer` keeps dependencies explicit and avoids annotation processing overhead.

**Tech Stack:** Kotlin, Jetpack Compose, Room 2.6, Navigation Compose, MPAndroidChart, CameraX Photo Picker, Android SpeechRecognizer, Coil

---

## File Map

```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/gymtracker/
│   │       ├── GymTrackerApp.kt
│   │       ├── AppContainer.kt
│   │       ├── data/
│   │       │   ├── db/
│   │       │   │   ├── AppDatabase.kt
│   │       │   │   ├── Converters.kt
│   │       │   │   ├── DatabaseSeeder.kt
│   │       │   │   ├── entity/
│   │       │   │   │   ├── BodyZone.kt
│   │       │   │   │   ├── MuscleGroup.kt
│   │       │   │   │   ├── Exercise.kt
│   │       │   │   │   ├── Session.kt
│   │       │   │   │   └── BodyPhoto.kt
│   │       │   │   └── dao/
│   │       │   │       ├── MuscleGroupDao.kt
│   │       │   │       ├── ExerciseDao.kt
│   │       │   │       ├── SessionDao.kt
│   │       │   │       └── BodyPhotoDao.kt
│   │       │   └── repository/
│   │       │       ├── ExerciseRepository.kt
│   │       │       ├── SessionRepository.kt
│   │       │       └── BodyPhotoRepository.kt
│   │       ├── domain/
│   │       │   └── voice/
│   │       │       ├── SessionParser.kt
│   │       │       └── VoiceRecognizer.kt
│   │       └── ui/
│   │           ├── MainActivity.kt
│   │           ├── navigation/
│   │           │   └── AppNavigation.kt
│   │           ├── theme/
│   │           │   └── Theme.kt
│   │           ├── home/
│   │           │   ├── HomeScreen.kt
│   │           │   └── HomeViewModel.kt
│   │           ├── exercises/
│   │           │   ├── ExerciseListScreen.kt
│   │           │   ├── ExerciseListViewModel.kt
│   │           │   ├── ExerciseDetailScreen.kt
│   │           │   └── ExerciseDetailViewModel.kt
│   │           ├── progress/
│   │           │   ├── ProgressScreen.kt
│   │           │   ├── ProgressViewModel.kt
│   │           │   ├── ExerciseProgressScreen.kt
│   │           │   └── ExerciseProgressViewModel.kt
│   │           └── photos/
│   │               ├── PhotosScreen.kt
│   │               └── PhotosViewModel.kt
│   ├── test/java/com/gymtracker/
│   │   └── domain/voice/
│   │       └── SessionParserTest.kt
│   └── androidTest/java/com/gymtracker/
│       └── data/db/dao/
│           └── SessionDaoTest.kt
```

---

## Task 1: Create Android project & configure Gradle

**Files:**
- Create: `build.gradle.kts` (project root)
- Modify: `app/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create new Android project in Android Studio**

  File → New → New Project → "Empty Activity". Configure:
  - Name: `GymTracker`
  - Package: `com.gymtracker`
  - Save location: `C:\Users\denis\Proyectos-IA\diario gym`
  - Language: Kotlin
  - Minimum SDK: API 26 (Android 8.0)

- [ ] **Step 2: Replace project-level `build.gradle.kts`**

```kotlin
// build.gradle.kts (project root)
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}
```

- [ ] **Step 3: Replace `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gymtracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gymtracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Camera (Photo Picker + CameraX)
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

- [ ] **Step 4: Add JitPack to `settings.gradle.kts`**

  Inside `dependencyResolutionManagement.repositories {}`, add:
  ```kotlin
  maven { url = uri("https://jitpack.io") }
  ```

- [ ] **Step 5: Replace `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".GymTrackerApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.GymTracker">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.GymTracker">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 6: Sync Gradle and verify build**

  In Android Studio: File → Sync Project with Gradle Files.
  Expected: BUILD SUCCESSFUL, no errors.

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "feat: initial Android project setup with Gradle dependencies"
```

---

## Task 2: Data entities & enums

**Files:**
- Create: `app/src/main/java/com/gymtracker/data/db/entity/BodyZone.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/entity/MuscleGroup.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/entity/Exercise.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/entity/Session.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/entity/BodyPhoto.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/Converters.kt`

- [ ] **Step 1: Create `BodyZone.kt`**

```kotlin
package com.gymtracker.data.db.entity

enum class BodyZone {
    FULL_BODY, CHEST, BACK, ARMS, LEGS, SHOULDERS
}
```

- [ ] **Step 2: Create `MuscleGroup.kt`**

```kotlin
package com.gymtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscle_groups")
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String
)
```

- [ ] **Step 3: Create `Exercise.kt`**

```kotlin
package com.gymtracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = MuscleGroup::class,
        parentColumns = ["id"],
        childColumns = ["muscleGroupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("muscleGroupId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val muscleGroupId: Long,
    val name: String,
    val description: String,
    val photoPath: String? = null
)
```

- [ ] **Step 4: Create `Session.kt`**

```kotlin
package com.gymtracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = Exercise::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("exerciseId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val date: Long,        // epoch ms
    val sets: Int,
    val reps: Int,
    val weightKg: Float
)
```

- [ ] **Step 5: Create `BodyPhoto.kt`**

```kotlin
package com.gymtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_photos")
data class BodyPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val zone: BodyZone,
    val photoPath: String
)
```

- [ ] **Step 6: Create `Converters.kt`**

```kotlin
package com.gymtracker.data.db

import androidx.room.TypeConverter
import com.gymtracker.data.db.entity.BodyZone

class Converters {
    @TypeConverter
    fun fromBodyZone(zone: BodyZone): String = zone.name

    @TypeConverter
    fun toBodyZone(value: String): BodyZone = BodyZone.valueOf(value)
}
```

- [ ] **Step 7: Verify project compiles**

  Build → Make Project. Expected: no errors.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/gymtracker/data/
git commit -m "feat: add Room entities and BodyZone enum"
```

---

## Task 3: DAOs

**Files:**
- Create: `app/src/main/java/com/gymtracker/data/db/dao/MuscleGroupDao.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/dao/ExerciseDao.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/dao/SessionDao.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/dao/BodyPhotoDao.kt`
- Create: `app/src/androidTest/java/com/gymtracker/data/db/dao/SessionDaoTest.kt`

- [ ] **Step 1: Create `MuscleGroupDao.kt`**

```kotlin
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
}
```

- [ ] **Step 2: Create `ExerciseDao.kt`**

```kotlin
package com.gymtracker.data.db.dao

import androidx.room.*
import com.gymtracker.data.db.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE muscleGroupId = :groupId ORDER BY name ASC")
    fun getByGroup(groupId: Long): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)
}
```

- [ ] **Step 3: Create `SessionDao.kt`**

```kotlin
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

    @Insert
    suspend fun insert(session: Session): Long

    @Delete
    suspend fun delete(session: Session)
}
```

- [ ] **Step 4: Create `BodyPhotoDao.kt`**

```kotlin
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

    @Insert
    suspend fun insert(photo: BodyPhoto): Long

    @Delete
    suspend fun delete(photo: BodyPhoto)
}
```

- [ ] **Step 5: Write failing instrumented test for `SessionDao`**

```kotlin
// app/src/androidTest/java/com/gymtracker/data/db/dao/SessionDaoTest.kt
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
```

- [ ] **Step 6: Run test — expect failure (AppDatabase not yet created)**

  Run: Android Studio → right-click `SessionDaoTest` → Run.
  Expected: compilation error or class not found.

- [ ] **Step 7: Commit DAOs (tests will pass after Task 4)**

```bash
git add app/src/main/java/com/gymtracker/data/db/dao/
git add app/src/androidTest/
git commit -m "feat: add Room DAOs and SessionDao instrumented tests"
```

---

## Task 4: AppDatabase + AppContainer + Application + seed data

**Files:**
- Create: `app/src/main/java/com/gymtracker/data/db/AppDatabase.kt`
- Create: `app/src/main/java/com/gymtracker/data/db/DatabaseSeeder.kt`
- Create: `app/src/main/java/com/gymtracker/AppContainer.kt`
- Create: `app/src/main/java/com/gymtracker/GymTrackerApp.kt`

- [ ] **Step 1: Create `AppDatabase.kt`**

```kotlin
package com.gymtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymtracker.data.db.dao.*
import com.gymtracker.data.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MuscleGroup::class, Exercise::class, Session::class, BodyPhoto::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun bodyPhotoDao(): BodyPhotoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "gymtracker.db")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DatabaseSeeder.seed(database)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
```

- [ ] **Step 2: Create `DatabaseSeeder.kt`**

```kotlin
package com.gymtracker.data.db

import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup

object DatabaseSeeder {
    suspend fun seed(db: AppDatabase) {
        val groups = listOf(
            MuscleGroup(name = "Pecho", emoji = "🏋️"),
            MuscleGroup(name = "Espalda", emoji = "🔙"),
            MuscleGroup(name = "Piernas", emoji = "🦵"),
            MuscleGroup(name = "Hombros", emoji = "💪"),
            MuscleGroup(name = "Bíceps", emoji = "💪"),
            MuscleGroup(name = "Tríceps", emoji = "💪")
        )
        groups.forEach { group ->
            val groupId = db.muscleGroupDao().insert(group)
            val exercises = when (group.name) {
                "Pecho" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Press Banca", description = "Tumbado en banco plano. Agarre ligeramente más ancho que los hombros. Bajar la barra hasta el pecho y empujar."),
                    Exercise(muscleGroupId = groupId, name = "Aperturas", description = "Tumbado en banco plano con mancuernas. Abrir los brazos en arco amplio y cerrar."),
                    Exercise(muscleGroupId = groupId, name = "Fondos en paralelas", description = "En paralelas, inclinarse levemente hacia adelante para activar el pecho. Bajar hasta 90° y empujar.")
                )
                "Espalda" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Dominadas", description = "Agarre prono, más ancho que los hombros. Subir hasta que el mentón supere la barra."),
                    Exercise(muscleGroupId = groupId, name = "Remo con barra", description = "Inclinado hacia adelante ~45°. Tirar de la barra hacia el abdomen manteniendo la espalda recta."),
                    Exercise(muscleGroupId = groupId, name = "Jalón al pecho", description = "En máquina de poleas. Tirar de la barra hacia el pecho con agarre prono ancho.")
                )
                "Piernas" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Sentadilla", description = "Pies a la anchura de los hombros. Bajar hasta que los muslos queden paralelos al suelo manteniendo la espalda recta."),
                    Exercise(muscleGroupId = groupId, name = "Prensa de piernas", description = "En máquina de prensa. Pies a la anchura de los hombros. Empujar sin bloquear las rodillas."),
                    Exercise(muscleGroupId = groupId, name = "Extensiones de cuádriceps", description = "En máquina de extensiones. Extender las piernas completamente y bajar de forma controlada.")
                )
                "Hombros" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Press Militar", description = "De pie o sentado. Empujar la barra o mancuernas desde los hombros hacia arriba hasta extender los brazos."),
                    Exercise(muscleGroupId = groupId, name = "Elevaciones laterales", description = "De pie con mancuernas. Elevar los brazos lateralmente hasta la altura de los hombros.")
                )
                "Bíceps" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Curl con barra", description = "De pie, agarre supino. Flexionar los codos llevando la barra hacia los hombros. Bajar de forma controlada."),
                    Exercise(muscleGroupId = groupId, name = "Curl martillo", description = "De pie con mancuernas en agarre neutro (pulgares arriba). Flexionar los codos alternando o simultáneamente.")
                )
                "Tríceps" -> listOf(
                    Exercise(muscleGroupId = groupId, name = "Press francés", description = "Tumbado en banco plano con barra EZ. Bajar la barra hacia la frente flexionando los codos y extender."),
                    Exercise(muscleGroupId = groupId, name = "Extensiones en polea", description = "En polea alta con cuerda o barra. Empujar hacia abajo extendiendo completamente los codos.")
                )
                else -> emptyList()
            }
            db.exerciseDao().insertAll(exercises)
        }
    }
}
```

- [ ] **Step 3: Create `AppContainer.kt`**

```kotlin
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
```

- [ ] **Step 4: Create `GymTrackerApp.kt`**

```kotlin
package com.gymtracker

import android.app.Application

class GymTrackerApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

- [ ] **Step 5: Run `SessionDaoTest` — expect PASS**

  Run the instrumented test on a device or emulator.
  Expected: 2 tests pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/gymtracker/
git commit -m "feat: add AppDatabase, DatabaseSeeder, AppContainer and Application class"
```

---

## Task 5: Repositories

**Files:**
- Create: `app/src/main/java/com/gymtracker/data/repository/ExerciseRepository.kt`
- Create: `app/src/main/java/com/gymtracker/data/repository/SessionRepository.kt`
- Create: `app/src/main/java/com/gymtracker/data/repository/BodyPhotoRepository.kt`

- [ ] **Step 1: Create `ExerciseRepository.kt`**

```kotlin
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
```

- [ ] **Step 2: Create `SessionRepository.kt`**

```kotlin
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
```

- [ ] **Step 3: Create `BodyPhotoRepository.kt`**

```kotlin
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
```

- [ ] **Step 4: Build project**

  Build → Make Project. Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/gymtracker/data/repository/
git commit -m "feat: add exercise, session and body photo repositories"
```

---

## Task 6: SessionParser (TDD)

**Files:**
- Create: `app/src/test/java/com/gymtracker/domain/voice/SessionParserTest.kt`
- Create: `app/src/main/java/com/gymtracker/domain/voice/SessionParser.kt`

- [ ] **Step 1: Write failing unit tests**

```kotlin
// app/src/test/java/com/gymtracker/domain/voice/SessionParserTest.kt
package com.gymtracker.domain.voice

import org.junit.Assert.*
import org.junit.Test

class SessionParserTest {
    private val parser = SessionParser()

    @Test
    fun `parses 'X series de Y con Z kilos'`() {
        val result = parser.parse("press banca, 3 series de 8 con 80 kilos")
        assertEquals(3, result.sets)
        assertEquals(8, result.reps)
        assertEquals(80f, result.weightKg)
    }

    @Test
    fun `parses 'X por Y a Z'`() {
        val result = parser.parse("sentadilla 4 por 10 a 60")
        assertEquals(4, result.sets)
        assertEquals(10, result.reps)
        assertEquals(60f, result.weightKg)
    }

    @Test
    fun `parses 'sin peso'`() {
        val result = parser.parse("dominadas, 5 series de 6 sin peso")
        assertEquals(5, result.sets)
        assertEquals(6, result.reps)
        assertEquals(0f, result.weightKg)
    }

    @Test
    fun `parses weight before sets 'Z kilos, X de Y'`() {
        val result = parser.parse("peso muerto 100 kilos, 3 de 5")
        assertEquals(3, result.sets)
        assertEquals(5, result.reps)
        assertEquals(100f, result.weightKg)
    }

    @Test
    fun `parses 'X por Y' with kg suffix`() {
        val result = parser.parse("curl 3 por 12 con 15 kg")
        assertEquals(3, result.sets)
        assertEquals(12, result.reps)
        assertEquals(15f, result.weightKg)
    }

    @Test
    fun `returns nulls when unparseable`() {
        val result = parser.parse("hola mundo")
        assertNull(result.sets)
        assertNull(result.reps)
        assertNull(result.weightKg)
    }
}
```

- [ ] **Step 2: Run tests — expect failure (class not found)**

  Run: `./gradlew test --tests "com.gymtracker.domain.voice.SessionParserTest"`
  Expected: compilation error.

- [ ] **Step 3: Create `SessionParser.kt`**

```kotlin
package com.gymtracker.domain.voice

data class ParsedSession(
    val sets: Int?,
    val reps: Int?,
    val weightKg: Float?
) {
    val isComplete: Boolean get() = sets != null && reps != null && weightKg != null
}

class SessionParser {
    fun parse(text: String): ParsedSession {
        val t = text.lowercase().trim()
        return ParsedSession(
            sets = extractSets(t),
            reps = extractReps(t),
            weightKg = extractWeight(t)
        )
    }

    private fun extractSets(t: String): Int? {
        Regex("""(\d+)\s*series""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*[xX×]\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*por\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        // "Z kilos, X de Y" — weight comes first, sets is the number after comma/kilos
        Regex("""(?:kilos?|kg)[^0-9]*(\d+)\s*de\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*de\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractReps(t: String): Int? {
        Regex("""series\s+de\s+(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*[xX×]\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*por\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(?:kilos?|kg)[^0-9]*\d+\s*de\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*de\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractWeight(t: String): Float? {
        if (Regex("""sin\s*peso|peso\s*corporal|cuerpo""").containsMatchIn(t)) return 0f
        Regex("""(\d+(?:[.,]\d+)?)\s*(?:kilos?|kg)""").find(t)?.groupValues?.get(1)
            ?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        Regex("""(?:con|a)\s+(\d+(?:[.,]\d+)?)(?!\s*(?:series?|reps?|repeticiones?))""").find(t)
            ?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        return null
    }
}
```

- [ ] **Step 4: Run tests — expect all pass**

  Run: `./gradlew test --tests "com.gymtracker.domain.voice.SessionParserTest"`
  Expected: 6 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/test/ app/src/main/java/com/gymtracker/domain/
git commit -m "feat: add SessionParser with TDD (6 tests passing)"
```

---

## Task 7: VoiceRecognizer wrapper

**Files:**
- Create: `app/src/main/java/com/gymtracker/domain/voice/VoiceRecognizer.kt`

- [ ] **Step 1: Create `VoiceRecognizer.kt`**

```kotlin
package com.gymtracker.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class VoiceResult {
    data class Success(val text: String) : VoiceResult()
    data class Error(val message: String) : VoiceResult()
}

class VoiceRecognizer(private val context: Context) {

    fun listen(): Flow<VoiceResult> = callbackFlow {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                trySend(VoiceResult.Success(text))
                close()
            }

            override fun onError(error: Int) {
                trySend(VoiceResult.Error("Error de reconocimiento: código $error"))
                close()
            }

            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partial: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        recognizer.startListening(intent)
        awaitClose { recognizer.destroy() }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/gymtracker/domain/voice/VoiceRecognizer.kt
git commit -m "feat: add VoiceRecognizer wrapper for Android SpeechRecognizer"
```

---

## Task 8: Theme + navigation scaffold + MainActivity

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/gymtracker/ui/navigation/AppNavigation.kt`
- Create: `app/src/main/java/com/gymtracker/ui/MainActivity.kt`

- [ ] **Step 1: Create `Theme.kt`**

```kotlin
package com.gymtracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE94560),
    onPrimary = Color.White,
    secondary = Color(0xFF00B4D8),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF16213E),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun GymTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
```

- [ ] **Step 2: Create `AppNavigation.kt`**

```kotlin
package com.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gymtracker.ui.exercises.ExerciseDetailScreen
import com.gymtracker.ui.exercises.ExerciseListScreen
import com.gymtracker.ui.home.HomeScreen
import com.gymtracker.ui.photos.PhotosScreen
import com.gymtracker.ui.progress.ExerciseProgressScreen
import com.gymtracker.ui.progress.ProgressScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ExerciseList : Screen("exercises/{groupId}") {
        fun route(groupId: Long) = "exercises/$groupId"
    }
    object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun route(exerciseId: Long) = "exercise/$exerciseId"
    }
    object Progress : Screen("progress")
    object ExerciseProgress : Screen("progress/{exerciseId}") {
        fun route(exerciseId: Long) = "progress/$exerciseId"
    }
    object Photos : Screen("photos")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val topLevelRoutes = listOf(Screen.Home, Screen.Progress, Screen.Photos)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { screen ->
                    val (label, icon) = when (screen) {
                        Screen.Home -> "Ejercicios" to Icons.Default.FitnessCenter
                        Screen.Progress -> "Progresión" to Icons.Default.BarChart
                        Screen.Photos -> "Fotos" to Icons.Default.PhotoLibrary
                        else -> return@forEach
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(innerPadding, onGroupClick = { groupId ->
                    navController.navigate(Screen.ExerciseList.route(groupId))
                })
            }
            composable(
                Screen.ExerciseList.route,
                arguments = listOf(navArgument("groupId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseListScreen(
                    innerPadding,
                    groupId = backStackEntry.arguments!!.getLong("groupId"),
                    onExerciseClick = { exerciseId ->
                        navController.navigate(Screen.ExerciseDetail.route(exerciseId))
                    }
                )
            }
            composable(
                Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseDetailScreen(
                    exerciseId = backStackEntry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen(innerPadding, onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseProgress.route(exerciseId))
                })
            }
            composable(
                Screen.ExerciseProgress.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseProgressScreen(
                    exerciseId = backStackEntry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Photos.route) {
                PhotosScreen(innerPadding)
            }
        }
    }
}
```

- [ ] **Step 3: Create `MainActivity.kt`**

```kotlin
package com.gymtracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gymtracker.ui.navigation.AppNavigation
import com.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymTrackerTheme {
                AppNavigation()
            }
        }
    }
}
```

- [ ] **Step 4: Create placeholder composables** so the project compiles. Create each file with a minimal composable that just shows a `Text`:

  - `ui/home/HomeScreen.kt` — `@Composable fun HomeScreen(padding: PaddingValues, onGroupClick: (Long) -> Unit) { Box(Modifier.padding(padding)) { Text("Home") } }`
  - `ui/exercises/ExerciseListScreen.kt` — similar stub with `groupId: Long, onExerciseClick: (Long) -> Unit`
  - `ui/exercises/ExerciseDetailScreen.kt` — stub with `exerciseId: Long, onBack: () -> Unit`
  - `ui/progress/ProgressScreen.kt` — stub with `padding: PaddingValues, onExerciseClick: (Long) -> Unit`
  - `ui/progress/ExerciseProgressScreen.kt` — stub with `exerciseId: Long, onBack: () -> Unit`
  - `ui/photos/PhotosScreen.kt` — stub with `padding: PaddingValues`

- [ ] **Step 5: Build and run on emulator — verify app launches with bottom nav**

  Run → select emulator. Expected: app launches, bottom nav shows 3 tabs, each shows placeholder text.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/
git commit -m "feat: add theme, navigation scaffold and stub screens"
```

---

## Task 9: HomeScreen

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/home/HomeScreen.kt`

- [ ] **Step 1: Create `HomeViewModel.kt`**

```kotlin
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
    val isStale: Boolean   // true if no session logged in >7 days
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
```

- [ ] **Step 2: Implement `HomeScreen.kt`**

```kotlin
package com.gymtracker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.db.entity.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    onGroupClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(app.container.exerciseRepository, app.container.sessionRepository)
    )
    val groups by vm.groups.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("GymTracker", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir grupo muscular")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding).padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups) { item ->
                MuscleGroupCard(item = item, onClick = { onGroupClick(item.group.id) })
            }
        }
    }

    if (showAddDialog) {
        AddMuscleGroupDialog(
            onConfirm = { name, emoji ->
                // inserted via ViewModel — add function below
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun MuscleGroupCard(item: MuscleGroupUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isStale)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.group.emoji, fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(item.group.name, style = MaterialTheme.typography.titleMedium)
                if (item.isStale) {
                    Spacer(Modifier.height(4.dp))
                    Text("Sin registros recientes", fontSize = 10.sp, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
fun AddMuscleGroupDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💪") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo grupo muscular") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Emoji") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, emoji) }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
```

- [ ] **Step 3: Add `addGroup` to `HomeViewModel`**

  Inside `HomeViewModel`, add:
  ```kotlin
  fun addGroup(name: String, emoji: String) {
      viewModelScope.launch {
          exerciseRepo.insertGroup(MuscleGroup(name = name, emoji = emoji))
      }
  }
  ```
  Update the `onConfirm` lambda in `HomeScreen` to call `vm.addGroup(name, emoji)`.

- [ ] **Step 4: Run on emulator — verify grid shows 6 muscle groups from seed**

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/home/
git commit -m "feat: implement HomeScreen with muscle group grid and stale indicator"
```

---

## Task 10: ExerciseListScreen

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseListViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseListScreen.kt`

- [ ] **Step 1: Create `ExerciseListViewModel.kt`**

```kotlin
package com.gymtracker.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup
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
            exerciseRepo.insertExercise(Exercise(muscleGroupId = groupId, name = name, description = description))
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
```

- [ ] **Step 2: Implement `ExerciseListScreen.kt`**

```kotlin
package com.gymtracker.ui.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    padding: PaddingValues,
    groupId: Long,
    onExerciseClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ExerciseListViewModel = viewModel(
        key = "group_$groupId",
        factory = ExerciseListViewModel.Factory(groupId, app.container.exerciseRepository, app.container.sessionRepository)
    )
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Añadir ejercicio")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises, key = { it.exercise.id }) { item ->
                ExerciseRow(item = item, onClick = { onExerciseClick(item.exercise.id) })
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onConfirm = { name, description ->
                vm.addExercise(name, description)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun ExerciseRow(item: ExerciseUi, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.exercise.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (item.lastWeightKg != null) {
                Text(
                    "${item.lastSets}×${item.lastReps} · ${item.lastWeightKg}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddExerciseDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ejercicio") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, minLines = 3)
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, description) }) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
```

- [ ] **Step 3: Run on emulator — tap a muscle group, verify exercise list appears**

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/exercises/ExerciseListScreen.kt
git add app/src/main/java/com/gymtracker/ui/exercises/ExerciseListViewModel.kt
git commit -m "feat: implement ExerciseListScreen with last session stats"
```

---

## Task 11: ExerciseDetailScreen (+/- controls + voice + personal record)

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/exercises/ExerciseDetailScreen.kt`

- [ ] **Step 1: Create `ExerciseDetailViewModel.kt`**

```kotlin
package com.gymtracker.ui.exercises

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import com.gymtracker.domain.voice.ParsedSession
import com.gymtracker.domain.voice.SessionParser
import com.gymtracker.domain.voice.VoiceRecognizer
import com.gymtracker.domain.voice.VoiceResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val exercise: Exercise? = null,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isPersonalRecord: Boolean = false,
    val isListening: Boolean = false,
    val pendingParsed: ParsedSession? = null,   // waiting for user confirmation
    val voiceRawText: String = "",
    val justSaved: Boolean = false
)

class ExerciseDetailViewModel(
    private val exerciseId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepo.getExerciseById(exerciseId)
            val latest = sessionRepo.getLatestSession(exerciseId)
            _state.update {
                it.copy(
                    exercise = exercise,
                    sets = latest?.sets ?: 3,
                    reps = latest?.reps ?: 10,
                    weightKg = latest?.weightKg ?: 0f
                )
            }
        }
    }

    fun setSets(value: Int) = _state.update { it.copy(sets = maxOf(1, value)) }
    fun setReps(value: Int) = _state.update { it.copy(reps = maxOf(1, value)) }
    fun setWeight(value: Float) = _state.update { it.copy(weightKg = maxOf(0f, value)) }

    fun saveSession() {
        viewModelScope.launch {
            val s = _state.value
            val maxWeight = sessionRepo.getMaxWeight(exerciseId) ?: 0f
            val isRecord = s.weightKg > maxWeight
            sessionRepo.insertSession(
                Session(exerciseId = exerciseId, date = System.currentTimeMillis(),
                    sets = s.sets, reps = s.reps, weightKg = s.weightKg)
            )
            _state.update { it.copy(isPersonalRecord = isRecord, justSaved = true) }
        }
    }

    fun startVoice(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isListening = true) }
            VoiceRecognizer(context).listen().collect { result ->
                when (result) {
                    is VoiceResult.Success -> {
                        val parsed = SessionParser().parse(result.text)
                        _state.update {
                            it.copy(isListening = false, pendingParsed = parsed, voiceRawText = result.text)
                        }
                    }
                    is VoiceResult.Error -> _state.update { it.copy(isListening = false) }
                }
            }
        }
    }

    fun confirmVoiceSession(sets: Int, reps: Int, weightKg: Float) {
        _state.update { it.copy(sets = sets, reps = reps, weightKg = weightKg, pendingParsed = null) }
        saveSession()
    }

    fun dismissVoiceDialog() = _state.update { it.copy(pendingParsed = null) }
    fun dismissRecord() = _state.update { it.copy(isPersonalRecord = false, justSaved = false) }

    class Factory(
        private val exerciseId: Long,
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExerciseDetailViewModel(exerciseId, exerciseRepo, sessionRepo) as T
        }
    }
}
```

- [ ] **Step 2: Implement `ExerciseDetailScreen.kt`**

```kotlin
package com.gymtracker.ui.exercises

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(exerciseId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ExerciseDetailViewModel = viewModel(
        key = "detail_$exerciseId",
        factory = ExerciseDetailViewModel.Factory(exerciseId, app.container.exerciseRepository, app.container.sessionRepository)
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.startVoice(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exercise?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            state.exercise?.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            // +/- controls
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StepperField(label = "SERIES", value = state.sets,
                        onDecrement = { vm.setSets(state.sets - 1) },
                        onIncrement = { vm.setSets(state.sets + 1) })
                    StepperField(label = "REPS", value = state.reps,
                        onDecrement = { vm.setReps(state.reps - 1) },
                        onIncrement = { vm.setReps(state.reps + 1) })
                    StepperField(label = "KG", value = state.weightKg.toInt(),
                        onDecrement = { vm.setWeight(state.weightKg - 2.5f) },
                        onIncrement = { vm.setWeight(state.weightKg + 2.5f) })
                }
            }

            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isListening
                ) {
                    Icon(Icons.Default.Mic, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isListening) "Escuchando..." else "Voz")
                }
                OutlinedButton(onClick = { vm.saveSession() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar")
                }
            }
        }
    }

    // Voice confirmation dialog
    state.pendingParsed?.let { parsed ->
        var sets by remember { mutableStateOf(parsed.sets?.toString() ?: "") }
        var reps by remember { mutableStateOf(parsed.reps?.toString() ?: "") }
        var weight by remember { mutableStateOf(parsed.weightKg?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { vm.dismissVoiceDialog() },
            title = { Text("Confirmar sesión") },
            text = {
                Column {
                    Text("\"${state.voiceRawText}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sets, onValueChange = { sets = it },
                            label = { Text("Series") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reps, onValueChange = { reps = it },
                            label = { Text("Reps") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = weight, onValueChange = { weight = it },
                            label = { Text("Kg") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.confirmVoiceSession(
                        sets.toIntOrNull() ?: 0,
                        reps.toIntOrNull() ?: 0,
                        weight.toFloatOrNull() ?: 0f
                    )
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissVoiceDialog() }) { Text("Cancelar") } }
        )
    }

    // Personal record dialog
    if (state.isPersonalRecord && state.justSaved) {
        AlertDialog(
            onDismissRequest = { vm.dismissRecord() },
            title = { Text("🏆 ¡Nuevo récord personal!") },
            text = { Text("${state.weightKg}kg — tu mejor marca en este ejercicio.") },
            confirmButton = { TextButton(onClick = { vm.dismissRecord() }) { Text("Genial") } }
        )
    }
}

@Composable
fun StepperField(label: String, value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(4.dp))
        Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }
            FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
```

- [ ] **Step 3: Run on emulator — tap exercise, adjust +/- values, save, verify session saved**

- [ ] **Step 4: Test voice flow** — tap mic button, speak "press banca 3 series de 8 con 80 kilos", verify dialog shows parsed values.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/exercises/
git commit -m "feat: implement ExerciseDetailScreen with +/- controls, voice input and personal record detection"
```

---

## Task 12: ProgressScreen (group summary with mini charts)

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/progress/ProgressViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/progress/ProgressScreen.kt`

- [ ] **Step 1: Create `ProgressViewModel.kt`**

```kotlin
package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.MuscleGroup
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseProgressSummary(
    val exercise: Exercise,
    val sessions: List<Session>,
    val improvementPct: Float?   // null if <2 sessions
)

data class GroupProgressUi(
    val group: MuscleGroup,
    val exercises: List<ExerciseProgressSummary>
)

class ProgressViewModel(
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val groupProgress: StateFlow<List<GroupProgressUi>> =
        exerciseRepo.getAllGroups()
            .map { groups ->
                groups.map { group ->
                    val exercises = exerciseRepo.getExercisesForGroup(group.id).first()
                    val summaries = exercises.map { exercise ->
                        val sessions = sessionRepo.getSessionsForExercise(exercise.id).first()
                        val improvementPct = if (sessions.size >= 2) {
                            val first = sessions.last().weightKg
                            val last = sessions.first().weightKg
                            if (first > 0f) ((last - first) / first) * 100f else null
                        } else null
                        ExerciseProgressSummary(exercise, sessions.takeLast(8), improvementPct)
                    }.filter { it.sessions.isNotEmpty() }
                    GroupProgressUi(group, summaries)
                }.filter { it.exercises.isNotEmpty() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(exerciseRepo, sessionRepo) as T
        }
    }
}
```

- [ ] **Step 2: Implement `ProgressScreen.kt`**

```kotlin
package com.gymtracker.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.db.entity.Session

@Composable
fun ProgressScreen(padding: PaddingValues, onExerciseClick: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ProgressViewModel = viewModel(
        factory = ProgressViewModel.Factory(app.container.exerciseRepository, app.container.sessionRepository)
    )
    val groups by vm.groupProgress.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groups.forEach { groupUi ->
            item { Text(groupUi.group.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(groupUi.exercises) { summary ->
                ExerciseProgressRow(summary = summary, onClick = { onExerciseClick(summary.exercise.id) })
            }
        }
    }
}

@Composable
fun ExerciseProgressRow(summary: ExerciseProgressSummary, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.exercise.name, style = MaterialTheme.typography.titleSmall)
                summary.improvementPct?.let {
                    val color = if (it >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
                    Text(
                        "${if (it >= 0) "+" else ""}${"%.1f".format(it)}%",
                        fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (summary.sessions.size >= 2) {
                MiniSparklineChart(sessions = summary.sessions, modifier = Modifier.size(width = 80.dp, height = 40.dp))
            }
        }
    }
}

@Composable
fun MiniSparklineChart(sessions: List<Session>, modifier: Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                xAxis.isEnabled = false
                setDrawGridBackground(false)
                setBackgroundColor(Color.Transparent.toArgb())
            }
        },
        update = { chart ->
            val entries = sessions.mapIndexed { i, s -> Entry(i.toFloat(), s.weightKg) }
            val dataSet = LineDataSet(entries, "").apply {
                color = primaryColor
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/progress/ProgressScreen.kt
git add app/src/main/java/com/gymtracker/ui/progress/ProgressViewModel.kt
git commit -m "feat: implement ProgressScreen with group summaries and mini sparkline charts"
```

---

## Task 13: ExerciseProgressScreen (full chart)

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/progress/ExerciseProgressViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/progress/ExerciseProgressScreen.kt`

- [ ] **Step 1: Create `ExerciseProgressViewModel.kt`**

```kotlin
package com.gymtracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.Exercise
import com.gymtracker.data.db.entity.Session
import com.gymtracker.data.repository.ExerciseRepository
import com.gymtracker.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ProgressMetric { MAX_WEIGHT, VOLUME, ONE_RM }

data class ExerciseProgressState(
    val exercise: Exercise? = null,
    val sessions: List<Session> = emptyList(),
    val metric: ProgressMetric = ProgressMetric.MAX_WEIGHT
)

class ExerciseProgressViewModel(
    private val exerciseId: Long,
    private val exerciseRepo: ExerciseRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _metric = MutableStateFlow(ProgressMetric.MAX_WEIGHT)

    val state: StateFlow<ExerciseProgressState> = combine(
        sessionRepo.getSessionsForExercise(exerciseId),
        _metric
    ) { sessions, metric ->
        ExerciseProgressState(
            exercise = exerciseRepo.getExerciseById(exerciseId),
            sessions = sessions,
            metric = metric
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseProgressState())

    fun setMetric(metric: ProgressMetric) { _metric.value = metric }

    class Factory(
        private val exerciseId: Long,
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExerciseProgressViewModel(exerciseId, exerciseRepo, sessionRepo) as T
        }
    }
}

fun Session.metricValue(metric: ProgressMetric): Float = when (metric) {
    ProgressMetric.MAX_WEIGHT -> weightKg
    ProgressMetric.VOLUME -> sets * reps * weightKg
    ProgressMetric.ONE_RM -> if (reps > 0) weightKg * (1 + reps / 30f) else weightKg
}
```

- [ ] **Step 2: Implement `ExerciseProgressScreen.kt`**

```kotlin
package com.gymtracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.gymtracker.GymTrackerApp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(exerciseId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: ExerciseProgressViewModel = viewModel(
        key = "progress_$exerciseId",
        factory = ExerciseProgressViewModel.Factory(exerciseId, app.container.exerciseRepository, app.container.sessionRepository)
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exercise?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Metric filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressMetric.values().forEach { metric ->
                    val label = when (metric) {
                        ProgressMetric.MAX_WEIGHT -> "Peso máx"
                        ProgressMetric.VOLUME -> "Volumen"
                        ProgressMetric.ONE_RM -> "1RM est."
                    }
                    FilterChip(
                        selected = state.metric == metric,
                        onClick = { vm.setMetric(metric) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.sessions.size < 2) {
                Text("Necesitas al menos 2 sesiones para ver la gráfica.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                AndroidView(
                    factory = { ctx ->
                        LineChart(ctx).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setBackgroundColor(surfaceColor)
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.textColor = onSurfaceColor
                            axisLeft.textColor = onSurfaceColor
                            axisRight.isEnabled = false
                            setDrawGridBackground(false)
                        }
                    },
                    update = { chart ->
                        val sorted = state.sessions.sortedBy { it.date }
                        val entries = sorted.mapIndexed { i, s ->
                            Entry(i.toFloat(), s.metricValue(state.metric))
                        }
                        val labels = sorted.map { dateFormat.format(it.date) }
                        chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)

                        val dataSet = LineDataSet(entries, "").apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            circleRadius = 4f
                            lineWidth = 2.5f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                        }
                        chart.data = LineData(dataSet)
                        chart.invalidate()
                    },
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/progress/
git commit -m "feat: implement ExerciseProgressScreen with full MPAndroidChart and metric filters"
```

---

## Task 14: PhotosScreen

**Files:**
- Create: `app/src/main/java/com/gymtracker/ui/photos/PhotosViewModel.kt`
- Modify: `app/src/main/java/com/gymtracker/ui/photos/PhotosScreen.kt`

- [ ] **Step 1: Create `PhotosViewModel.kt`**

```kotlin
package com.gymtracker.ui.photos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.db.entity.BodyPhoto
import com.gymtracker.data.db.entity.BodyZone
import com.gymtracker.data.repository.BodyPhotoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class PhotosViewModel(private val repo: BodyPhotoRepository) : ViewModel() {

    private val _selectedZone = MutableStateFlow<BodyZone?>(null)

    val photos: StateFlow<List<BodyPhoto>> = _selectedZone.flatMapLatest { zone ->
        if (zone == null) repo.getAllPhotos() else repo.getPhotosByZone(zone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedZone: StateFlow<BodyZone?> = _selectedZone.asStateFlow()

    fun selectZone(zone: BodyZone?) { _selectedZone.value = zone }

    fun savePhoto(context: Context, uri: Uri, zone: BodyZone) {
        viewModelScope.launch {
            val destFile = File(context.filesDir, "body_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            repo.insertPhoto(BodyPhoto(date = System.currentTimeMillis(), zone = zone, photoPath = destFile.absolutePath))
        }
    }

    class Factory(private val repo: BodyPhotoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PhotosViewModel(repo) as T
        }
    }
}
```

- [ ] **Step 2: Implement `PhotosScreen.kt`**

```kotlin
package com.gymtracker.ui.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.db.entity.BodyZone
import java.io.File

@Composable
fun PhotosScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as GymTrackerApp
    val vm: PhotosViewModel = viewModel(
        factory = PhotosViewModel.Factory(app.container.bodyPhotoRepository)
    )
    val photos by vm.photos.collectAsStateWithLifecycle()
    val selectedZone by vm.selectedZone.collectAsStateWithLifecycle()
    var showZonePicker by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pendingUri = it; showZonePicker = true }
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Default.Add, "Añadir foto")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Zone filter chips
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(selected = selectedZone == null, onClick = { vm.selectZone(null) }, label = { Text("Todo") })
                BodyZone.values().forEach { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick = { vm.selectZone(if (selectedZone == zone) null else zone) },
                        label = { Text(zone.displayName()) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    AsyncImage(
                        model = File(photo.photoPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }

    if (showZonePicker && pendingUri != null) {
        var chosenZone by remember { mutableStateOf(BodyZone.FULL_BODY) }
        AlertDialog(
            onDismissRequest = { showZonePicker = false },
            title = { Text("Zona corporal") },
            text = {
                Column {
                    BodyZone.values().forEach { zone ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected = chosenZone == zone, onClick = { chosenZone = zone })
                            Text(zone.displayName())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingUri?.let { vm.savePhoto(context, it, chosenZone) }
                    showZonePicker = false
                    pendingUri = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showZonePicker = false }) { Text("Cancelar") } }
        )
    }
}

fun BodyZone.displayName(): String = when (this) {
    BodyZone.FULL_BODY -> "Cuerpo entero"
    BodyZone.CHEST -> "Pecho"
    BodyZone.BACK -> "Espalda"
    BodyZone.ARMS -> "Brazos"
    BodyZone.LEGS -> "Piernas"
    BodyZone.SHOULDERS -> "Hombros"
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/gymtracker/ui/photos/
git commit -m "feat: implement PhotosScreen with zone filter and photo picker"
```

---

## Task 15: CSV export

**Files:**
- Modify: `app/src/main/java/com/gymtracker/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/com/gymtracker/data/repository/CsvExporter.kt`

- [ ] **Step 1: Create `CsvExporter.kt`**

```kotlin
package com.gymtracker.data.repository

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.gymtracker.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    suspend fun export(context: Context, db: AppDatabase): Intent = withContext(Dispatchers.IO) {
        val groups = db.muscleGroupDao().getAll().first()
        val sb = StringBuilder("fecha,ejercicio,grupo_muscular,series,repeticiones,peso_kg\n")

        groups.forEach { group ->
            db.exerciseDao().getByGroup(group.id).first().forEach { exercise ->
                db.sessionDao().getByExercise(exercise.id).first().forEach { session ->
                    sb.append("${dateFormat.format(Date(session.date))},")
                    sb.append("\"${exercise.name}\",")
                    sb.append("\"${group.name}\",")
                    sb.append("${session.sets},${session.reps},${session.weightKg}\n")
                }
            }
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "gymtracker_export.csv")
        file.writeText(sb.toString())

        Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
```

- [ ] **Step 2: Add FileProvider to `AndroidManifest.xml`**

  Inside `<application>`, add:
  ```xml
  <provider
      android:name="androidx.core.content.FileProvider"
      android:authorities="${applicationId}.provider"
      android:exported="false"
      android:grantUriPermissions="true">
      <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/file_paths" />
  </provider>
  ```

- [ ] **Step 3: Create `app/src/main/res/xml/file_paths.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="documents" path="Documents/" />
</paths>
```

- [ ] **Step 4: Add overflow menu to `HomeScreen.kt`**

  In the `TopAppBar`, add `actions`:
  ```kotlin
  actions = {
      var menuExpanded by remember { mutableStateOf(false) }
      IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, "Menú") }
      DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
          DropdownMenuItem(
              text = { Text("Exportar CSV") },
              onClick = {
                  menuExpanded = false
                  scope.launch {
                      val intent = CsvExporter.export(context, AppDatabase.getInstance(context))
                      context.startActivity(Intent.createChooser(intent, "Exportar datos"))
                  }
              }
          )
      }
  }
  ```
  Add `val scope = rememberCoroutineScope()` at the top of `HomeScreen`.

- [ ] **Step 5: Build, run, verify CSV export shares a valid file**

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/gymtracker/data/repository/CsvExporter.kt
git add app/src/main/res/xml/file_paths.xml
git add app/src/main/AndroidManifest.xml
git commit -m "feat: add CSV export via share intent"
```

---

## Task 16: Final build verification

- [ ] **Step 1: Run all unit tests**

  ```bash
  ./gradlew test
  ```
  Expected: SessionParserTest — 6 tests PASS.

- [ ] **Step 2: Run instrumented tests on emulator**

  ```bash
  ./gradlew connectedAndroidTest
  ```
  Expected: SessionDaoTest — 2 tests PASS.

- [ ] **Step 3: Build release APK**

  ```bash
  ./gradlew assembleDebug
  ```
  Expected: `app/build/outputs/apk/debug/app-debug.apk` generated.

- [ ] **Step 4: Install and smoke test on device**

  - Home screen: 6 muscle groups visible, no stale indicator (no sessions yet)
  - Tap Pecho → list of 3 exercises
  - Tap Press Banca → +/- controls, mic button
  - Adjust to 3 series / 8 reps / 80 kg → Guardar → no record alert (first session)
  - Adjust to 4 series / 8 reps / 85 kg → Guardar → record alert shows
  - Back → Progresión tab → Pecho → Press Banca shows sessions
  - Fotos tab → add a photo → select zone → photo appears in grid
  - Exportar CSV → share sheet opens

- [ ] **Step 5: Final commit**

```bash
git add .
git commit -m "feat: GymTracker v1.0 complete"
```
