package com.sporti.feature.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sporti.core.designsystem.R
import com.sporti.core.designsystem.component.EmptyState
import com.sporti.core.designsystem.component.SportiButtonShape
import com.sporti.core.designsystem.component.SportiCard
import com.sporti.core.designsystem.component.StatusPill
import com.sporti.core.domain.model.Favorite
import com.sporti.core.domain.model.FavoriteType
import com.sporti.core.domain.model.Note
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(state, viewModel::saveNote, viewModel::deleteNote, viewModel::removeFavorite, modifier)
}

@Composable
private fun LibraryScreen(
    state: LibraryUiState,
    onSaveNote: (Note?, String, String) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onRemoveFavorite: (Favorite) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableIntStateOf(0) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                Text(stringResource(com.sporti.feature.library.R.string.library_title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(com.sporti.feature.library.R.string.library_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(com.sporti.feature.library.R.string.notes_count, state.notes.size)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(com.sporti.feature.library.R.string.favorites_count, state.favorites.size)) })
            }
            if (tab == 0) NotesList(
                notes = state.notes,
                onEdit = { editingNote = it; showEditor = true },
                onDelete = onDeleteNote,
            ) else FavoritesList(state.favorites, onRemoveFavorite)
        }
        if (tab == 0) {
            FloatingActionButton(
                onClick = { editingNote = null; showEditor = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 108.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(com.sporti.feature.library.R.string.add_note))
            }
        }
    }

    if (showEditor) {
        NoteEditor(
            note = editingNote,
            onDismiss = { showEditor = false },
            onSave = { title, body -> onSaveNote(editingNote, title, body); showEditor = false },
        )
    }
}

@Composable
private fun NotesList(notes: List<Note>, onEdit: (Note) -> Unit, onDelete: (Note) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (notes.isEmpty()) item { EmptyState(stringResource(com.sporti.feature.library.R.string.no_notes_title), stringResource(com.sporti.feature.library.R.string.no_notes_body)) }
        items(notes, key = Note::id) { note ->
            SportiCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatDate(note.updatedAtMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onEdit(note) }) { Icon(painterResource(R.drawable.ic_edit), contentDescription = stringResource(com.sporti.feature.library.R.string.edit_note)) }
                        IconButton(onClick = { onDelete(note) }) { Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(com.sporti.feature.library.R.string.delete_note), tint = MaterialTheme.colorScheme.primary) }
                    }
                    if (note.body.isNotBlank()) Text(note.body, modifier = Modifier.padding(end = 16.dp, top = 10.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun FavoritesList(favorites: List<Favorite>, onRemove: (Favorite) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (favorites.isEmpty()) item { EmptyState(stringResource(com.sporti.feature.library.R.string.no_favorites_title), stringResource(com.sporti.feature.library.R.string.no_favorites_body)) }
        items(favorites, key = { "${it.type}-${it.contentId}" }) { favorite ->
            SportiCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painterResource(if (favorite.type == FavoriteType.MATCH) R.drawable.ic_matches else R.drawable.ic_open), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        StatusPill(if (favorite.type == FavoriteType.MATCH) stringResource(com.sporti.feature.library.R.string.match) else stringResource(com.sporti.feature.library.R.string.article), false)
                        Text(favorite.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                        Text(favorite.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onRemove(favorite) }) { Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(com.sporti.feature.library.R.string.remove_favorite), tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun NoteEditor(note: Note?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember(note) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note) { mutableStateOf(note?.body.orEmpty()) }
    val untitled = stringResource(com.sporti.feature.library.R.string.untitled)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) stringResource(com.sporti.feature.library.R.string.new_note) else stringResource(com.sporti.feature.library.R.string.edit_note)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(com.sporti.feature.library.R.string.note_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text(stringResource(com.sporti.feature.library.R.string.note_body)) }, minLines = 5, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onSave(title.ifBlank { untitled }, body) }, enabled = title.isNotBlank() || body.isNotBlank(), shape = SportiButtonShape) { Text(stringResource(com.sporti.feature.library.R.string.save)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss, shape = SportiButtonShape) { Text(stringResource(com.sporti.feature.library.R.string.cancel)) } },
    )
}

private val noteDateFormatter = DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale.forLanguageTag("ru"))
private fun formatDate(value: Long): String = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).format(noteDateFormatter)
