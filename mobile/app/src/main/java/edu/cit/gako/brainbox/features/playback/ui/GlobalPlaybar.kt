package edu.cit.gako.brainbox.features.playback.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.playback.ui.player.buildLiveSubtitleWords
import edu.cit.gako.brainbox.features.playback.ui.player.findLiveSubtitleWordIndex
import edu.cit.gako.brainbox.features.playback.ui.player.formatPlaybackRate
import edu.cit.gako.brainbox.features.playback.ui.player.formatPlaybackTime
import edu.cit.gako.brainbox.features.playback.ui.player.playbackProgressFraction
import edu.cit.gako.brainbox.features.playback.ui.queue.QueueOverlayLayer
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.AccentDark
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Cream
import edu.cit.gako.brainbox.shared.ui.theme.Cream2
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.Ink4
import edu.cit.gako.brainbox.shared.ui.theme.White
import java.util.Locale
import kotlinx.coroutines.delay

internal val PlayerBg = Cream
internal val PlayerBgTop = AccentBg
internal val PlayerPanel = White
internal val PlayerPanelSoft = Cream2
internal val PlayerMuted = Ink2
internal val PlayerFaint = Ink4
internal val PlayerGreen = Accent

@Composable
internal fun GlobalPlaybar(
    playbackState: PlaybackUiState,
    queueSize: Int,
    playbackQueue: List<NotebookSummary>,
    playlists: List<PlaylistSummary>,
    activePlaylistUuid: String?,
    activePlaylistTitle: String?,
    currentQueueIndex: Int,
    isLooping: Boolean,
    isShuffling: Boolean,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onReplay: () -> Unit,
    onCycleRate: () -> Unit,
    onStartQueue: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPlayback = playbackState.isVisible && !playbackState.notebookId.isNullOrBlank()
    if (!hasPlayback && playbackQueue.isEmpty()) return

    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showPlaylistChooser by rememberSaveable { mutableStateOf(false) }

    val activePlaylistName = activePlaylistTitle?.takeIf { it.isNotBlank() } ?: "Playlist"
    val activeIndex = if (playbackQueue.isNotEmpty()) {
        currentQueueIndex.coerceIn(0, playbackQueue.lastIndex)
    } else {
        0
    }
    val activeQueueNotebook = playbackState.notebookId
        ?.let { notebookId -> playbackQueue.firstOrNull { it.uuid == notebookId } }
        ?: playbackQueue.getOrNull(activeIndex)
    val titleText = if (hasPlayback) {
        playbackState.notebookTitle.ifBlank { activeQueueNotebook?.title ?: "Audio session" }
    } else {
        activeQueueNotebook?.title ?: activePlaylistName
    }
    val sourceText = activeQueueNotebook?.categoryName ?: activePlaylistName
    val subtitleText = playbackState.fullText
        .takeIf { hasPlayback && it.isNotBlank() }
        ?: playbackState.currentChunkText
            ?.takeIf { hasPlayback && it.isNotBlank() }
        ?: ""

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PlayerBgTop, PlayerBg, Cream),
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            PlayerHeader(
                playlistName = activePlaylistName,
                onCollapse = onCollapse
            )

            PlayerArtwork(title = titleText, source = sourceText)

            PlayerTitleBlock(
                title = titleText,
                source = sourceText,
                playlistName = activePlaylistName
            )

            if (!showQueue && hasPlayback && subtitleText.isNotBlank()) {
                LiveSubtitleStrip(
                    text = subtitleText,
                    activeWordRange = playbackState.activeWordRange,
                    isPlaying = playbackState.isPlaying
                )
            }

            PlayerProgress(playbackState = playbackState, hasPlayback = hasPlayback)

            PlayerControlRow(
                playbackState = playbackState,
                hasPlayback = hasPlayback,
                queueSize = queueSize,
                isLooping = isLooping,
                isShuffling = isShuffling,
                onResume = onResume,
                onPause = onPause,
                onReplay = onReplay,
                onStartQueue = onStartQueue,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleLoop = onToggleLoop,
                onToggleShuffle = onToggleShuffle
            )

            PlayerUtilityRow(
                showQueue = showQueue,
                playbackState = playbackState,
                hasPlayback = hasPlayback,
                queueSize = queueSize,
                onToggleQueue = { showQueue = !showQueue },
                onCycleRate = onCycleRate
            )

            Spacer(Modifier.height(12.dp))
        }

        if (showQueue) {
            QueueOverlayLayer(
                activePlaylistName = activePlaylistName,
                activePlaylistUuid = activePlaylistUuid,
                playlists = playlists,
                playbackQueue = playbackQueue,
                activeIndex = activeIndex,
                isShuffling = isShuffling,
                showPlaylistChooser = showPlaylistChooser,
                onTogglePlaylistChooser = { showPlaylistChooser = !showPlaylistChooser },
                onSelectPlaylist = { playlistUuid ->
                    showPlaylistChooser = false
                    onSelectPlaylist(playlistUuid)
                },
                onDismiss = {
                    showQueue = false
                    showPlaylistChooser = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PlayerHeader(
    playlistName: String,
    onCollapse: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = "Collapse player",
                tint = Ink,
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "PLAYING FROM PLAYLIST",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Ink3,
                maxLines = 1
            )
            Text(
                text = playlistName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun PlayerArtwork(title: String, source: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AccentBg, Cream2, White)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = source.uppercase(Locale.ENGLISH),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AccentDark.copy(alpha = 0.14f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = title.trim()
                .split(Regex("\\s+"))
                .take(2)
                .joinToString("") { it.take(1) }
                .uppercase(Locale.ENGLISH)
                .ifBlank { "BB" },
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = AccentDark.copy(alpha = 0.9f),
            maxLines = 1
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink2.copy(alpha = 0.42f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun PlayerTitleBlock(
    title: String,
    source: String,
    playlistName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = source.takeIf { it.isNotBlank() } ?: playlistName,
            style = MaterialTheme.typography.titleMedium,
            color = PlayerMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LiveSubtitleStrip(
    text: String,
    activeWordRange: IntRange?,
    isPlaying: Boolean
) {
    val words = remember(text) { buildLiveSubtitleWords(text) }
    val activeIndex = remember(words, activeWordRange) {
        findLiveSubtitleWordIndex(words, activeWordRange)
    }
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex, words.size, isPlaying) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        shape = RoundedCornerShape(999.dp),
        color = White.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Border.copy(alpha = 0.7f))
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = words,
                key = { _, word -> word.id }
            ) { index, word ->
                val isActiveWord = index == activeIndex
                Text(
                    text = word.text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isActiveWord) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isActiveWord) Ink else Ink3,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlayerProgress(playbackState: PlaybackUiState, hasPlayback: Boolean) {
    val liveCurrentTimeMs = rememberLivePlaybackTimeMs(playbackState, hasPlayback)
    val liveProgressFraction = when {
        !hasPlayback -> 0f
        playbackState.durationMs > 0L -> {
            (liveCurrentTimeMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
        }
        else -> playbackProgressFraction(playbackState)
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Border, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(liveProgressFraction)
                    .background(Accent, RoundedCornerShape(999.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlaybackTime(liveCurrentTimeMs),
                style = MaterialTheme.typography.labelMedium,
                color = PlayerMuted
            )
            Text(
                text = formatPlaybackTime(playbackState.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = PlayerMuted
            )
        }
    }
}

@Composable
private fun PlayerControlRow(
    playbackState: PlaybackUiState,
    hasPlayback: Boolean,
    queueSize: Int,
    isLooping: Boolean,
    isShuffling: Boolean,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onReplay: () -> Unit,
    onStartQueue: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onToggleShuffle, enabled = queueSize > 1) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = if (isShuffling) "Turn shuffle off" else "Turn shuffle on",
                tint = when {
                    queueSize <= 1 -> PlayerFaint
                    isShuffling -> PlayerGreen
                    else -> Ink2
                },
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onSkipPrevious,
            enabled = hasPlayback && queueSize > 1
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Skip to previous",
                tint = if (hasPlayback && queueSize > 1) Ink else PlayerFaint,
                modifier = Modifier.size(38.dp)
            )
        }

        Surface(
            onClick = {
                when {
                    !hasPlayback -> onStartQueue()
                    playbackState.isPlaying -> onPause()
                    playbackState.canResume -> onResume()
                    else -> onReplay()
                }
            },
            shape = CircleShape,
            color = Accent,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.size(78.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) {
                        Icons.Rounded.Pause
                    } else {
                        Icons.Rounded.PlayArrow
                    },
                    contentDescription = when {
                        playbackState.isPlaying -> "Pause audio"
                        hasPlayback -> "Play audio"
                        else -> "Start queue"
                    },
                    tint = White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        IconButton(
            onClick = onSkipNext,
            enabled = hasPlayback && queueSize > 1
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Skip to next",
                tint = if (hasPlayback && queueSize > 1) Ink else PlayerFaint,
                modifier = Modifier.size(38.dp)
            )
        }

        IconButton(onClick = onToggleLoop, enabled = hasPlayback) {
            Icon(
                imageVector = Icons.Rounded.Repeat,
                contentDescription = if (isLooping) "Turn loop off" else "Turn loop on",
                tint = when {
                    !hasPlayback -> PlayerFaint
                    isLooping -> PlayerGreen
                    else -> Ink2
                },
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun PlayerUtilityRow(
    showQueue: Boolean,
    playbackState: PlaybackUiState,
    hasPlayback: Boolean,
    queueSize: Int,
    onToggleQueue: () -> Unit,
    onCycleRate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onCycleRate,
            enabled = hasPlayback,
            shape = RoundedCornerShape(999.dp),
            color = PlayerPanelSoft
        ) {
            Text(
                text = formatPlaybackRate(playbackState.speechRate),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (hasPlayback) Ink else PlayerFaint,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onToggleQueue) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = if (showQueue) "Hide queue" else "Show queue",
                    tint = if (showQueue) PlayerGreen else PlayerMuted,
                    modifier = Modifier.size(24.dp)
                )
                if (queueSize > 0) {
                    Text(
                        text = "$queueSize",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PlayerGreen,
                        modifier = Modifier
                            .background(AccentBg, CircleShape)
                            .padding(horizontal = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun CollapsedPlaybar(
    playbackState: PlaybackUiState,
    title: String,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onExpand,
        modifier = modifier
            .height(58.dp)
            .widthIn(min = 220.dp, max = 420.dp),
        shape = RoundedCornerShape(16.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.ifBlank { "Playlist ready" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = Ink,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = when {
                            playbackState.isPlaying -> "Pause playback"
                            playbackState.isVisible -> "Resume playback"
                            else -> "Start playlist"
                        },
                        tint = White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun QueueRow(
    index: Int,
    notebook: NotebookSummary,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) AccentBg else PlayerPanel)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(AccentBg, Cream2)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notebook.title.trim()
                    .take(2)
                    .uppercase(Locale.ENGLISH)
                    .ifBlank { "${index + 1}" },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = AccentDark
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = if (isActive) "... ${notebook.title}" else notebook.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) AccentDark else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = notebook.categoryName ?: "Notebook audio",
                style = MaterialTheme.typography.bodyMedium,
                color = PlayerMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun rememberLivePlaybackTimeMs(
    playbackState: PlaybackUiState,
    hasPlayback: Boolean
): Long {
    var nowMs by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(
        hasPlayback,
        playbackState.isPlaying,
        playbackState.currentTimeMs,
        playbackState.updatedAtEpochMs
    ) {
        nowMs = System.currentTimeMillis()
        while (hasPlayback && playbackState.isPlaying) {
            delay(250L)
            nowMs = System.currentTimeMillis()
        }
    }

    val elapsedSinceSnapshotMs = if (
        hasPlayback &&
        playbackState.isPlaying &&
        playbackState.updatedAtEpochMs > 0L
    ) {
        (nowMs - playbackState.updatedAtEpochMs).coerceAtLeast(0L)
    } else {
        0L
    }

    val durationMs = playbackState.durationMs.coerceAtLeast(0L)
    return if (durationMs > 0L) {
        (playbackState.currentTimeMs + elapsedSinceSnapshotMs).coerceIn(0L, durationMs)
    } else {
        playbackState.currentTimeMs.coerceAtLeast(0L)
    }
}
