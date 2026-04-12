package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.shared.BrainBoxPlaybar
import edu.cit.gako.brainbox.shared.BrainBoxPlaybarIconAction

@Composable
internal fun GlobalPlaybar(
    playbackState: PlaybackUiState,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onReplay: () -> Unit,
    onStop: () -> Unit
) {
    val notebookId = playbackState.notebookId
    if (!playbackState.isVisible || notebookId.isNullOrBlank()) {
        return
    }

    BrainBoxPlaybar(
        title = playbackState.notebookTitle.ifBlank { "Notebook audio" },
        supportingText = buildGlobalPlaybarSupportingText(playbackState),
        eyebrow = "Global player",
        progressText = buildChunkProgressLabel(playbackState),
        progress = playbackProgressFraction(playbackState),
        isPlaying = playbackState.isPlaying,
        onPrimaryAction = {
            if (playbackState.isPlaying) {
                onPause()
            } else if (playbackState.canResume) {
                onResume()
            } else {
                onReplay()
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        leadingActions = {
            BrainBoxPlaybarIconAction(
                icon = Icons.Filled.Replay,
                label = "Replay current section",
                onClick = onReplay,
                enabled = playbackState.isVisible
            )
        },
        trailingActions = {
            BrainBoxPlaybarIconAction(
                icon = Icons.Filled.Close,
                label = "Stop playback",
                onClick = onStop,
                enabled = playbackState.isVisible
            )
        }
    )
}

internal fun buildChunkProgressLabel(playbackState: PlaybackUiState): String {
    val totalChunks = playbackState.totalChunks.coerceAtLeast(1)
    val currentChunk = (playbackState.currentChunkIndex + 1).coerceIn(1, totalChunks)
    return "$currentChunk / $totalChunks"
}

internal fun playbackProgressFraction(playbackState: PlaybackUiState): Float {
    val totalChunks = playbackState.totalChunks
    if (totalChunks <= 0) {
        return 0f
    }

    return ((playbackState.currentChunkIndex + 1).toFloat() / totalChunks.toFloat())
        .coerceIn(0f, 1f)
}

private fun buildGlobalPlaybarSupportingText(playbackState: PlaybackUiState): String {
    val status = when {
        playbackState.isPlaying -> "Playing"
        playbackState.canResume -> "Paused"
        else -> "Ready"
    }

    return buildString {
        append(status)
        append(" - ")
        append("${playbackState.speechRate}x")
        if (playbackState.isOfflineOnly) {
            append(" - Offline voice")
        }
    }
}
