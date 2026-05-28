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
