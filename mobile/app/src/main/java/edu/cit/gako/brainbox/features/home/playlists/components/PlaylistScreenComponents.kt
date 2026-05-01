package edu.cit.gako.brainbox.features.home.playlists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Cream
import edu.cit.gako.brainbox.shared.ui.theme.Cream2
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import kotlinx.coroutines.launch

@Composable
internal fun PlaylistSelector(
    playlists: List<PlaylistChipUi>,
    selectedPlaylist: PlaylistSummary?,
    selectedPlaylistUuid: String?,
    onOpenPicker: () -> Unit,
    onCreatePlaylist: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedPlaylist?.title ?: "Choose one playlist to manage at a time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedPlaylist == null) Ink3 else Ink2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    onClick = onCreatePlaylist,
                    shape = CircleShape,
                    color = Cream2,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create playlist",
                        tint = Ink2,
                        modifier = Modifier.padding(7.dp).size(18.dp)
                    )
                }
            }

            if (playlists.isEmpty()) {
                EmptyPanel(
                    title = "No playlists yet",
                    message = "Create one to start organizing notebooks into a repeatable listening order.",
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            } else {
                FilledTonalButton(
                    onClick = onOpenPicker,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Cream2,
                        contentColor = Ink2
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedPlaylistUuid == null) "Choose playlist" else "Change playlist")
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun PlaylistPickerSheet(
    playlists: List<PlaylistChipUi>,
    selectedPlaylistUuid: String?,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredPlaylists = remember(playlists, query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            playlists
        } else {
            playlists.filter { it.playlist.title.lowercase().contains(normalizedQuery) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Choose playlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${playlists.size} playlist${if (playlists.size == 1) "" else "s"} available",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
                TextButton(onClick = onCreatePlaylist) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Search playlists") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    focusedPlaceholderColor = Ink3,
                    unfocusedPlaceholderColor = Ink3
                )
            )

            if (filteredPlaylists.isEmpty()) {
                EmptyPanel(
                    title = "No matches found",
                    message = "Try a different playlist name.",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPlaylists, key = { it.playlist.uuid }) { item ->
                        PlaylistSelectorCard(
                            item = item,
                            selected = item.playlist.uuid == selectedPlaylistUuid,
                            onClick = { onSelectPlaylist(item.playlist.uuid) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlaylistSelectorCard(
    item: PlaylistChipUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) AccentBg else Cream,
        border = BorderStroke(1.dp, if (selected) Accent.copy(alpha = 0.35f) else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(item.palette)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.playlist.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) Accent else Ink,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.playlist.queue.size} notebook${if (item.playlist.queue.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }
    }
}

@Composable
internal fun PlaylistHeroCard(
    playlistTitle: String?,
    notebookCount: Int,
    palette: List<Color>
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(palette))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (playlistTitle != null) "Selected playlist" else "Playlist management",
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.74f)
                    )
                    Text(
                        text = playlistTitle ?: "Create your first playlist",
                        style = MaterialTheme.typography.headlineMedium,
                        color = White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (playlistTitle != null) {
                            "$notebookCount notebook${if (notebookCount == 1) "" else "s"} ready to play and reorder."
                        } else {
                            "Use the library section to add notebooks once you have a playlist."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActionRow(
    hasSelectedPlaylist: Boolean,
    onDeletePlaylist: () -> Unit
) {
    FilledTonalButton(
        onClick = onDeletePlaylist,
        enabled = hasSelectedPlaylist,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = ErrorRed.copy(alpha = 0.1f),
            contentColor = ErrorRed
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("Delete")
    }
}

@Composable
internal fun PlaylistSectionTabs(
    activeSection: PlaylistSection,
    onSelectSection: (PlaylistSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChip(
            selected = activeSection == PlaylistSection.LIBRARY,
            onClick = { onSelectSection(PlaylistSection.LIBRARY) },
            label = { Text("Library") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentBg,
                selectedLabelColor = Accent
            )
        )
        FilterChip(
            selected = activeSection == PlaylistSection.QUEUE,
            onClick = { onSelectSection(PlaylistSection.QUEUE) },
            label = { Text("Queue") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentBg,
                selectedLabelColor = Accent
            )
        )
    }
}

@Composable
internal fun PanelHeader(
    label: String,
    title: String,
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Ink3
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )
    }
}

@Composable
internal fun LibraryControls(
    query: String,
    sortBy: SortBy,
    sortAscending: Boolean,
    onQueryChange: (String) -> Unit,
    onSortChange: (SortBy) -> Unit,
    onToggleDirection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Search notebooks") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedPlaceholderColor = Ink3,
                unfocusedPlaceholderColor = Ink3
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(SortBy.UPDATED_AT, SortBy.TITLE, SortBy.WORD_COUNT).forEach { option ->
                FilterChip(
                    selected = sortBy == option,
                    onClick = { onSortChange(option) },
                    label = {
                        Text(
                            when (option) {
                                SortBy.UPDATED_AT -> "Recent"
                                SortBy.TITLE -> "Title"
                                SortBy.WORD_COUNT -> "Words"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBg,
                        selectedLabelColor = Accent
                    )
                )
            }

            Surface(
                onClick = onToggleDirection,
                shape = RoundedCornerShape(999.dp),
                color = White,
                border = BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = Ink2,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (sortAscending) "Ascending" else "Descending",
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink2
                    )
                }
            }
        }
    }
}

@Composable
internal fun LibraryNotebookRow(
    notebook: NotebookSummary,
    alreadyAdded: Boolean,
    hasSelectedPlaylist: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Cream2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Ink2,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = notebook.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaPill(text = notebook.categoryName ?: "Uncategorized")
                Text(
                    text = "${formatWordCount(notebook.wordCount ?: 0)} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }

        if (alreadyAdded) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = AccentBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Added",
                        style = MaterialTheme.typography.labelMedium,
                        color = Accent
                    )
                }
            }
        } else {
            FilledTonalButton(
                onClick = onAdd,
                enabled = hasSelectedPlaylist,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Cream2,
                    contentColor = Ink2
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@Composable
internal fun QueueNotebookRow(
    notebook: NotebookSummary,
    position: Int,
    total: Int,
    isLoading: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) AccentBg else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) White else Cream2
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = if (isActive) Accent else Ink3,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = position.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) Accent else Ink3
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = notebook.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) Accent else Ink,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaPill(text = notebook.categoryName ?: "Uncategorized")
                Text(
                    text = "${formatWordCount(notebook.wordCount ?: 0)} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPlay, modifier = Modifier.size(34.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Accent
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Playing" else "Play notebook",
                        tint = if (isActive) Accent else Ink2,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                onClick = onMoveUp,
                enabled = position > 1,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Move up",
                    tint = Ink2,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = position < total,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Move down",
                    tint = Ink2,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove from playlist",
                    tint = ErrorRed.copy(alpha = 0.82f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Cream2
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Ink2,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
internal fun EmptyPanel(
    title: String,
    message: String,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )
    }
}

@Composable
internal fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("My study playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun DeletePlaylistDialog(
    playlistTitle: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete playlist") },
        text = {
            Text(
                text = "Delete \"$playlistTitle\"? The notebooks stay in your library, but this playlist queue will be removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink2
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = ErrorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatWordCount(count: Int): String = when {
    count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
    else -> count.toString()
}
