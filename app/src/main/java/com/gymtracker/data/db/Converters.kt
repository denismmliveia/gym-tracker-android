package com.gymtracker.data.db

import androidx.room.TypeConverter
import com.gymtracker.data.db.entity.BodyZone

class Converters {
    @TypeConverter
    fun fromBodyZone(zone: BodyZone): String = zone.name

    @TypeConverter
    fun toBodyZone(value: String): BodyZone =
        BodyZone.entries.firstOrNull { it.name == value } ?: BodyZone.FULL_BODY
}
