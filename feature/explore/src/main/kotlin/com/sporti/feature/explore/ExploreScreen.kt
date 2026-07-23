package com.sporti.feature.explore

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sporti.core.designsystem.R
import com.sporti.core.designsystem.component.EmptyState
import com.sporti.core.designsystem.component.SportiCard
import com.sporti.core.designsystem.component.StatusPill
import com.sporti.core.domain.model.EventStatus
import com.sporti.core.domain.model.NewsArticle
import com.sporti.core.domain.model.SportEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExploreRoute(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreen(state, viewModel::refresh, viewModel::selectLeague, viewModel::toggleFavorite, viewModel::toggleFavorite, modifier)
}

@Composable
private fun ExploreScreen(
    state: ExploreUiState,
    onRefresh: () -> Unit,
    onSelectLeague: (String?) -> Unit,
    onFavoriteEvent: (SportEvent) -> Unit,
    onFavoriteNews: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 22.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(com.sporti.feature.explore.R.string.explore_title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(com.sporti.feature.explore.R.string.explore_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                if (state.isRefreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(painterResource(R.drawable.ic_refresh), contentDescription = stringResource(com.sporti.feature.explore.R.string.refresh))
            }
        }
        if (state.hasRefreshError) StatusPill(stringResource(com.sporti.feature.explore.R.string.refresh_error), false, Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(com.sporti.feature.explore.R.string.matches)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(com.sporti.feature.explore.R.string.news)) })
        }
        if (tab == 0) MatchesTab(state, onSelectLeague, onFavoriteEvent) else NewsTab(state.news, onFavoriteNews)
    }
}

@Composable
private fun MatchesTab(
    state: ExploreUiState,
    onSelectLeague: (String?) -> Unit,
    onFavorite: (SportEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { FilterChip(selected = state.selectedLeague == null, onClick = { onSelectLeague(null) }, label = { Text(stringResource(com.sporti.feature.explore.R.string.all_leagues)) }) }
                items(state.leagues) { league ->
                    FilterChip(selected = state.selectedLeague == league, onClick = { onSelectLeague(league) }, label = { Text(league, maxLines = 1) })
                }
            }
        }
        if (state.visibleEvents.isEmpty()) {
            item { EmptyState(stringResource(com.sporti.feature.explore.R.string.empty_matches_title), stringResource(com.sporti.feature.explore.R.string.empty_matches_body)) }
        }
        items(state.visibleEvents, key = SportEvent::id) { event ->
            MatchCard(event, { onFavorite(event) }, Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun MatchCard(event: SportEvent, onFavorite: () -> Unit, modifier: Modifier = Modifier) {
    SportiCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    text = when (event.status) {
                        EventStatus.LIVE -> stringResource(com.sporti.feature.explore.R.string.live)
                        EventStatus.FINISHED -> stringResource(com.sporti.feature.explore.R.string.finished)
                        EventStatus.UPCOMING -> formatDate(event.startTimeMillis)
                    },
                    emphasized = event.status == EventStatus.LIVE,
                )
                Text(event.league, modifier = Modifier.weight(1f).padding(horizontal = 10.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onFavorite) {
                    Icon(
                        painterResource(R.drawable.ic_heart),
                        contentDescription = stringResource(if (event.isFavorite) com.sporti.feature.explore.R.string.remove_favorite else com.sporti.feature.explore.R.string.add_favorite),
                        tint = if (event.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TeamScoreRow(event.homeTeam, event.homeScore, emphasized = true)
            TeamScoreRow(event.awayTeam, event.awayScore, emphasized = false)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.venue ?: stringResource(com.sporti.feature.explore.R.string.venue_unknown), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatTime(event.startTimeMillis), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun TeamScoreRow(team: String, score: Int?, emphasized: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Text(team.take(1).uppercase(), fontWeight = FontWeight.Black, color = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(team, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.titleMedium)
        Text(score?.toString() ?: "—", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun NewsTab(news: List<NewsArticle>, onFavorite: (NewsArticle) -> Unit) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (news.isEmpty()) item { EmptyState(stringResource(com.sporti.feature.explore.R.string.empty_news_title), stringResource(com.sporti.feature.explore.R.string.empty_news_body)) }
        items(news, key = NewsArticle::id) { article ->
            NewsCard(
                article = article,
                onFavorite = { onFavorite(article) },
                onOpen = { context.startActivity(Intent(Intent.ACTION_VIEW, article.url.toUri())) },
            )
        }
        item { Spacer(Modifier.height(92.dp)) }
    }
}

@Composable
private fun NewsCard(article: NewsArticle, onFavorite: () -> Unit, onOpen: () -> Unit) {
    SportiCard(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column {
            article.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = article.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16 / 8f).clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(article.source.uppercase(Locale.getDefault()), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = onFavorite) {
                        Icon(painterResource(R.drawable.ic_heart), contentDescription = stringResource(if (article.isFavorite) com.sporti.feature.explore.R.string.remove_favorite else com.sporti.feature.explore.R.string.add_favorite), tint = if (article.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(article.title, style = MaterialTheme.typography.titleLarge)
                if (article.summary.isNotBlank()) Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(formatDate(article.publishedAtMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM, EEE", Locale.forLanguageTag("ru"))
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private fun formatDate(value: Long): String = if (value == 0L) "" else Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(dateFormatter)
private fun formatTime(value: Long): String = if (value == 0L) "—" else Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(timeFormatter)
