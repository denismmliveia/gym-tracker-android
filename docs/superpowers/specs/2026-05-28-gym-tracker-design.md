# GymTracker — Diseño de la aplicación Android

**Fecha:** 2026-05-28
**Stack:** Kotlin + Jetpack Compose + Room (SQLite) + MVVM
**Plataforma:** Android (uso personal, sin costes de API, datos 100% locales — voz requiere internet)

---

## Objetivo

App personal para registrar y visualizar la progresión en ejercicios de gimnasio. El foco es la progresión por ejercicio, no la planificación de calendario.

---

## Arquitectura

### Stack técnico

| Componente | Tecnología |
|---|---|
| UI | Jetpack Compose |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| Base de datos | Room (SQLite) |
| Gráficas | MPAndroidChart |
| Reconocimiento de voz | Android SpeechRecognizer (Google, gratuito) |
| Cámara / galería | CameraX + Photo Picker API |
| Navegación | Navigation Compose con bottom nav |

### Estructura de módulos

```
app/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── entities/          (MuscleGroup, Exercise, Session, BodyPhoto)
│   │   └── dao/               (MuscleGroupDao, ExerciseDao, SessionDao, BodyPhotoDao)
│   └── repository/
│       ├── ExerciseRepository.kt
│       ├── SessionRepository.kt
│       └── BodyPhotoRepository.kt
├── domain/
│   └── voice/
│       ├── VoiceRecognizer.kt    (wrapper SpeechRecognizer)
│       └── SessionParser.kt      (regex parser español)
└── ui/
    ├── home/                  HomeScreen + HomeViewModel
    ├── exercises/             ExerciseListScreen, ExerciseDetailScreen + ViewModels
    ├── progress/              ProgressScreen, ExerciseProgressScreen + ViewModels
    └── photos/                PhotosScreen, PhotoViewerScreen + ViewModels
```

---

## Modelo de datos

```kotlin
@Entity
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,       // "Pecho", "Espalda", etc.
    val emoji: String       // "🏋️", "💪", etc.
)

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val muscleGroupId: Long,
    val name: String,
    val description: String,
    val photoPath: String?  // ruta interna de la app
)

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val date: Long,         // epoch ms
    val sets: Int,
    val reps: Int,
    val weightKg: Float
)

@Entity
data class BodyPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val zone: BodyZone,     // enum: FULL_BODY, CHEST, BACK, ARMS, LEGS, SHOULDERS
    val photoPath: String
)
```

Cada actualización de un ejercicio crea un nuevo `Session`. Esto permite historial completo y gráficas de progresión.

---

## Pantallas y navegación

### Bottom Navigation Bar (3 tabs)
- **Ejercicios** — pantalla principal
- **Progresión** — gráficas
- **Fotos** — álbum corporal

### Flujo de pantallas

```
HomeScreen
  Grid de grupos musculares con emoji
  └── ExerciseListScreen
        Lista de ejercicios del grupo
        └── ExerciseDetailScreen
              Foto del ejercicio + descripción
              Stats última sesión (sets / reps / peso)
              Controles +/- para ajuste rápido
              Botón de voz prominente
              Botón editar (formulario completo)

ProgressScreen
  Resumen por grupo muscular con mini-gráficas en línea
  % de mejora por ejercicio
  └── ExerciseProgressScreen
        Gráfica completa de progresión
        Filtros: peso máximo | volumen total | 1RM estimado

PhotosScreen
  Filtros por zona: Todo el cuerpo / Pecho / Espalda / Brazos / Piernas / Hombros
  Grid cronológico de fotos
  Botón añadir nueva foto (cámara o galería)
  └── PhotoViewerScreen
        Foto ampliada con fecha y zona
```

---

## Funcionalidad de voz

### Flujo completo
1. Usuario pulsa botón de micrófono en ExerciseDetailScreen
2. Se lanza Android SpeechRecognizer (requiere internet para Google on-device, pero sin API key ni coste)
3. El texto reconocido pasa por `SessionParser`
4. Se muestra diálogo de confirmación con los datos parseados
5. Usuario confirma → se guarda `Session`. Cancela → no se guarda nada

### Parser regex (español)

Ejemplos reconocidos:
```
"press banca, 3 series de 8 con 80 kilos"    → sets=3, reps=8, weight=80
"sentadilla 4 por 10 a 60"                   → sets=4, reps=10, weight=60
"dominadas, 5 series de 6 sin peso"          → sets=5, reps=6, weight=0
"peso muerto 100 kilos, 3 de 5"              → sets=3, reps=5, weight=100
```

Si el parser no puede extraer todos los campos, muestra el texto reconocido en un formulario pre-rellenado con lo que sí se pudo parsear. Nunca guarda sin confirmación explícita del usuario.

---

## Funcionalidades adicionales

### Récord personal automático
- Al guardar una sesión, se compara el peso con el máximo histórico del ejercicio
- Si se supera, se muestra un badge/animación de récord en ExerciseDetailScreen
- El récord queda marcado visualmente en la gráfica de progresión

### Indicador de días sin entrenar
- En HomeScreen, cada tarjeta de grupo muscular muestra un indicador sutil si el ejercicio más reciente de ese grupo tiene más de 7 días sin sesión registrada
- No es intrusivo: un punto de color o el color de la tarjeta ligeramente diferente

### Exportar datos a CSV
- Botón en ajustes (menú overflow en HomeScreen)
- Exporta todas las sesiones a un CSV en la carpeta Descargas del dispositivo
- Formato: `fecha, ejercicio, grupo_muscular, series, repeticiones, peso_kg`

---

## Permisos Android requeridos

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.INTERNET" />  <!-- SpeechRecognizer -->
```

---

## Datos iniciales (seed)

La app incluye grupos musculares y ejercicios de ejemplo pre-cargados para que sea usable desde el primer arranque:

- **Pecho:** Press banca, Aperturas, Fondos
- **Espalda:** Dominadas, Remo con barra, Jalón al pecho
- **Piernas:** Sentadilla, Prensa, Extensiones
- **Hombros:** Press militar, Elevaciones laterales
- **Bíceps:** Curl con barra, Curl martillo
- **Tríceps:** Fondos en paralelas, Press francés

El usuario puede añadir, editar y eliminar grupos y ejercicios libremente.

---

## Fuera de alcance (v1)

- Planificación de rutinas semanales / calendario
- Sincronización en la nube
- Compartir progresión con otros usuarios
- Temporizador de descanso entre series
- Videos de ejercicios
