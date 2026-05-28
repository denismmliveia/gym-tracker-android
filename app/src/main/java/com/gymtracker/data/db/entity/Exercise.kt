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
