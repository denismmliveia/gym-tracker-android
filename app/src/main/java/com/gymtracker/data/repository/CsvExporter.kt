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
