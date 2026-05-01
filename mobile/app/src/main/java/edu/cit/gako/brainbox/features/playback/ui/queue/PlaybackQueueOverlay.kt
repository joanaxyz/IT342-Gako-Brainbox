package edu.cit.gako.brainbox.features.playback.ui.queue

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
import edu.cit.gako.brainbox.features.playback.ui.PlayerGreen
import edu.cit.gako.brainbox.features.playback.ui.PlayerMuted
import edu.cit.gako.brainbox.features.playback.ui.PlayerPanel
import edu.cit.gako.brainbox.features.playback.ui.PlayerPanelSoft
import edu.cit.gako.brainbox.features.playback.ui.QueueRow
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

@Composable
internal fun QueueOverlayLayer(
    activePlaylistName: String,
    activePlaylistUuid: String?,
    playlists: List<PlaylistSummary>,
    playbackQueue: List<NotebookSummary>,
    activeIndex: Int,
    isShuffling: Boolean,
    showPlaylistChooser: Boolean,
    onTogglePlaylistChooser: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink.copy(alpha = 0.16f))
                .clickable(onClick = onDismiss)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.72f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = PlayerPanel,
            border = BorderStroke(1.dp, Border),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 46.dp, height = 5.dp)
                        .background(Border, RoundedCornerShape(999.dp))
                )

                QueueOverlayHeader(
                    activePlaylistName = activePlaylistName,
                    queueSize = playbackQueue.size,
                    isShuffling = isShuffling,
                    hasActivePlaylist = activePlaylistUuid != null,
                    onTogglePlaylistChooser = onTogglePlaylistChooser
                )

                if (showPlaylistChooser) {
                    PlaylistChooserPanel(
                        playlists = playlists,
                        activePlaylistUuid = activePlaylistUuid,
                        onSelectPlaylist = onSelectPlaylist
                    )
                }

                if (playbackQueue.isEmpty()) {
                    Text(
                        text = "Choose a playlist to use as the queue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlayerMuted,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                    playbackQueue.forEachIndexed { index, notebook ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = Border,
                                thickness = 0.5.dp
                            )
                        }
                        QueueRow(
                            index = index,
                            notebook = notebook,
                            isActive = index == activeIndex
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueOverlayHeader(
    activePlaylistName: String,
    queueSize: Int,
    isShuffling: Boolean,
    hasActivePlaylist: Boolean,
    onTogglePlaylistChooser: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Text(
                    text = if (isShuffling) {
                        "Shuffling from $activePlaylistName"
                    } else {
                        "Playing from $activePlaylistName"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PlayerMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onTogglePlaylistChooser,
                shape = RoundedCornerShape(999.dp),
                color = AccentBg,
                border = BorderStroke(1.dp, Accent.copy(alpha = 0.34f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = AccentDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (hasActivePlaylist) "Replace" else "Choose",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentDark,
                        maxLines = 1
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = PlayerPanelSoft,
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink3
                    )
                    Text(
                        text = activePlaylistName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$queueSize notebooks",
                    style = MaterialTheme.typography.labelMedium,
                    color = PlayerMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlaylistChooserPanel(
    playlists: List<PlaylistSummary>,
    activePlaylistUuid: String?,
    onSelectPlaylist: (String) -> Unit
) {
    if (playlists.isEmpty()) {
        Text(
            text = "Create a playlist first to use it as your queue.",
            style = MaterialTheme.typography.bodyMedium,
            color = PlayerMuted,
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .background(PlayerPanelSoft, RoundedCornerShape(16.dp))
            .padding(vertical = 6.dp)
    ) {
        playlists.forEach { playlist ->
            val selected = playlist.uuid == activePlaylistUuid
            Surface(
                onClick = { onSelectPlaylist(playlist.uuid) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) PlayerGreen.copy(alpha = 0.14f) else PlayerPanelSoft
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = playlist.title.trim()
                                .take(2)
                                .uppercase(Locale.ENGLISH)
                                .ifBlank { "PL" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = AccentDark
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = playlist.title.ifBlank { "Untitled playlist" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) AccentDark else Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${playlist.queue.size} notebooks",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlayerMuted,
                            maxLines = 1
                        )
                    }
                    if (selected) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentDark
                        )
                    }
                }
            }
        }
    }
}
