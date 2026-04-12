package edu.cit.gako.brainbox.audio

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
    val currentCharOffset: Int = 0,
    val activeWordRange: IntRange? = null,
    val errorMessage: String? = null
)

fun BrainBoxAudioSnapshot.toPlaybackUiState(): PlaybackUiState {
    val request = request
    return PlaybackUiState(
        notebookId = request?.notebookId,
        notebookTitle = request?.notebookTitle.orEmpty(),
        isVisible = hasLoadedRequest,
        isPlaying = status == BrainBoxAudioPlaybackStatus.PLAYING,
        canResume = status == BrainBoxAudioPlaybackStatus.PAUSED || status == BrainBoxAudioPlaybackStatus.READY,
        isOfflineOnly = request?.offlineOnly == true,
        status = status,
        speechRate = speechRate,
        currentChunkIndex = currentChunkIndex,
        totalChunks = request?.chunks?.size ?: 0,
        currentCharOffset = currentCharOffset,
        activeWordRange = request?.resolveActiveWordRange(currentCharOffset),
        errorMessage = errorMessage
    )
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
