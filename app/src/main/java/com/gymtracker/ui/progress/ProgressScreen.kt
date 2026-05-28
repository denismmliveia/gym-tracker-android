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
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groups.forEach { groupUi ->
            item {
                Text(
                    groupUi.group.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(groupUi.exercises) { summary ->
                ExerciseProgressRow(
                    summary = summary,
                    onClick = { onExerciseClick(summary.exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressRow(summary: ExerciseProgressSummary, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.exercise.name, style = MaterialTheme.typography.titleSmall)
                summary.improvementPct?.let {
                    val color = if (it >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
                    Text(
                        "${if (it >= 0) "+" else ""}${"%.1f".format(it)}%",
                        fontSize = 12.sp,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (summary.sessions.size >= 2) {
                MiniSparklineChart(
                    sessions = summary.sessions,
                    modifier = Modifier.size(width = 80.dp, height = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniSparklineChart(sessions: List<Session>, modifier: Modifier) {
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
