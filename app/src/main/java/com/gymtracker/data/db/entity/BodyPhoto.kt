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
