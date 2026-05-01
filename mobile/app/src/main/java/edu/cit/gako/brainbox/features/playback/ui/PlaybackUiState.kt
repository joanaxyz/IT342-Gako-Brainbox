package edu.cit.gako.brainbox.features.playback.ui

import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsChunk
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import kotlin.math.max
import kotlin.math.roundToLong

data class PlaybackUiState(
    val notebookId: String? = null,
    val notebookTitle: String = "",
    val isVisible: Boolean = false,
    val isPlaying: Boolean = false,
    val canResume: Boolean = false,
    val status: BrainBoxAudioPlaybackStatus = BrainBoxAudioPlaybackStatus.IDLE,
    val speechRate: Float = 1.0f,
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val progressFraction: Float = 0f,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentCharOffset: Int = 0,
    val activeWordRange: IntRange? = null,
    val fullText: String = "",
    val currentChunkText: String? = null,
    val currentChunkWordRange: IntRange? = null,
    val errorMessage: String? = null,
    val updatedAtEpochMs: Long = 0L
)

fun BrainBoxAudioSnapshot.toPlaybackUiState(): PlaybackUiState {
    val request = request
    val rate = speechRate.coerceAtLeast(MIN_PLAYBACK_RATE)
    val totalCharacterCount = request?.chunks?.lastOrNull()?.let { chunk ->
        max(chunk.endCharIndex, chunk.startCharIndex + chunk.text.length)
    } ?: 0
    val clampedOffset = currentCharOffset.coerceIn(0, totalCharacterCount.coerceAtLeast(0))
    val currentOffset = if (status == BrainBoxAudioPlaybackStatus.ENDED && totalCharacterCount > 0) {
        totalCharacterCount
    } else {
        clampedOffset
    }
    val currentChunk = request?.chunks?.getOrNull(currentChunkIndex)
    val chunkStartOffset = currentChunk?.startCharIndex ?: currentOffset
    val estimatedDurationMs = estimatePlaybackDurationMs(totalCharacterCount, rate)
    val estimatedCurrentTimeMs = estimatePlaybackDurationMs(currentOffset, rate)
    val chunkStartTimeMs = estimatePlaybackDurationMs(chunkStartOffset, rate)
    val currentTimeMs = max(estimatedCurrentTimeMs, chunkStartTimeMs + currentChunkElapsedMs)
        .coerceAtMost(estimatedDurationMs)
    val progressFraction = when {
        totalCharacterCount <= 0 -> 0f
        status == BrainBoxAudioPlaybackStatus.ENDED -> 1f
        else -> (currentOffset.toFloat() / totalCharacterCount.toFloat()).coerceIn(0f, 1f)
    }
    val hasCompletedFullText = status == BrainBoxAudioPlaybackStatus.ENDED ||
        (totalCharacterCount > 0 && currentOffset >= totalCharacterCount)

    return PlaybackUiState(
        notebookId = request?.notebookId,
        notebookTitle = request?.notebookTitle.orEmpty(),
        isVisible = hasLoadedRequest,
        isPlaying = status == BrainBoxAudioPlaybackStatus.PLAYING,
        canResume = status == BrainBoxAudioPlaybackStatus.PAUSED ||
            status == BrainBoxAudioPlaybackStatus.READY ||
            status == BrainBoxAudioPlaybackStatus.ENDED,
        status = status,
        speechRate = rate,
        currentChunkIndex = currentChunkIndex,
        totalChunks = request?.chunks?.size ?: 0,
        progressFraction = progressFraction,
        currentTimeMs = currentTimeMs,
        durationMs = estimatedDurationMs,
        currentCharOffset = currentOffset,
        activeWordRange = if (hasCompletedFullText) null else request?.resolveActiveWordRange(currentOffset),
        fullText = request?.playbackText.orEmpty(),
        currentChunkText = currentChunk?.text,
        currentChunkWordRange = if (hasCompletedFullText) {
            null
        } else {
            request?.let { activeRequest ->
                currentChunk?.resolveWordRange(activeRequest, currentOffset)
            }
        },
        errorMessage = errorMessage,
        updatedAtEpochMs = updatedAtEpochMs
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
    val normalizedText = playbackText
    if (normalizedText.isBlank()) {
        return null
    }
    if (currentOffset >= normalizedText.length) {
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

private fun BrainBoxTtsChunk.resolveWordRange(currentOffset: Int): IntRange? {
    if (text.isBlank()) {
        return null
    }
    if (currentOffset > endCharIndex) {
        return null
    }

    val safeOffset = (currentOffset - startCharIndex)
        .coerceIn(0, text.length.coerceAtLeast(1) - 1)
    val wordRegex = Regex("\\S+")
    return wordRegex.findAll(text)
        .map { it.range }
        .firstOrNull { range ->
            safeOffset in range || safeOffset == (range.last + 1).coerceAtMost(text.lastIndex)
        }
}

private fun BrainBoxTtsChunk.resolveWordRange(
    request: BrainBoxTtsRequest,
    currentOffset: Int
): IntRange? {
    val absoluteRange = request.resolveActiveWordRange(currentOffset) ?: return null
    if (absoluteRange.first < startCharIndex || absoluteRange.last >= endCharIndex) {
        return resolveWordRange(currentOffset)
    }

    val localStart = (absoluteRange.first - startCharIndex).coerceAtLeast(0)
    val localEnd = (absoluteRange.last - startCharIndex).coerceAtMost(text.lastIndex)
    return if (localEnd >= localStart) {
        localStart..localEnd
    } else {
        resolveWordRange(currentOffset)
    }
}
