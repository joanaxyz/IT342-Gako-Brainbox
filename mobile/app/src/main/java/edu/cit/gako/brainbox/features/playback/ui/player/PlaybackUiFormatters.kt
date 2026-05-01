package edu.cit.gako.brainbox.features.playback.ui.player

import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun buildChunkProgressLabel(playbackState: PlaybackUiState): String {
    val totalChunks = playbackState.totalChunks.coerceAtLeast(1)
    val currentChunk = (playbackState.currentChunkIndex + 1).coerceIn(1, totalChunks)
    return "$currentChunk / $totalChunks"
}

internal fun playbackProgressFraction(playbackState: PlaybackUiState): Float {
    return playbackState.progressFraction.coerceIn(0f, 1f)
}

internal data class LiveSubtitleWord(
    val id: String,
    val text: String,
    val start: Int,
    val endExclusive: Int
)

internal fun buildLiveSubtitleWords(text: String): List<LiveSubtitleWord> {
    if (text.isBlank()) {
        return emptyList()
    }

    return Regex("\\S+").findAll(text).mapIndexed { index, match ->
        LiveSubtitleWord(
            id = "subtitle-word-$index-${match.range.first}",
            text = match.value,
            start = match.range.first,
            endExclusive = match.range.last + 1
        )
    }.toList()
}

internal fun findLiveSubtitleWordIndex(
    words: List<LiveSubtitleWord>,
    activeWordRange: IntRange?
): Int {
    if (words.isEmpty() || activeWordRange == null) {
        return -1
    }

    val activeStart = activeWordRange.first
    val activeEndExclusive = activeWordRange.last + 1
    return words.indexOfFirst { word ->
        activeStart < word.endExclusive && activeEndExclusive > word.start
    }
}

internal fun nextPlaybackRate(currentRate: Float): Float {
    val playbackRates = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    val currentIndex = playbackRates.indexOfFirst { abs(it - currentRate) < 0.01f }
    val safeIndex = if (currentIndex >= 0) currentIndex else playbackRates.indexOf(1f)
    return playbackRates[(safeIndex + 1) % playbackRates.size]
}

internal fun formatPlaybackTime(timeMs: Long): String {
    val totalSeconds = (timeMs.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
}

internal fun formatPlaybackRate(rate: Float): String {
    return if (abs(rate - rate.roundToInt().toFloat()) < 0.01f) {
        "${rate.roundToInt()}x"
    } else {
        String.format(Locale.ENGLISH, "%.2fx", rate).replace(".00", "")
    }
}
