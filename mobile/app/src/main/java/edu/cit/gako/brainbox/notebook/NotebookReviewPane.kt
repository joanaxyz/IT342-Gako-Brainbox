package edu.cit.gako.brainbox.notebook

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun NotebookReviewModePane(
    title: String,
    playbackState: PlaybackUiState,
    reviewState: ReviewUiState,
    onSeekToHeading: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (playbackState.status) {
        BrainBoxAudioPlaybackStatus.PLAYING -> "Tracking words live"
        BrainBoxAudioPlaybackStatus.PAUSED -> "Paused in review"
        BrainBoxAudioPlaybackStatus.ENDED -> "Review finished"
        BrainBoxAudioPlaybackStatus.ERROR -> "Playback error"
        BrainBoxAudioPlaybackStatus.UNAVAILABLE -> "Audio unavailable"
        else -> "Ready for review"
    }
    val activeHeading = remember(reviewState.headingTargets, playbackState.currentChunkIndex) {
        reviewState.headingTargets.lastOrNull { it.chunkIndex <= playbackState.currentChunkIndex }?.title
    }
    val progressText = "${(playbackState.currentChunkIndex + 1).coerceAtLeast(1)}/${playbackState.totalChunks.coerceAtLeast(1)}"

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.extraLarge,
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeHeading ?: title.ifBlank { "Notebook review" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink
                    )
                    Text(
                        text = "$statusText • $progressText • ${playbackState.speechRate}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2
                    )
                }
                if (reviewState.headingTargets.isNotEmpty()) {
                    Text(
                        text = "${reviewState.headingTargets.size} headings",
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink2
                    )
                }
            }

            HorizontalDivider(color = Border)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                ReviewTranscript(
                    reviewState = reviewState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ReviewTranscript(
    reviewState: ReviewUiState,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScrollView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                addView(
                    TextView(context).apply {
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setTextColor(android.graphics.Color.parseColor("#1C1917"))
                        textSize = 16f
                        setLineSpacing(0f, 1.32f)
                        setPadding(18, 22, 18, 36)
                    }
                )
            }
        },
        update = { scrollView ->
            val textView = scrollView.getChildAt(0) as TextView
            val source = reviewState.plainText.ifBlank { "This notebook is empty." }
            val span = SpannableString(source)
            reviewState.activeWordRange?.let { range ->
                if (range.first in source.indices) {
                    span.setSpan(
                        BackgroundColorSpan(android.graphics.Color.parseColor("#FDE68A")),
                        range.first,
                        (range.last + 1).coerceAtMost(source.length),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    textView.post {
                        val layout = textView.layout ?: return@post
                        val safeOffset = range.first.coerceIn(0, source.length.coerceAtLeast(1) - 1)
                        val line = layout.getLineForOffset(safeOffset)
                        scrollView.smoothScrollTo(0, (layout.getLineTop(line) - scrollView.height / 3).coerceAtLeast(0))
                    }
                }
            }
            textView.text = span
        }
    )
}
