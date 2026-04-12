package edu.cit.gako.brainbox.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White

@Immutable
internal data class NotebookReviewStat(
    val label: String,
    val value: String,
    val icon: ImageVector? = null
)

@Composable
internal fun NotebookReviewHeroCard(
    title: String,
    subtitle: String,
    statusText: String,
    modifier: Modifier = Modifier,
    stats: List<NotebookReviewStat> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.78f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink2,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    NotebookReviewStatusPill(statusText = statusText)
                }
                if (stats.isNotEmpty()) {
                    NotebookReviewCompactStats(stats = stats)
                }
            }

            content()
        }
    }
}

@Composable
internal fun NotebookReviewPlaybackControlsRow(
    playback: PlaybackUiState,
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit,
    onStop: () -> Unit,
    onSpeed: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrimaryAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = White
            )
        ) {
            Icon(
                imageVector = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text(
                text = when {
                    playback.isPlaying -> "Pause"
                    playback.isVisible -> "Resume"
                    else -> "Play review"
                }
            )
        }

        OutlinedButton(
            onClick = onStop,
            enabled = playback.isVisible
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text("Stop")
        }

        OutlinedButton(onClick = onSpeed) {
            Icon(Icons.Filled.Speed, contentDescription = null)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text("${playback.speechRate}x")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotebookReviewHeadingChips(
    headings: List<String>,
    modifier: Modifier = Modifier,
    onHeadingClick: ((index: Int, heading: String) -> Unit)? = null
) {
    if (headings.isEmpty()) {
        Text(
            text = "No headings detected yet.",
            style = MaterialTheme.typography.bodySmall,
            color = Ink3,
            modifier = modifier
        )
        return
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        headings.forEachIndexed { index, heading ->
            AssistChip(
                onClick = { onHeadingClick?.invoke(index, heading) },
                enabled = onHeadingClick != null,
                label = {
                    Text(
                        text = heading,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Subject, contentDescription = null)
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotebookReviewTranscriptWordChips(
    transcript: String,
    activeWordRange: IntRange? = null,
    modifier: Modifier = Modifier,
    onWordClick: ((index: Int, word: String) -> Unit)? = null
) {
    val tokens = rememberTranscriptTokens(transcript)

    if (tokens.isEmpty()) {
        Text(
            text = "No transcript text yet.",
            style = MaterialTheme.typography.bodySmall,
            color = Ink3,
            modifier = modifier
        )
        return
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tokens.forEachIndexed { index, token ->
            val isActive = activeWordRange?.let { range ->
                token.start <= range.last && token.end >= range.first
            } ?: false

            NotebookReviewWordChip(
                word = token.word,
                active = isActive,
                onClick = onWordClick?.let { { it(index, token.word) } }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotebookReviewCompactStats(
    stats: List<NotebookReviewStat>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        stats.forEach { stat ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Cream2,
                border = BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stat.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.labelLarge,
                            color = Ink
                        )
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink3
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun NotebookReviewStatusPill(
    statusText: String,
    modifier: Modifier = Modifier
) {
    val colors = NotebookReviewStatusColor.forText(statusText)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NotebookReviewWordChip(
    word: String,
    active: Boolean,
    onClick: (() -> Unit)?
) {
    val background = if (active) AccentBg else White
    val contentColor = if (active) Ink else Ink2
    val surfaceModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = surfaceModifier,
        shape = RoundedCornerShape(999.dp),
        color = background,
        border = BorderStroke(1.dp, if (active) Accent.copy(alpha = 0.35f) else Border)
    ) {
        Text(
            text = word,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class NotebookReviewWordToken(
    val word: String,
    val start: Int,
    val end: Int
)

private data class NotebookReviewStatusColor(
    val background: Color,
    val border: Color,
    val content: Color
) {
    companion object {
        fun forText(text: String): NotebookReviewStatusColor {
            val normalized = text.lowercase()
            return when {
                normalized.contains("playing") || normalized.contains("tracking") -> NotebookReviewStatusColor(
                    background = AccentBg,
                    border = Accent.copy(alpha = 0.22f),
                    content = Accent
                )
                normalized.contains("paused") -> NotebookReviewStatusColor(
                    background = Cream2,
                    border = Border,
                    content = Ink2
                )
                normalized.contains("ready") || normalized.contains("resume") -> NotebookReviewStatusColor(
                    background = White,
                    border = Border,
                    content = Ink2
                )
                normalized.contains("finished") || normalized.contains("ended") -> NotebookReviewStatusColor(
                    background = Color(0xFFF0FDF4),
                    border = Color(0xFFBBF7D0),
                    content = Color(0xFF166534)
                )
                normalized.contains("error") || normalized.contains("unavailable") -> NotebookReviewStatusColor(
                    background = Color(0xFFFEE2E2),
                    border = Color(0xFFFCA5A5),
                    content = ErrorRed
                )
                else -> NotebookReviewStatusColor(
                    background = White,
                    border = Border,
                    content = Ink3
                )
            }
        }
    }
}

@Composable
private fun rememberTranscriptTokens(transcript: String): List<NotebookReviewWordToken> {
    val regex = Regex("\\S+")
    return androidx.compose.runtime.remember(transcript) {
        regex.findAll(transcript).map { match ->
            NotebookReviewWordToken(
                word = match.value,
                start = match.range.first,
                end = match.range.last
            )
        }.toList()
    }
}
