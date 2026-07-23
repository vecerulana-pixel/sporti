package com.sporti.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sporti.core.designsystem.R
import com.sporti.core.designsystem.component.SectionHeader
import com.sporti.core.designsystem.component.SportiButtonShape
import com.sporti.core.designsystem.component.SportiCard
import com.sporti.core.designsystem.component.StatusPill
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.SportEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeRoute(
    onOpenExplore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stopwatch by viewModel.stopwatch.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        stopwatch = stopwatch,
        onRefresh = viewModel::refresh,
        onToggleStopwatch = viewModel::toggleStopwatch,
        onLap = viewModel::addLap,
        onReset = viewModel::reset,
        onOpenExplore = onOpenExplore,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    stopwatch: StopwatchState,
    onRefresh: () -> Unit,
    onToggleStopwatch: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    onOpenExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("S", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(id = com.sporti.feature.home.R.string.home_greeting), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(com.sporti.feature.home.R.string.brand_name), style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onRefresh, enabled = !uiState.isRefreshing) {
                    if (uiState.isRefreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Icon(painterResource(R.drawable.ic_refresh), contentDescription = stringResource(com.sporti.feature.home.R.string.refresh))
                }
            }
        }
        if (uiState.hasRefreshError) {
            item { StatusPill(stringResource(com.sporti.feature.home.R.string.refresh_error), emphasized = false) }
        }
        item {
            StopwatchCard(stopwatch, onToggleStopwatch, onLap, onReset)
        }
        item {
            SectionHeader(stringResource(com.sporti.feature.home.R.string.next_matches)) {
                Text(
                    stringResource(com.sporti.feature.home.R.string.all),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onOpenExplore).padding(12.dp),
                )
            }
        }
        if (uiState.events.isEmpty() && !uiState.isRefreshing) {
            item { Text(stringResource(com.sporti.feature.home.R.string.no_matches), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            itemsIndexed(uiState.events, key = { _, event -> event.id }) { _, event -> HomeMatchRow(event) }
        }
        item {
            SectionHeader(stringResource(com.sporti.feature.home.R.string.latest_news)) {
                OutlinedButton(onClick = onOpenExplore, shape = SportiButtonShape) { Text(stringResource(com.sporti.feature.home.R.string.read_all)) }
            }
        }
        itemsIndexed(uiState.news, key = { _, article -> article.id }) { _, article -> HomeNewsRow(article) }
        item {
            Text(
                stringResource(com.sporti.feature.home.R.string.data_sources),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StopwatchCard(
    state: StopwatchState,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    val timerDescription = stringResource(com.sporti.feature.home.R.string.timer_description)
    SportiCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(com.sporti.feature.home.R.string.coach_timer), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(com.sporti.feature.home.R.string.interval_training), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusPill(if (state.isRunning) stringResource(com.sporti.feature.home.R.string.running) else stringResource(com.sporti.feature.home.R.string.ready), state.isRunning)
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.size(210.dp).semantics { contentDescription = timerDescription }, contentAlignment = Alignment.Center) {
                val track = MaterialTheme.colorScheme.surfaceVariant
                val accent = MaterialTheme.colorScheme.primary
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(track, -90f, 360f, false, style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                    val progress = (state.elapsedMillis % 60_000L) / 60_000f
                    drawArc(accent, -90f, progress * 360f, false, style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                    drawCircle(accent.copy(alpha = 0.16f), radius = size.minDimension * 0.34f, center = Offset(size.width / 2, size.height / 2))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatStopwatch(state.elapsedMillis), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(com.sporti.feature.home.R.string.laps_count, state.lapsMillis.size), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReset, enabled = state.elapsedMillis > 0, modifier = Modifier.weight(1f), shape = SportiButtonShape) {
                    Text(stringResource(com.sporti.feature.home.R.string.reset))
                }
                Button(onClick = onToggle, modifier = Modifier.weight(1.2f), shape = SportiButtonShape) {
                    Icon(painterResource(if (state.isRunning) R.drawable.ic_pause else R.drawable.ic_play), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (state.isRunning) stringResource(com.sporti.feature.home.R.string.stop) else stringResource(com.sporti.feature.home.R.string.start), modifier = Modifier.padding(start = 8.dp))
                }
                Button(
                    onClick = onLap,
                    enabled = state.isRunning,
                    modifier = Modifier.weight(1f),
                    shape = SportiButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Icon(painterResource(R.drawable.ic_flag), contentDescription = null, modifier = Modifier.size(17.dp))
                    Text(stringResource(com.sporti.feature.home.R.string.lap), modifier = Modifier.padding(start = 6.dp))
                }
            }
            state.lapsMillis.takeLast(3).reversed().forEachIndexed { index, lap ->
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(com.sporti.feature.home.R.string.lap_number, state.lapsMillis.size - index), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatStopwatch(lap), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HomeMatchRow(event: SportEvent) {
    SportiCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(event.league.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("${event.homeTeam} — ${event.awayTeam}", style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                event.venue?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatEventDate(event.startTimeMillis), style = MaterialTheme.typography.labelLarge)
                Text(formatEventTime(event.startTimeMillis), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HomeNewsRow(article: NewsArticle) {
    SportiCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(article.source.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Text(formatNewsDate(article.publishedAtMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(article.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (article.summary.isNotBlank()) Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatStopwatch(value: Long): String {
    val minutes = value / 60_000
    val seconds = value / 1_000 % 60
    val hundredths = value / 10 % 100
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}

private val eventDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("ru"))
private val eventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private fun formatEventDate(value: Long): String = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(eventDateFormatter)
private fun formatEventTime(value: Long): String = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(eventTimeFormatter)
private fun formatNewsDate(value: Long): String = if (value == 0L) "" else Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(eventDateFormatter)
