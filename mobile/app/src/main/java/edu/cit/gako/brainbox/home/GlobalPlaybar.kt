package edu.cit.gako.brainbox.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import java.util.Locale

@Composable
internal fun GlobalPlaybar(
    playbackState: PlaybackUiState,
    queueSize: Int,
    playbackQueue: List<NotebookSummary>,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onReplay: () -> Unit,
    onSkipNext: () -> Unit,
    onRemoveFromQueue: (String) -> Unit,
    onCollapse: () -> Unit
) {
    val notebookId = playbackState.notebookId
    if (!playbackState.isVisible || notebookId.isNullOrBlank()) return

    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showSubtitles by rememberSaveable { mutableStateOf(true) }

    val chunkText = playbackState.currentChunkText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Subtitle strip
        AnimatedVisibility(
            visible = showSubtitles && !chunkText.isNullOrBlank(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            if (!chunkText.isNullOrBlank()) {
                SubtitleWordStrip(
                    text = chunkText,
                    activeWordRange = playbackState.activeWordRange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = White,
            border = BorderStroke(1.dp, Border),
            shadowElevation = 20.dp
        ) {
            // ── Expanded full player ───────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress + collapse row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatPlaybackTime(playbackState.currentTimeMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink2
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Cream2, RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(playbackProgressFraction(playbackState))
                                .background(Accent, RoundedCornerShape(999.dp))
                        )
                    }
                    Text(
                        text = formatPlaybackTime(playbackState.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink2
                    )
                }

                    // Artwork + title + primary controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Artwork
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AccentBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = playbackState.notebookTitle.trim()
                                    .take(2).uppercase(Locale.ENGLISH).ifBlank { "BB" },
                                style = MaterialTheme.typography.titleMedium,
                                color = Accent
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = playbackState.notebookTitle.ifBlank { "Notebook audio" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildPlaybackStatusLine(playbackState),
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink3,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onReplay, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Filled.Replay,
                                    contentDescription = "Replay section",
                                    tint = Ink2,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Surface(
                                onClick = {
                                    if (playbackState.isPlaying) onPause()
                                    else if (playbackState.canResume) onResume()
                                    else onReplay()
                                },
                                shape = CircleShape,
                                color = Ink,
                                shadowElevation = 6.dp
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onSkipNext,
                                enabled = queueSize > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.SkipNext,
                                    contentDescription = "Skip to next",
                                    tint = if (queueSize > 0) Ink2 else Ink3,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Secondary controls: subtitle, queue, rate, collapse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Subtitle toggle
                        IconButton(
                            onClick = { showSubtitles = !showSubtitles },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.ClosedCaption,
                                contentDescription = if (showSubtitles) "Hide subtitles" else "Show subtitles",
                                tint = if (showSubtitles) Accent else Ink3,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Queue toggle with badge
                        BadgedBox(
                            badge = {
                                if (queueSize > 0) {
                                    Badge { Text("$queueSize", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { showQueue = !showQueue },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Queue,
                                    contentDescription = "Toggle queue",
                                    tint = if (showQueue) Accent else Ink3,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Speech rate label
                        Text(
                            text = "${playbackState.speechRate}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink3,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        if (playbackState.isOfflineOnly) {
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink3
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        // Collapse
                        IconButton(
                            onClick = onCollapse,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.ExpandMore,
                                contentDescription = "Collapse player",
                                tint = Ink3,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
        }

        // Inline queue panel
        AnimatedVisibility(
            visible = showQueue && playbackQueue.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 8.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Up next",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink2
                        )
                        Text(
                            "$queueSize track${if (queueSize == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    playbackQueue.take(6).forEachIndexed { index, notebook ->
                        if (index > 0) HorizontalDivider(color = Border, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink3,
                                modifier = Modifier.width(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    notebook.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (notebook.categoryName != null) {
                                    Text(
                                        notebook.categoryName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ink3,
                                        maxLines = 1
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onRemoveFromQueue(notebook.uuid) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove from queue",
                                    tint = Ink3,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    if (playbackQueue.size > 6) {
                        Text(
                            text = "+${playbackQueue.size - 6} more in queue",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink3,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun buildCollapsedStatusLine(playbackState: PlaybackUiState): String {
    return buildTimeRangeLabel(playbackState) ?: buildPlaybackStatusLine(playbackState)
}

private fun buildTimeRangeLabel(playbackState: PlaybackUiState): String? {
    if (playbackState.durationMs <= 0L) {
        return null
    }

    return "${formatPlaybackTime(playbackState.currentTimeMs)} / ${formatPlaybackTime(playbackState.durationMs)}"
}

private fun formatPlaybackTime(timeMs: Long): String {
    val totalSeconds = (timeMs.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
}

private fun buildPlaybackStatusLine(playbackState: PlaybackUiState): String {
    val status = when {
        playbackState.isPlaying -> "Playing"
        playbackState.canResume -> "Paused"
        else -> "Ready"
    }

    return buildString {
        append(status)
        append(" | ${playbackState.speechRate}x")
        if (playbackState.isOfflineOnly) append(" | Offline")
    }
}

@Composable
internal fun CollapsedPlaybar(
    playbackState: PlaybackUiState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .clickable(onClick = onExpand)
        )

        Surface(
            onClick = onPlayPause,
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = Ink,
            shadowElevation = 14.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Pause playback" else "Resume playback",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SubtitleWordStrip(
    text: String,
    activeWordRange: IntRange?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val words = rememberWordTokens(text)
    val activeWordIndex = if (activeWordRange != null) {
        words.indexOfFirst { token ->
            token.start <= activeWordRange.last && token.end >= activeWordRange.first
        }.takeIf { it >= 0 }
    } else null

    LaunchedEffect(activeWordIndex) {
        if (activeWordIndex != null && activeWordIndex > 0) {
            scrollState.animateScrollTo((activeWordIndex * 60).coerceAtLeast(0))
        }
    }

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEachIndexed { index, token ->
            val isActive = index == activeWordIndex
            Box(
                modifier = if (isActive) {
                    Modifier
                        .background(AccentBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                }
            ) {
                Text(
                    text = token.word,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) Accent else Ink3,
                    maxLines = 1
                )
            }
        }
    }
}

private data class WordToken(val word: String, val start: Int, val end: Int)

@Composable
private fun rememberWordTokens(text: String): List<WordToken> {
    val regex = Regex("\\S+")
    return androidx.compose.runtime.remember(text) {
        regex.findAll(text).map { match ->
            WordToken(word = match.value, start = match.range.first, end = match.range.last)
        }.toList()
    }
}

internal fun buildChunkProgressLabel(playbackState: PlaybackUiState): String {
    val totalChunks = playbackState.totalChunks.coerceAtLeast(1)
    val currentChunk = (playbackState.currentChunkIndex + 1).coerceIn(1, totalChunks)
    return "$currentChunk / $totalChunks"
}

internal fun playbackProgressFraction(playbackState: PlaybackUiState): Float {
    return playbackState.progressFraction.coerceIn(0f, 1f)
}

private fun buildStatusLine(playbackState: PlaybackUiState): String {
    val status = when {
        playbackState.isPlaying -> "Playing"
        playbackState.canResume -> "Paused"
        else -> "Ready"
    }
    return buildString {
        append(status)
        append(" · ${playbackState.speechRate}x")
        if (playbackState.isOfflineOnly) append(" · Offline")
    }
}
