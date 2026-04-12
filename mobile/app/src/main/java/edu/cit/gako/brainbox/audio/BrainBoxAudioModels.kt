package edu.cit.gako.brainbox.audio

data class BrainBoxTtsChunk(
    val id: String,
    val text: String,
    val startCharIndex: Int = 0,
    val endCharIndex: Int = text.length
)

data class BrainBoxTtsRequest(
    val notebookId: String,
    val notebookTitle: String,
    val chunks: List<BrainBoxTtsChunk>,
    val speechRate: Float = 1.0f,
    val languageTag: String? = null,
    val voiceName: String? = null,
    val startChunkIndex: Int = 0,
    val startCharOffset: Int = 0,
    val offlineOnly: Boolean = false
)

enum class BrainBoxAudioPlaybackStatus {
    IDLE,
    LOADING,
    READY,
    PLAYING,
    PAUSED,
    ENDED,
    UNAVAILABLE,
    ERROR
}

data class BrainBoxAudioSnapshot(
    val request: BrainBoxTtsRequest? = null,
    val status: BrainBoxAudioPlaybackStatus = BrainBoxAudioPlaybackStatus.IDLE,
    val currentChunkIndex: Int = 0,
    val currentCharOffset: Int = 0,
    val currentChunkElapsedMs: Long = 0L,
    val speechRate: Float = 1.0f,
    val offlineVoiceAvailable: Boolean = true,
    val errorMessage: String? = null,
    val updatedAtEpochMs: Long = 0L
) {
    val hasLoadedRequest: Boolean
        get() = request != null && request.chunks.isNotEmpty()

    val currentChunk: BrainBoxTtsChunk?
        get() = request?.chunks?.getOrNull(currentChunkIndex)
}

internal fun BrainBoxAudioPlaybackStatus.toPlayerState(): Int {
    return when (this) {
        BrainBoxAudioPlaybackStatus.IDLE -> androidx.media3.common.Player.STATE_IDLE
        BrainBoxAudioPlaybackStatus.LOADING -> androidx.media3.common.Player.STATE_BUFFERING
        BrainBoxAudioPlaybackStatus.READY -> androidx.media3.common.Player.STATE_READY
        BrainBoxAudioPlaybackStatus.PLAYING -> androidx.media3.common.Player.STATE_READY
        BrainBoxAudioPlaybackStatus.PAUSED -> androidx.media3.common.Player.STATE_READY
        BrainBoxAudioPlaybackStatus.ENDED -> androidx.media3.common.Player.STATE_ENDED
        BrainBoxAudioPlaybackStatus.UNAVAILABLE -> androidx.media3.common.Player.STATE_IDLE
        BrainBoxAudioPlaybackStatus.ERROR -> androidx.media3.common.Player.STATE_IDLE
    }
}
