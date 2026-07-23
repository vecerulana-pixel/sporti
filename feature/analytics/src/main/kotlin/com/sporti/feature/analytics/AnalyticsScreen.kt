package com.sporti.feature.analytics

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sporti.core.designsystem.component.SectionHeader
import com.sporti.core.designsystem.component.SportiCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnalyticsRoute(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AnalyticsScreen(state, modifier)
}

@Composable
private fun AnalyticsScreen(state: AnalyticsUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(stringResource(com.sporti.feature.analytics.R.string.analytics_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(com.sporti.feature.analytics.R.string.analytics_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(stringResource(com.sporti.feature.analytics.R.string.training_time), formatDuration(state.totalTrainingMillis), Modifier.weight(1f), true)
                MetricCard(stringResource(com.sporti.feature.analytics.R.string.sessions), state.sessionsCount.toString(), Modifier.weight(1f), false)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(stringResource(com.sporti.feature.analytics.R.string.best_lap), state.bestLapMillis?.let(::formatStopwatch) ?: "—", Modifier.weight(1f), false)
                MetricCard(stringResource(com.sporti.feature.analytics.R.string.saved), state.favoritesCount.toString(), Modifier.weight(1f), false)
            }
        }
        item {
            SectionHeader(stringResource(com.sporti.feature.analytics.R.string.week_activity))
            SportiCard(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(Modifier.padding(18.dp)) {
                    WeeklyChart(state.weekDurationsMillis, Modifier.fillMaxWidth().height(170.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        weekLabels().forEach { label -> Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center) }
                    }
                }
            }
        }
        item {
            SectionHeader(stringResource(com.sporti.feature.analytics.R.string.personal_base))
            SportiCard(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DataRow(stringResource(com.sporti.feature.analytics.R.string.notes_created), state.notesCount.toString())
                    DataRow(stringResource(com.sporti.feature.analytics.R.string.favorite_materials), state.favoritesCount.toString())
                    DataRow(stringResource(com.sporti.feature.analytics.R.string.total_laps_hint), if (state.bestLapMillis == null) stringResource(com.sporti.feature.analytics.R.string.no_data) else stringResource(com.sporti.feature.analytics.R.string.data_ready))
                }
            }
        }
        item {
            Text(stringResource(com.sporti.feature.analytics.R.string.analytics_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SportiCard(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(stringResource(com.sporti.feature.analytics.R.string.privacy_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(com.sporti.feature.analytics.R.string.privacy_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, "https://vecerulana-pixel.github.io/sporti/".toUri()),
                            )
                        },
                    ) {
                        Text(stringResource(com.sporti.feature.analytics.R.string.open))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier, primary: Boolean) {
    SportiCard(modifier.height(120.dp)) {
        Column(
            Modifier.fillMaxSize().background(if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (primary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun WeeklyChart(values: List<Long>, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier) {
        val max = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        val slot = size.width / values.size
        val barWidth = slot * 0.48f
        values.forEachIndexed { index, value ->
            val x = slot * index + (slot - barWidth) / 2
            drawRoundRect(track, Offset(x, 0f), Size(barWidth, size.height), CornerRadius(barWidth / 2))
            val height = if (value == 0L) 4.dp.toPx() else size.height * (value.toFloat() / max)
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.7f), accent)),
                topLeft = Offset(x, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun formatDuration(value: Long): String {
    val hours = value / 3_600_000
    val minutes = value / 60_000 % 60
    return if (hours > 0) {
        stringResource(com.sporti.feature.analytics.R.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(com.sporti.feature.analytics.R.string.duration_minutes, minutes)
    }
}

private fun formatStopwatch(value: Long): String = String.format(Locale.ENGLISH, "%02d:%02d.%02d", value / 60_000, value / 1_000 % 60, value / 10 % 100)

private fun weekLabels(): List<String> {
    val formatter = DateTimeFormatter.ofPattern("EE", Locale.ENGLISH)
    val today = LocalDate.now()
    return (6 downTo 0).map { today.minusDays(it.toLong()).format(formatter).take(2).uppercase(Locale.ENGLISH) }
}
