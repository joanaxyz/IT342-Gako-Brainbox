package edu.cit.gako.brainbox.features.playback.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.features.playback.ui.player.nextPlaybackRate

@Composable
internal fun PlaybackOverlay(
    playbackState: PlaybackUiState,
    playbackQueue: List<NotebookSummary>,
    playlists: List<PlaylistSummary>,
    activePlaylistUuid: String?,
    activePlaylistTitle: String?,
    currentQueueIndex: Int,
    isLooping: Boolean,
    isShuffling: Boolean,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onStartQueue: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowPlaybarOverlay = playbackState.isVisible || playbackQueue.isNotEmpty()
    if (!shouldShowPlaybarOverlay) {
        return
    }

    val context = LocalContext.current
    val audioClient = remember(context.applicationContext) {
        BrainBoxAudioClient(context.applicationContext)
    }

    Box(modifier = modifier) {
        if (isExpanded) {
            GlobalPlaybar(
                playbackState = playbackState,
                queueSize = playbackQueue.size,
                playbackQueue = playbackQueue,
                playlists = playlists,
                activePlaylistUuid = activePlaylistUuid,
                activePlaylistTitle = activePlaylistTitle,
                currentQueueIndex = currentQueueIndex,
                isLooping = isLooping,
                isShuffling = isShuffling,
                onResume = audioClient::resume,
                onPause = audioClient::pause,
                onReplay = {
                    audioClient.seekToChunk(playbackState.currentChunkIndex.coerceAtLeast(0))
                },
                onCycleRate = {
                    audioClient.setSpeechRate(nextPlaybackRate(playbackState.speechRate))
                },
                onStartQueue = onStartQueue,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleLoop = onToggleLoop,
                onToggleShuffle = onToggleShuffle,
                onSelectPlaylist = onSelectPlaylist,
                onCollapse = { onExpandedChange(false) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CollapsedPlaybar(
                playbackState = playbackState,
                title = playbackState.notebookTitle.ifBlank {
                    activePlaylistTitle ?: "Playback ready"
                },
                onExpand = { onExpandedChange(true) },
                onPlayPause = {
                    if (playbackState.isPlaying) audioClient.pause()
                    else if (playbackState.canResume) audioClient.resume()
                    else onStartQueue()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
