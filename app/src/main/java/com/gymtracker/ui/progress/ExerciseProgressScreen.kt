package com.gymtracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
                ProgressMetric.entries.forEach { metric ->
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
                Text(
                    "Necesitas al menos 2 sesiones para ver la gráfica.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
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
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

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
