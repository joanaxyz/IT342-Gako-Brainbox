package edu.cit.gako.brainbox.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.audio.buildNotebookTtsRequest
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.SectionHeader
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.shared.TokenBadge
import edu.cit.gako.brainbox.shared.formatShortDate
import edu.cit.gako.brainbox.shared.joinMeta
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.AccentDark
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import kotlinx.coroutines.launch

private const val AllLibraryCollectionId = "__all_library__"

private val PlaylistPalettes = listOf(
    listOf(Ink, Ink2),                                           // #1C1917 → #57534E
    listOf(AccentDark, Accent),                                  // #9A3412 → #C2410C
    listOf(Color(0xFF1E3A5F), Color(0xFF2563EB)),                // navy → blue
    listOf(Color(0xFF14532D), Color(0xFF16A34A)),                // deep green → green
    listOf(Color(0xFF4A1D96), Color(0xFF7C3AED)),                // deep purple → violet
    listOf(Color(0xFF831843), Color(0xFFDB2777)),                // deep pink
    listOf(Color(0xFF713F12), Color(0xFFCA8A04)),                // deep amber → yellow
    listOf(Color(0xFF164E63), Color(0xFF0891B2)),                // deep teal → cyan
)

private data class PlaylistCollectionUi(
    val id: String,
    val title: String,
    val typeLabel: String,
    val notebooks: List<NotebookSummary>,
    val currentIndex: Int,
    val colors: List<Color>
)

@Composable
internal fun PlaylistsScreen(
    notebooks: List<NotebookSummary>,
    playlists: List<PlaylistSummary>,
    playbackState: PlaybackUiState,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenNotebook: (String) -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val appGraph = remember(context.applicationContext) { BrainBoxAppGraph.from(context.applicationContext) }
    val repository = remember(appGraph) { appGraph.repository }
    val audioClient = remember(context.applicationContext) { BrainBoxAudioClient(context.applicationContext) }

    var query by rememberSaveable { mutableStateOf("") }
    var selectedCollectionId by rememberSaveable { mutableStateOf(AllLibraryCollectionId) }
    var loadingNotebookUuid by rememberSaveable { mutableStateOf<String?>(null) }

    val collections = remember(notebooks, playlists) {
        buildPlaylistCollections(
            notebooks = notebooks,
            playlists = playlists
        )
    }
    val selectedCollection = collections.firstOrNull { it.id == selectedCollectionId } ?: collections.firstOrNull()
    val filteredNotebooks = remember(selectedCollection, query) {
        val source = selectedCollection?.notebooks.orEmpty()
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            source
        } else {
            source.filter { notebook ->
                notebook.title.contains(normalizedQuery, ignoreCase = true) ||
                    notebook.categoryName.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
        }
    }
    val heroTarget = remember(selectedCollection, filteredNotebooks) {
        resolveCollectionStartNotebook(selectedCollection, filteredNotebooks)
    }

    LaunchedEffect(collections, selectedCollectionId) {
        if (collections.none { it.id == selectedCollectionId }) {
            selectedCollectionId = AllLibraryCollectionId
        }
    }

    fun playNotebook(notebook: NotebookSummary) {
        if (loadingNotebookUuid == notebook.uuid) return

        if (playbackState.notebookId == notebook.uuid && playbackState.isVisible) {
            if (playbackState.isPlaying) {
                audioClient.pause()
            } else {
                audioClient.resume()
            }
            return
        }

        scope.launch {
            loadingNotebookUuid = notebook.uuid
            runCatching { repository.getNotebook(notebook.uuid) }
                .onSuccess { detail ->
                    audioClient.play(
                        buildNotebookTtsRequest(
                            notebook = detail,
                            html = detail.content,
                            offlineOnly = false
                        )
                    )
                }
                .onFailure {
                    onFeatureRequest("We couldn't load ${notebook.title} for audio yet.")
                }
            loadingNotebookUuid = null
        }
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = "Study queues now mirror the web playlist flow, but tuned for thumbs, faster scanning, and quick resume playback.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink2
            )
        }

        if (!syncNotice.isNullOrBlank()) {
            item {
                SyncNoticeBanner(
                    message = syncNotice,
                    meta = syncedAtLabel
                )
            }
        }

        item {
            BrandedSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search this collection"
            )
        }

        item {
            SectionHeader(title = "Collections")
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(collections, key = { collection -> collection.id }) { collection ->
                    PlaylistCollectionCard(
                        collection = collection,
                        selected = collection.id == selectedCollectionId,
                        onClick = { selectedCollectionId = collection.id }
                    )
                }
            }
        }

        if (selectedCollection != null) {
            item {
                PlaylistHeroCard(
                    collection = selectedCollection,
                    targetNotebook = heroTarget,
                    isBusy = heroTarget?.uuid == loadingNotebookUuid,
                    playbackState = playbackState,
                    onPrimaryAction = {
                        heroTarget?.let(::playNotebook)
                            ?: onFeatureRequest("Add notebooks to this playlist first.")
                    }
                )
            }
        }

        item {
            SectionHeader(
                title = if (selectedCollection?.typeLabel == "Playlist") "Queue" else "Notebook list"
            )
        }

        item {
            Text(
                text = when {
                    filteredNotebooks.isEmpty() && query.isNotBlank() -> "No tracks match \"$query\"."
                    selectedCollection?.notebooks?.isEmpty() == true -> "This collection is empty."
                    else -> "${filteredNotebooks.size} track${if (filteredNotebooks.size == 1) "" else "s"} ready for playback."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )
        }

        if (filteredNotebooks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (query.isNotBlank()) "Nothing matched your search" else "No notebooks in this collection yet",
                    body = if (query.isNotBlank()) {
                        "Try a broader title or category keyword."
                    } else if (selectedCollection?.typeLabel == "Playlist") {
                        "The web queue is already feeding mobile. Once a playlist has notebooks, they appear here with direct playback."
                    } else {
                        "Create or sync notebooks from your library to build a mobile listening queue."
                    }
                )
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredNotebooks.forEachIndexed { index, notebook ->
                        PlaylistTrackRow(
                            index = index,
                            notebook = notebook,
                            isLoading = loadingNotebookUuid == notebook.uuid,
                            isActive = playbackState.notebookId == notebook.uuid,
                            isPlaying = playbackState.notebookId == notebook.uuid && playbackState.isPlaying,
                            onPlay = { playNotebook(notebook) },
                            onOpen = { onOpenNotebook(notebook.uuid) }
                        )
                    }
                }
            }
        }
    }
}

private fun buildPlaylistCollections(
    notebooks: List<NotebookSummary>,
    playlists: List<PlaylistSummary>
): List<PlaylistCollectionUi> {
    val allLibrary = PlaylistCollectionUi(
        id = AllLibraryCollectionId,
        title = "All notebooks",
        typeLabel = "Collection",
        notebooks = notebooks,
        currentIndex = 0,
        colors = PlaylistPalettes.first()
    )

    val playlistCollections = playlists.mapIndexed { index, playlist ->
        PlaylistCollectionUi(
            id = playlist.uuid,
            title = playlist.title.ifBlank { "Untitled playlist" },
            typeLabel = "Playlist",
            notebooks = playlist.queue,
            currentIndex = playlist.currentIndex,
            colors = PlaylistPalettes[(index + 1) % PlaylistPalettes.size]
        )
    }

    return listOf(allLibrary) + playlistCollections
}

private fun resolveCollectionStartNotebook(
    collection: PlaylistCollectionUi?,
    filteredNotebooks: List<NotebookSummary>
): NotebookSummary? {
    if (collection == null || filteredNotebooks.isEmpty()) {
        return null
    }

    if (collection.typeLabel != "Playlist") {
        return filteredNotebooks.firstOrNull()
    }

    val currentIndex = collection.currentIndex.coerceIn(0, (collection.notebooks.size - 1).coerceAtLeast(0))
    val orderedQueue = collection.notebooks.drop(currentIndex) + collection.notebooks.take(currentIndex)
    return orderedQueue.firstOrNull { candidate ->
        filteredNotebooks.any { notebook -> notebook.uuid == candidate.uuid }
    } ?: filteredNotebooks.firstOrNull()
}

@Composable
private fun PlaylistCollectionCard(
    collection: PlaylistCollectionUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .widthIn(min = 200.dp, max = 240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = White,
        border = BorderStroke(1.dp, if (selected) Accent.copy(alpha = 0.32f) else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(collection.colors),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${collection.notebooks.size} notebook${if (collection.notebooks.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeroCard(
    collection: PlaylistCollectionUi,
    targetNotebook: NotebookSummary?,
    isBusy: Boolean,
    playbackState: PlaybackUiState,
    onPrimaryAction: () -> Unit
) {
    val isTargetActive = targetNotebook != null && playbackState.notebookId == targetNotebook.uuid && playbackState.isVisible
    val isTargetPlaying = isTargetActive && playbackState.isPlaying

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(collection.colors))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TokenBadge(
                        text = if (collection.typeLabel == "Playlist") "QUEUE" else "LIB",
                        selected = true
                    )
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildCollectionSupportingText(collection, targetNotebook),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.84f)
                    )
                }

                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = White.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isTargetPlaying) Icons.Filled.GraphicEq else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onPrimaryAction,
                    enabled = targetNotebook != null && !isBusy,
                    shape = RoundedCornerShape(999.dp),
                    color = White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Accent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isTargetPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Accent
                            )
                        }
                        Text(
                            text = when {
                                targetNotebook == null -> "Collection empty"
                                isTargetPlaying -> "Pause"
                                isTargetActive -> "Resume"
                                else -> if (collection.typeLabel == "Playlist") "Play playlist" else "Play library"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = Accent
                        )
                    }
                }

                targetNotebook?.let { notebook ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = White.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Starts with ${notebook.title}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun buildCollectionSupportingText(
    collection: PlaylistCollectionUi,
    targetNotebook: NotebookSummary?
): String {
    val countLabel = "${collection.notebooks.size} notebook${if (collection.notebooks.size == 1) "" else "s"}"
    return if (collection.typeLabel == "Playlist") {
        buildString {
            append(countLabel)
            append(" · ")
            append("Current index ${collection.currentIndex + 1}")
            targetNotebook?.let {
                append(" · ")
                append(it.categoryName ?: "Notebook")
            }
        }
    } else {
        buildString {
            append(countLabel)
            append(" · ")
            append("Everything from your library in one audio view")
        }
    }
}

@Composable
private fun PlaylistTrackRow(
    index: Int,
    notebook: NotebookSummary,
    isLoading: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) AccentBg else White,
        border = BorderStroke(1.dp, if (isActive) Accent.copy(alpha = 0.18f) else Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Accent else Ink3
            )

            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = if (isActive) Ink else Cream,
                border = BorderStroke(1.dp, if (isActive) Ink else Border)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = if (isActive) White else Accent,
                            strokeWidth = 2.dp
                        )
                        isPlaying -> Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Pause playback",
                            tint = White
                        )
                        else -> Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play notebook",
                            tint = if (isActive) White else Ink
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notebook.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = joinMeta(
                        notebook.categoryName ?: "Notebook",
                        notebook.updatedAt?.let(::formatShortDate),
                        notebook.wordCount?.let { "$it words" }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = White
                ) {
                    Text(
                        text = if (isPlaying) "Playing" else "Ready",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Accent
                    )
                }
            } else {
                Surface(
                    onClick = onOpen,
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Open notebook",
                            tint = Ink2,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
