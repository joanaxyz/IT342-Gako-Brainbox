package edu.cit.gako.brainbox.audio

import kotlin.math.max
import kotlin.math.roundToLong

data class PlaybackUiState(
    val notebookId: String? = null,
    val notebookTitle: String = "",
    val isVisible: Boolean = false,
    val isPlaying: Boolean = false,
    val canResume: Boolean = false,
    val isOfflineOnly: Boolean = false,
    val status: BrainBoxAudioPlaybackStatus = BrainBoxAudioPlaybackStatus.IDLE,
    val speechRate: Float = 1.0f,
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val progressFraction: Float = 0f,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentCharOffset: Int = 0,
    val activeWordRange: IntRange? = null,
    val currentChunkText: String? = null,
    val errorMessage: String? = null
)

fun BrainBoxAudioSnapshot.toPlaybackUiState(): PlaybackUiState {
    val request = request
    val rate = speechRate.coerceAtLeast(MIN_PLAYBACK_RATE)
    val totalCharacterCount = request?.chunks?.lastOrNull()?.let { chunk ->
        max(chunk.endCharIndex, chunk.startCharIndex + chunk.text.length)
    } ?: 0
    val currentOffset = currentCharOffset.coerceIn(0, totalCharacterCount.coerceAtLeast(0))
    val chunkStartOffset = request?.chunks?.getOrNull(currentChunkIndex)?.startCharIndex ?: currentOffset
    val estimatedDurationMs = estimatePlaybackDurationMs(totalCharacterCount, rate)
    val estimatedCurrentTimeMs = estimatePlaybackDurationMs(currentOffset, rate)
    val chunkStartTimeMs = estimatePlaybackDurationMs(chunkStartOffset, rate)
    val currentTimeMs = max(estimatedCurrentTimeMs, chunkStartTimeMs + currentChunkElapsedMs)
        .coerceAtMost(estimatedDurationMs)
    val progressFraction = when {
        totalCharacterCount <= 0 -> 0f
        else -> (currentOffset.toFloat() / totalCharacterCount.toFloat()).coerceIn(0f, 1f)
    }

    return PlaybackUiState(
        notebookId = request?.notebookId,
        notebookTitle = request?.notebookTitle.orEmpty(),
        isVisible = hasLoadedRequest,
        isPlaying = status == BrainBoxAudioPlaybackStatus.PLAYING,
        canResume = status == BrainBoxAudioPlaybackStatus.PAUSED || status == BrainBoxAudioPlaybackStatus.READY,
        isOfflineOnly = request?.offlineOnly == true,
        status = status,
        speechRate = rate,
        currentChunkIndex = currentChunkIndex,
        totalChunks = request?.chunks?.size ?: 0,
        progressFraction = progressFraction,
        currentTimeMs = currentTimeMs,
        durationMs = estimatedDurationMs,
        currentCharOffset = currentOffset,
        activeWordRange = request?.resolveActiveWordRange(currentOffset),
        currentChunkText = request?.chunks?.getOrNull(currentChunkIndex)?.text,
        errorMessage = errorMessage
    )
}

private const val CHARS_PER_SECOND = 15f
private const val MIN_PLAYBACK_RATE = 0.25f

private fun estimatePlaybackDurationMs(characterCount: Int, rate: Float): Long {
    if (characterCount <= 0) {
        return 0L
    }

    return ((characterCount / (CHARS_PER_SECOND * rate.coerceAtLeast(MIN_PLAYBACK_RATE))) * 1000f)
        .roundToLong()
}

private fun BrainBoxTtsRequest.resolveActiveWordRange(currentOffset: Int): IntRange? {
    val normalizedText = chunks.joinToString(separator = " ") { it.text }.trim()
    if (normalizedText.isBlank()) {
        return null
    }

    val safeOffset = currentOffset.coerceIn(0, normalizedText.length.coerceAtLeast(1) - 1)
    val wordRegex = Regex("\\S+")
    return wordRegex.findAll(normalizedText)
        .map { it.range }
        .firstOrNull { range ->
            safeOffset in range || safeOffset == (range.last + 1).coerceAtMost(normalizedText.lastIndex)
        }
}
