package edu.cit.gako.brainbox.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.audio.buildNotebookTtsRequest
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.shared.formatShortDate
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.AccentDark
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import kotlinx.coroutines.launch

// ─── palette mirrors web ─────────────────────────────────────────────────────

private const val AllLibraryCollectionId = "__all_library__"

private val PlaylistPalettes = listOf(
    listOf(Color(0xFF1C1917), Color(0xFF57534E)),
    listOf(Color(0xFF9A3412), Color(0xFFC2410C)),
    listOf(Color(0xFF1E3A5F), Color(0xFF2563EB)),
    listOf(Color(0xFF14532D), Color(0xFF16A34A)),
    listOf(Color(0xFF4A1D96), Color(0xFF7C3AED)),
    listOf(Color(0xFF831843), Color(0xFFDB2777)),
    listOf(Color(0xFF713F12), Color(0xFFCA8A04)),
    listOf(Color(0xFF164E63), Color(0xFF0891B2)),
)

private data class CollectionUi(
    val id: String,
    val title: String,
    val typeLabel: String,
    val notebooks: List<NotebookSummary>,
    val currentIndex: Int,
    val palette: List<Color>
)

private enum class SortBy { UPDATED_AT, TITLE, WORD_COUNT }

// ─── screen ──────────────────────────────────────────────────────────────────

@Composable
internal fun PlaylistsScreen(
    notebooks: List<NotebookSummary>,
    playlists: List<PlaylistSummary>,
    playbackState: PlaybackUiState,
    playbackQueue: List<NotebookSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenNotebook: (String) -> Unit,
    onAddToQueue: (NotebookSummary) -> Unit,
    onRemoveFromQueue: (String) -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val appGraph = remember(context.applicationContext) { BrainBoxAppGraph.from(context.applicationContext) }
    val repository = remember(appGraph) { appGraph.repository }
    val audioClient = remember(context.applicationContext) { BrainBoxAudioClient(context.applicationContext) }

    // local mutable playlist list so create/delete/add/remove update instantly
    var localPlaylists by remember(playlists) { mutableStateOf(playlists) }

    var selectedId by rememberSaveable { mutableStateOf(AllLibraryCollectionId) }
    var query by rememberSaveable { mutableStateOf("") }
    var sortBy by rememberSaveable { mutableStateOf(SortBy.UPDATED_AT) }
    var sortAsc by rememberSaveable { mutableStateOf(false) }
    var loadingUuid by rememberSaveable { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var addToPlaylistNotebook by remember { mutableStateOf<NotebookSummary?>(null) }

    val collections = remember(notebooks, localPlaylists) {
        buildCollections(notebooks, localPlaylists)
    }
    val selected = collections.firstOrNull { it.id == selectedId } ?: collections.firstOrNull()

    LaunchedEffect(collections, selectedId) {
        if (collections.none { it.id == selectedId }) selectedId = AllLibraryCollectionId
    }

    val displayNotebooks = remember(selected, query, sortBy, sortAsc) {
        var list = selected?.notebooks.orEmpty()
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) || it.categoryName.orEmpty().lowercase().contains(q)
            }
        }
        if (selected?.typeLabel != "Playlist") {
            list = when (sortBy) {
                SortBy.TITLE -> if (sortAsc) list.sortedBy { it.title } else list.sortedByDescending { it.title }
                SortBy.WORD_COUNT -> if (sortAsc) list.sortedBy { it.wordCount ?: 0 } else list.sortedByDescending { it.wordCount ?: 0 }
                SortBy.UPDATED_AT -> if (sortAsc) list.sortedBy { it.updatedAt } else list.sortedByDescending { it.updatedAt }
            }
        }
        list
    }

    val heroTarget = remember(selected, displayNotebooks) {
        resolveStartNotebook(selected, displayNotebooks)
    }

    fun playNotebook(notebook: NotebookSummary) {
        if (loadingUuid == notebook.uuid) return
        if (playbackState.notebookId == notebook.uuid && playbackState.isVisible) {
            when {
                playbackState.isPlaying -> { audioClient.pause(); return }
                playbackState.canResume -> { audioClient.resume(); return }
                // ENDED state — fall through to reload and replay
            }
        }
        scope.launch {
            loadingUuid = notebook.uuid
            runCatching { repository.getNotebook(notebook.uuid) }
                .onSuccess { audioClient.play(buildNotebookTtsRequest(it, it.content, false)) }
                .onFailure { onFeatureRequest("Couldn't load \"${notebook.title}\" for audio.") }
            loadingUuid = null
        }
    }

    fun playAll() {
        if (displayNotebooks.isEmpty()) return
        playNotebook(displayNotebooks.first())
        displayNotebooks.drop(1).forEach { onAddToQueue(it) }
    }

    fun createPlaylist(title: String) {
        scope.launch {
            runCatching { repository.createPlaylist(title) }
                .onSuccess { created ->
                    localPlaylists = localPlaylists + created
                    selectedId = created.uuid
                }
                .onFailure { onFeatureRequest("Couldn't create playlist.") }
        }
    }

    fun deletePlaylist(uuid: String) {
        scope.launch {
            runCatching { repository.deletePlaylist(uuid) }
                .onSuccess {
                    localPlaylists = localPlaylists.filter { it.uuid != uuid }
                    if (selectedId == uuid) selectedId = AllLibraryCollectionId
                }
                .onFailure { onFeatureRequest("Couldn't delete playlist.") }
        }
    }

    fun addNotebookToPlaylist(playlistUuid: String, notebookUuid: String) {
        scope.launch {
            runCatching { repository.addNotebookToPlaylist(playlistUuid, notebookUuid) }
                .onSuccess { updated ->
                    localPlaylists = localPlaylists.map { if (it.uuid == updated.uuid) updated else it }
                }
                .onFailure { onFeatureRequest("Couldn't add notebook to playlist.") }
        }
    }

    fun removeNotebookFromPlaylist(notebookUuid: String) {
        val playlistUuid = selectedId.takeIf { it != AllLibraryCollectionId } ?: return
        scope.launch {
            runCatching { repository.removeNotebookFromPlaylist(playlistUuid, notebookUuid) }
                .onSuccess { updated ->
                    localPlaylists = localPlaylists.map { if (it.uuid == updated.uuid) updated else it }
                }
                .onFailure { onFeatureRequest("Couldn't remove notebook from playlist.") }
        }
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // ── sync notice ────────────────────────────────────────────────────
        if (!syncNotice.isNullOrBlank()) {
            item {
                SyncNoticeBanner(
                    message = syncNotice,
                    meta = syncedAtLabel,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // ── library nav ────────────────────────────────────────────────────
        item {
            LibraryNav(
                collections = collections,
                selectedId = selectedId,
                onSelect = { selectedId = it },
                onNewPlaylist = { showCreateDialog = true }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── hero ───────────────────────────────────────────────────────────
        if (selected != null) {
            item {
                PlaylistHero(
                    collection = selected,
                    notebookCount = displayNotebooks.size
                )
            }
        }

        item { Spacer(Modifier.height(4.dp)) }

        // ── toolbar ────────────────────────────────────────────────────────
        item {
            Toolbar(
                isPlaylist = selected?.typeLabel == "Playlist",
                hasNotebooks = displayNotebooks.isNotEmpty(),
                sortBy = sortBy,
                sortAsc = sortAsc,
                query = query,
                onPlayAll = ::playAll,
                onDeletePlaylist = { selected?.let { deletePlaylist(it.id) } },
                onSortBy = { newSort ->
                    if (sortBy == newSort) sortAsc = !sortAsc
                    else { sortBy = newSort; sortAsc = newSort == SortBy.TITLE }
                },
                onQueryChange = { query = it }
            )
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── track list ─────────────────────────────────────────────────────
        if (displayNotebooks.isEmpty()) {
            item {
                EmptyTrackList(
                    isSearch = query.isNotBlank(),
                    isPlaylist = selected?.typeLabel == "Playlist",
                    query = query
                )
            }
        } else {
            item {
                TrackListHeader(isPlaylist = selected?.typeLabel == "Playlist")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column {
                        displayNotebooks.forEachIndexed { index, notebook ->
                            if (index > 0) HorizontalDivider(color = Border, thickness = 0.5.dp)
                            TrackRow(
                                index = index,
                                notebook = notebook,
                                isLoading = loadingUuid == notebook.uuid,
                                isActive = playbackState.notebookId == notebook.uuid,
                                isPlaying = playbackState.notebookId == notebook.uuid && playbackState.isPlaying,
                                inQueue = playbackQueue.any { it.uuid == notebook.uuid },
                                isPlaylist = selected?.typeLabel == "Playlist",
                                onPlay = { playNotebook(notebook) },
                                onAddToQueue = { onAddToQueue(notebook) },
                                onAddToPlaylist = { addToPlaylistNotebook = notebook },
                                onRemoveFromPlaylist = { removeNotebookFromPlaylist(notebook.uuid) }
                            )
                        }
                    }
                }
            }
        }

        // ── up-next queue ──────────────────────────────────────────────────
        if (playbackQueue.isNotEmpty()) {
            item { Spacer(Modifier.height(20.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Up next",
                        style = MaterialTheme.typography.labelLarge,
                        color = Ink2,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${playbackQueue.size} track${if (playbackQueue.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column {
                        playbackQueue.forEachIndexed { index, notebook ->
                            if (index > 0) HorizontalDivider(color = Border, thickness = 0.5.dp)
                            QueueRow(notebook = notebook, onRemove = { onRemoveFromQueue(notebook.uuid) })
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    // ── dialogs ───────────────────────────────────────────────────────────────
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title ->
                showCreateDialog = false
                createPlaylist(title)
            }
        )
    }

    addToPlaylistNotebook?.let { notebook ->
        AddToPlaylistDialog(
            notebook = notebook,
            playlists = localPlaylists,
            onAdd = { playlistUuid -> addNotebookToPlaylist(playlistUuid, notebook.uuid) },
            onDismiss = { addToPlaylistNotebook = null }
        )
    }
}

// ─── library nav (mirrors web sidebar) ───────────────────────────────────────

@Composable
private fun LibraryNav(
    collections: List<CollectionUi>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onNewPlaylist: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink3,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    onClick = onNewPlaylist,
                    shape = CircleShape,
                    color = Cream2,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New playlist",
                        tint = Ink2,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
            }
            HorizontalDivider(color = Border, thickness = 0.5.dp)
            collections.forEachIndexed { index, collection ->
                if (index > 0) HorizontalDivider(color = Border, thickness = 0.5.dp)
                LibraryNavItem(
                    collection = collection,
                    selected = collection.id == selectedId,
                    onClick = { onSelect(collection.id) }
                )
            }
            HorizontalDivider(color = Border, thickness = 0.5.dp)
            TextButton(
                onClick = onNewPlaylist,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Ink3
                )
                Spacer(Modifier.width(6.dp))
                Text("New Playlist", color = Ink3, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LibraryNavItem(
    collection: CollectionUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) AccentBg else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(collection.palette)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = White.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = collection.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Accent else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${collection.notebooks.size} notebook${if (collection.notebooks.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent)
            )
        }
    }
}

// ─── hero (gradient banner, matches web) ─────────────────────────────────────

@Composable
private fun PlaylistHero(collection: CollectionUi, notebookCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(collection.palette))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (collection.typeLabel == "Playlist") "Playlist" else "Collection",
                    style = MaterialTheme.typography.labelSmall,
                    color = White.copy(alpha = 0.7f)
                )
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$notebookCount notebook${if (notebookCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─── toolbar ─────────────────────────────────────────────────────────────────

@Composable
private fun Toolbar(
    isPlaylist: Boolean,
    hasNotebooks: Boolean,
    sortBy: SortBy,
    sortAsc: Boolean,
    query: String,
    onPlayAll: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onSortBy: (SortBy) -> Unit,
    onQueryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play all
            FilledTonalButton(
                onClick = onPlayAll,
                enabled = hasNotebooks,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Accent,
                    contentColor = White
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Play all", style = MaterialTheme.typography.labelLarge)
            }

            // Delete (only for specific playlists)
            if (isPlaylist) {
                FilledTonalButton(
                    onClick = onDeletePlaylist,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ErrorRed.copy(alpha = 0.1f),
                        contentColor = ErrorRed
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.weight(1f))

            // Sort toggle (only for All Notebooks view)
            if (!isPlaylist) {
                Surface(
                    onClick = {
                        // cycle through sort options
                        val next = when (sortBy) {
                            SortBy.UPDATED_AT -> SortBy.TITLE
                            SortBy.TITLE -> SortBy.WORD_COUNT
                            SortBy.WORD_COUNT -> SortBy.UPDATED_AT
                        }
                        onSortBy(next)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Cream2,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (sortAsc) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = "Sort direction",
                            tint = Ink2,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (sortBy) {
                                SortBy.UPDATED_AT -> "Date"
                                SortBy.TITLE -> "Title"
                                SortBy.WORD_COUNT -> "Words"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Ink2
                        )
                    }
                }

            }
        }

        // search row
        SearchField(value = query, onValueChange = onQueryChange)
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Ink3, modifier = Modifier.size(18.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("Search…", style = MaterialTheme.typography.bodyMedium, color = Ink3)
                    }
                    inner()
                }
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = Ink3, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─── track list ───────────────────────────────────────────────────────────────

@Composable
private fun TrackListHeader(isPlaylist: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", style = MaterialTheme.typography.labelSmall, color = Ink3, modifier = Modifier.width(28.dp))
        Text("Title", style = MaterialTheme.typography.labelSmall, color = Ink3, modifier = Modifier.weight(1f))
        Text("Words", style = MaterialTheme.typography.labelSmall, color = Ink3)
    }
}

@Composable
private fun TrackRow(
    index: Int,
    notebook: NotebookSummary,
    isLoading: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    inQueue: Boolean,
    isPlaylist: Boolean,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromPlaylist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) AccentBg else Color.Transparent)
            .clickable(onClick = onPlay)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // # or animated indicator
        Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Accent
                )
                isPlaying -> Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = "Playing",
                    tint = Accent,
                    modifier = Modifier.size(16.dp)
                )
                else -> Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Accent else Ink3
                )
            }
        }

        // title + category
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = notebook.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) Accent else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (notebook.categoryName != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isActive) Accent.copy(alpha = 0.12f) else Cream2,
                    modifier = Modifier
                ) {
                    Text(
                        text = notebook.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) Accent else Ink3,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }

        // word count
        Text(
            text = notebook.wordCount?.let { formatWordCount(it) } ?: "—",
            style = MaterialTheme.typography.labelSmall,
            color = Ink3
        )

        // action icons
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            if (!inQueue && !isActive) {
                IconButton(onClick = onAddToQueue, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Queue, contentDescription = "Add to queue", tint = Ink3, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onAddToPlaylist, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add to playlist", tint = Ink3, modifier = Modifier.size(16.dp))
            }
            if (isPlaylist) {
                IconButton(onClick = onRemoveFromPlaylist, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove from playlist", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── queue row ────────────────────────────────────────────────────────────────

@Composable
private fun QueueRow(notebook: NotebookSummary, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = Ink3,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = notebook.title,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (notebook.categoryName != null) {
                Text(
                    text = notebook.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink3,
                    maxLines = 1
                )
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Ink3, modifier = Modifier.size(14.dp))
        }
    }
}

// ─── empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyTrackList(isSearch: Boolean, isPlaylist: Boolean, query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = Ink3,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = when {
                isSearch -> "No results for \"$query\""
                isPlaylist -> "This playlist is empty"
                else -> "No notebooks yet"
            },
            style = MaterialTheme.typography.titleSmall,
            color = Ink2
        )
        Text(
            text = when {
                isSearch -> "Try a different title or category keyword."
                isPlaylist -> "Add notebooks using the + button on any track."
                else -> "Create notebooks in your library to play them here."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )
    }
}

// ─── dialogs ──────────────────────────────────────────────────────────────────

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create playlist") },
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
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddToPlaylistDialog(
    notebook: NotebookSummary,
    playlists: List<PlaylistSummary>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add to playlist")
                Text(notebook.title, style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
        },
        text = {
            if (playlists.isEmpty()) {
                Text("No playlists yet. Create one from Your Library.", style = MaterialTheme.typography.bodySmall, color = Ink3)
            } else {
                Column {
                    playlists.forEachIndexed { i, pl ->
                        val alreadyIn = pl.queue.any { it.uuid == notebook.uuid }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let { m -> if (!alreadyIn) m.clickable { onAdd(pl.uuid); onDismiss() } else m }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.linearGradient(PlaylistPalettes[(i + 1) % PlaylistPalettes.size])),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = White, modifier = Modifier.size(14.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.title, style = MaterialTheme.typography.bodyMedium, color = Ink, maxLines = 1)
                                Text("${pl.queue.size} notebooks", style = MaterialTheme.typography.labelSmall, color = Ink3)
                            }
                            if (alreadyIn) {
                                Icon(Icons.Filled.Check, contentDescription = "Already added", tint = Accent, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (i < playlists.lastIndex) HorizontalDivider(color = Border, thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

// ─── helpers ──────────────────────────────────────────────────────────────────

private fun buildCollections(notebooks: List<NotebookSummary>, playlists: List<PlaylistSummary>): List<CollectionUi> {
    val all = CollectionUi(
        id = AllLibraryCollectionId,
        title = "All Notebooks",
        typeLabel = "Collection",
        notebooks = notebooks,
        currentIndex = 0,
        palette = PlaylistPalettes[0]
    )
    val playlistItems = playlists.mapIndexed { i, pl ->
        CollectionUi(
            id = pl.uuid,
            title = pl.title.ifBlank { "Untitled playlist" },
            typeLabel = "Playlist",
            notebooks = pl.queue,
            currentIndex = pl.currentIndex,
            palette = PlaylistPalettes[(i + 1) % PlaylistPalettes.size]
        )
    }
    return listOf(all) + playlistItems
}

private fun resolveStartNotebook(collection: CollectionUi?, list: List<NotebookSummary>): NotebookSummary? {
    if (collection == null || list.isEmpty()) return null
    if (collection.typeLabel != "Playlist") return list.firstOrNull()
    val idx = collection.currentIndex.coerceIn(0, (collection.notebooks.size - 1).coerceAtLeast(0))
    val ordered = collection.notebooks.drop(idx) + collection.notebooks.take(idx)
    return ordered.firstOrNull { c -> list.any { it.uuid == c.uuid } } ?: list.firstOrNull()
}

private fun formatWordCount(count: Int): String = when {
    count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
    else -> count.toString()
}
