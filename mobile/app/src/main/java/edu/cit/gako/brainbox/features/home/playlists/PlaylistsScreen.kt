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
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistRepository
import edu.cit.gako.brainbox.features.home.playlists.data.replaceOptimisticPlaylist
import edu.cit.gako.brainbox.features.home.playlists.data.withNotebookInPlaylist
import edu.cit.gako.brainbox.features.home.playlists.data.withOptimisticPlaylist
import edu.cit.gako.brainbox.features.home.playlists.data.withPlaylistQueueOrder
import edu.cit.gako.brainbox.features.home.playlists.data.withoutNotebookInPlaylist
import edu.cit.gako.brainbox.features.home.playlists.data.withoutPlaylist
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.shared.ui.SyncNoticeBanner
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

internal val PlaylistPalettes = listOf(
    listOf(Color(0xFF1C1917), Color(0xFF57534E)),
    listOf(Color(0xFF9A3412), Color(0xFFC2410C)),
    listOf(Color(0xFF1E3A5F), Color(0xFF2563EB)),
    listOf(Color(0xFF14532D), Color(0xFF16A34A)),
    listOf(Color(0xFF4A1D96), Color(0xFF7C3AED)),
    listOf(Color(0xFF831843), Color(0xFFDB2777)),
    listOf(Color(0xFF713F12), Color(0xFFCA8A04)),
    listOf(Color(0xFF164E63), Color(0xFF0891B2)),
)

internal enum class SortBy { UPDATED_AT, TITLE, WORD_COUNT }

internal enum class PlaylistSection { LIBRARY, QUEUE }

internal data class PlaylistChipUi(
    val playlist: PlaylistSummary,
    val palette: List<Color>
)

@Composable
internal fun PlaylistsScreen(
    repository: PlaylistRepository,
    notebooks: List<NotebookSummary>,
    playlists: List<PlaylistSummary>,
    playbackState: PlaybackUiState,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onPlayNotebook: (NotebookSummary) -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var localPlaylists by remember(playlists) { mutableStateOf(playlists) }
    var selectedPlaylistUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var libraryQuery by rememberSaveable { mutableStateOf("") }
    var sortBy by rememberSaveable { mutableStateOf(SortBy.UPDATED_AT) }
    var sortAscending by rememberSaveable { mutableStateOf(false) }
    var activeSection by rememberSaveable { mutableStateOf(PlaylistSection.LIBRARY) }
    var temporaryPlaylistId by rememberSaveable { mutableStateOf(1) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<PlaylistSummary?>(null) }

    LaunchedEffect(localPlaylists, selectedPlaylistUuid) {
        if (localPlaylists.isEmpty()) {
            selectedPlaylistUuid = null
            activeSection = PlaylistSection.LIBRARY
            return@LaunchedEffect
        }

        if (selectedPlaylistUuid == null || localPlaylists.none { it.uuid == selectedPlaylistUuid }) {
            selectedPlaylistUuid = localPlaylists.first().uuid
        }
    }

    val selectedPlaylist = remember(localPlaylists, selectedPlaylistUuid) {
        localPlaylists.firstOrNull { it.uuid == selectedPlaylistUuid }
    }
    val selectedPlaylistIndex = remember(localPlaylists, selectedPlaylistUuid) {
        localPlaylists.indexOfFirst { it.uuid == selectedPlaylistUuid }.coerceAtLeast(0)
    }
    val selectedPalette = remember(selectedPlaylistIndex) {
        PlaylistPalettes[selectedPlaylistIndex % PlaylistPalettes.size]
    }
    val notebookMap = remember(notebooks) { notebooks.associateBy { it.uuid } }
    val playlistQueue = remember(selectedPlaylist, notebookMap) {
        selectedPlaylist?.queue.orEmpty().map { notebookMap[it.uuid] ?: it }
    }
    val selectedNotebookIds = remember(playlistQueue) {
        playlistQueue.map { it.uuid }.toSet()
    }
    val libraryNotebooks = remember(notebooks, libraryQuery, sortBy, sortAscending) {
        val filtered = if (libraryQuery.isBlank()) {
            notebooks
        } else {
            val query = libraryQuery.trim().lowercase()
            notebooks.filter {
                it.title.lowercase().contains(query) ||
                    it.categoryName.orEmpty().lowercase().contains(query)
            }
        }

        when (sortBy) {
            SortBy.TITLE -> if (sortAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
            SortBy.WORD_COUNT -> if (sortAscending) filtered.sortedBy { it.wordCount ?: 0 } else filtered.sortedByDescending { it.wordCount ?: 0 }
            SortBy.UPDATED_AT -> if (sortAscending) filtered.sortedBy { it.updatedAt } else filtered.sortedByDescending { it.updatedAt }
        }
    }
    val playlistChips = remember(localPlaylists) {
        localPlaylists.mapIndexed { index, playlist ->
            PlaylistChipUi(
                playlist = playlist,
                palette = PlaylistPalettes[index % PlaylistPalettes.size]
            )
        }
    }

    fun createPlaylist(title: String) {
        val previousPlaylists = localPlaylists
        val previousSelectedPlaylistUuid = selectedPlaylistUuid
        val temporaryUuid = "optimistic-playlist-$temporaryPlaylistId"
        temporaryPlaylistId += 1
        val optimisticPlaylist = PlaylistSummary(uuid = temporaryUuid, title = title)

        localPlaylists = localPlaylists.withOptimisticPlaylist(optimisticPlaylist)
        selectedPlaylistUuid = temporaryUuid
        activeSection = PlaylistSection.LIBRARY

        scope.launch {
            runCatching { repository.createPlaylist(title) }
                .onSuccess { created ->
                    localPlaylists = localPlaylists.replaceOptimisticPlaylist(temporaryUuid, created)
                    selectedPlaylistUuid = created.uuid
                    activeSection = PlaylistSection.LIBRARY
                }
                .onFailure {
                    localPlaylists = previousPlaylists
                    selectedPlaylistUuid = previousSelectedPlaylistUuid
                    onFeatureRequest("Couldn't create playlist.")
                }
        }
    }

    fun deletePlaylist(target: PlaylistSummary) {
        val previousPlaylists = localPlaylists
        val currentIndex = localPlaylists.indexOfFirst { it.uuid == target.uuid }
        val fallbackPlaylist = localPlaylists.getOrNull(currentIndex + 1)
            ?: localPlaylists.getOrNull(currentIndex - 1)

        localPlaylists = localPlaylists.withoutPlaylist(target.uuid)
        selectedPlaylistUuid = fallbackPlaylist?.uuid
        playlistToDelete = null
        activeSection = PlaylistSection.LIBRARY

        scope.launch {
            runCatching { repository.deletePlaylist(target.uuid) }
                .onFailure {
                    localPlaylists = previousPlaylists
                    selectedPlaylistUuid = target.uuid
                    onFeatureRequest("Couldn't delete playlist.")
                }
        }
    }

    fun addNotebookToPlaylist(notebookUuid: String) {
        val playlistUuid = selectedPlaylistUuid ?: run {
            showCreateDialog = true
            return
        }

        val notebook = notebookMap[notebookUuid] ?: return
        val previousPlaylists = localPlaylists
        localPlaylists = localPlaylists.withNotebookInPlaylist(playlistUuid, notebook)

        scope.launch {
            runCatching { repository.addNotebookToPlaylist(playlistUuid, notebookUuid) }
                .onSuccess { updated ->
                    localPlaylists = localPlaylists.withOptimisticPlaylist(updated)
                }
                .onFailure {
                    localPlaylists = previousPlaylists
                    onFeatureRequest("Couldn't add notebook to playlist.")
                }
        }
    }

    fun removeNotebookFromPlaylist(notebookUuid: String) {
        val playlistUuid = selectedPlaylistUuid ?: return

        val previousPlaylists = localPlaylists
        localPlaylists = localPlaylists.withoutNotebookInPlaylist(playlistUuid, notebookUuid)

        scope.launch {
            runCatching { repository.removeNotebookFromPlaylist(playlistUuid, notebookUuid) }
                .onSuccess { updated ->
                    localPlaylists = localPlaylists.withOptimisticPlaylist(updated)
                }
                .onFailure {
                    localPlaylists = previousPlaylists
                    onFeatureRequest("Couldn't remove notebook from playlist.")
                }
        }
    }

    fun moveNotebookInPlaylist(fromIndex: Int, toIndex: Int) {
        val playlistUuid = selectedPlaylistUuid ?: return
        if (toIndex < 0 || toIndex >= playlistQueue.size) {
            return
        }

        val nextOrder = playlistQueue.map { it.uuid }.toMutableList()
        val movedUuid = nextOrder.removeAt(fromIndex)
        nextOrder.add(toIndex, movedUuid)

        val previousPlaylists = localPlaylists
        localPlaylists = localPlaylists.withPlaylistQueueOrder(playlistUuid, nextOrder)

        scope.launch {
            runCatching { repository.reorderPlaylist(playlistUuid, nextOrder) }
                .onSuccess { updated ->
                    localPlaylists = localPlaylists.withOptimisticPlaylist(updated)
                }
                .onFailure {
                    localPlaylists = previousPlaylists
                    onFeatureRequest("Couldn't reorder playlist.")
                }
        }
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!syncNotice.isNullOrBlank()) {
            item {
                SyncNoticeBanner(
                    message = syncNotice,
                    meta = syncedAtLabel
                )
            }
        }

        item {
            PlaylistSelector(
                playlists = playlistChips,
                selectedPlaylist = selectedPlaylist,
                selectedPlaylistUuid = selectedPlaylistUuid,
                onOpenPicker = { showPlaylistPicker = true },
                onCreatePlaylist = { showCreateDialog = true }
            )
        }

        item {
            PlaylistHeroCard(
                playlistTitle = selectedPlaylist?.title,
                notebookCount = playlistQueue.size,
                palette = selectedPalette
            )
        }

        item {
            ActionRow(
                hasSelectedPlaylist = selectedPlaylist != null,
                onDeletePlaylist = {
                    if (selectedPlaylist != null) {
                        playlistToDelete = selectedPlaylist
                    }
                }
            )
        }

        if (selectedPlaylist != null) {
            item {
                PlaylistSectionTabs(
                    activeSection = activeSection,
                    onSelectSection = { activeSection = it }
                )
            }
        }

        if (selectedPlaylist == null || activeSection == PlaylistSection.LIBRARY) {
            item {
                PanelHeader(
                    label = "Library",
                    title = "Add notebooks",
                    description = if (selectedPlaylist != null) {
                        "Every add goes straight into ${selectedPlaylist.title}."
                    } else {
                        "Create a playlist first, then add notebooks from here."
                    }
                )
            }

            item {
                LibraryControls(
                    query = libraryQuery,
                    sortBy = sortBy,
                    sortAscending = sortAscending,
                    onQueryChange = { libraryQuery = it },
                    onSortChange = { nextSort ->
                        if (sortBy == nextSort) {
                            sortAscending = !sortAscending
                        } else {
                            sortBy = nextSort
                            sortAscending = nextSort == SortBy.TITLE
                        }
                    },
                    onToggleDirection = { sortAscending = !sortAscending }
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    if (libraryNotebooks.isEmpty()) {
                        EmptyPanel(
                            title = if (libraryQuery.isBlank()) "No notebooks yet" else "No matches found",
                            message = if (libraryQuery.isBlank()) {
                                "Create notebooks in your library and they will show up here."
                            } else {
                                "Try a different title or category."
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = Ink3,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                    } else {
                        Column {
                            libraryNotebooks.forEachIndexed { index, notebook ->
                                if (index > 0) {
                                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                                }
                                LibraryNotebookRow(
                                    notebook = notebook,
                                    alreadyAdded = notebook.uuid in selectedNotebookIds,
                                    hasSelectedPlaylist = selectedPlaylist != null,
                                    onAdd = { addNotebookToPlaylist(notebook.uuid) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedPlaylist != null && activeSection == PlaylistSection.QUEUE) {
            item {
                PanelHeader(
                    label = "Queue",
                    title = "${selectedPlaylist.title} queue",
                    description = "Use the arrows to adjust order, or remove notebooks you no longer want in this playlist."
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    if (playlistQueue.isEmpty()) {
                        EmptyPanel(
                            title = "This playlist is empty",
                            message = "Add notebooks from the library section, then reorder them here.",
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
                        Column {
                            playlistQueue.forEachIndexed { index, notebook ->
                                if (index > 0) {
                                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                                }
                                QueueNotebookRow(
                                    notebook = notebook,
                                    position = index + 1,
                                    total = playlistQueue.size,
                                    isLoading = playbackState.notebookId == notebook.uuid &&
                                        playbackState.status == BrainBoxAudioPlaybackStatus.LOADING,
                                    isActive = playbackState.notebookId == notebook.uuid &&
                                        playbackState.isVisible,
                                    isPlaying = playbackState.notebookId == notebook.uuid &&
                                        playbackState.isPlaying,
                                    onPlay = { onPlayNotebook(notebook) },
                                    onMoveUp = { moveNotebookInPlaylist(index, index - 1) },
                                    onMoveDown = { moveNotebookInPlaylist(index, index + 1) },
                                    onRemove = { removeNotebookFromPlaylist(notebook.uuid) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title ->
                showCreateDialog = false
                createPlaylist(title)
            }
        )
    }

    if (showPlaylistPicker) {
        PlaylistPickerSheet(
            playlists = playlistChips,
            selectedPlaylistUuid = selectedPlaylistUuid,
            onDismiss = { showPlaylistPicker = false },
            onSelectPlaylist = {
                selectedPlaylistUuid = it
                showPlaylistPicker = false
            },
            onCreatePlaylist = {
                showPlaylistPicker = false
                showCreateDialog = true
            }
        )
    }

    playlistToDelete?.let { target ->
        DeletePlaylistDialog(
            playlistTitle = target.title,
            onDismiss = { playlistToDelete = null },
            onDelete = { deletePlaylist(target) }
        )
    }
}
