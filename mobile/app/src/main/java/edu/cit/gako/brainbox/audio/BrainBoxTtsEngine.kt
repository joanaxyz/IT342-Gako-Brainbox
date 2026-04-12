package edu.cit.gako.brainbox.audio

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class BrainBoxTtsEngine(
    context: Context,
    private val scope: CoroutineScope,
    private val onSnapshotChanged: (BrainBoxAudioSnapshot) -> Unit = {}
) {
    private val appContext = context.applicationContext
    private val store = BrainBoxAudioStore(appContext)

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var initialized = false

    @Volatile
    private var pendingAutoplay = false

    private var currentRequest: BrainBoxTtsRequest? = null
    private var currentChunkStartedAtElapsedRealtimeMs = 0L

    private val snapshotState = kotlinx.coroutines.flow.MutableStateFlow(BrainBoxAudioSnapshot())
    val snapshotFlow = snapshotState

    init {
        scope.launch(Dispatchers.Main.immediate) {
            tts = TextToSpeech(appContext) { status ->
                initialized = status == TextToSpeech.SUCCESS
                if (initialized) {
                    tts?.setOnUtteranceProgressListener(progressListener)
                } else {
                    updateSnapshot(
                        snapshotState.value.copy(
                            status = BrainBoxAudioPlaybackStatus.ERROR,
                            errorMessage = "TextToSpeech failed to initialize.",
                            updatedAtEpochMs = System.currentTimeMillis()
                        )
                    )
                }

                if (initialized && pendingAutoplay && currentRequest != null) {
                    pendingAutoplay = false
                    speakCurrentChunk()
                }
            }
        }
    }

    fun restore(snapshot: BrainBoxAudioSnapshot) {
        currentRequest = snapshot.request
        pendingAutoplay = snapshot.status == BrainBoxAudioPlaybackStatus.PLAYING
        updateSnapshot(snapshot.copy(updatedAtEpochMs = System.currentTimeMillis()), persist = false)
    }

    fun load(request: BrainBoxTtsRequest, autoplay: Boolean = false) {
        val normalizedRequest = request.copy(
            speechRate = request.speechRate.coerceIn(0.25f, 3.0f),
            startChunkIndex = request.startChunkIndex.coerceIn(0, (request.chunks.size - 1).coerceAtLeast(0)),
            startCharOffset = request.startCharOffset.coerceAtLeast(0)
        )
        currentRequest = normalizedRequest
        currentChunkStartedAtElapsedRealtimeMs = 0L

        val voiceAvailable = if (tts == null) {
            !normalizedRequest.offlineOnly
        } else {
            isOfflineVoiceAvailable(normalizedRequest)
        }
        val status = when {
            normalizedRequest.offlineOnly && tts == null -> BrainBoxAudioPlaybackStatus.LOADING
            normalizedRequest.offlineOnly && !voiceAvailable -> BrainBoxAudioPlaybackStatus.UNAVAILABLE
            normalizedRequest.chunks.isEmpty() -> BrainBoxAudioPlaybackStatus.IDLE
            else -> BrainBoxAudioPlaybackStatus.READY
        }

        updateSnapshot(
            BrainBoxAudioSnapshot(
                request = normalizedRequest,
                status = status,
                currentChunkIndex = normalizedRequest.startChunkIndex,
                currentCharOffset = normalizedRequest.startCharOffset,
                currentChunkElapsedMs = 0L,
                speechRate = normalizedRequest.speechRate,
                offlineVoiceAvailable = voiceAvailable,
                errorMessage = if (status == BrainBoxAudioPlaybackStatus.UNAVAILABLE) {
                    "Offline TTS voice is not available on this device."
                } else {
                    null
                },
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        if (autoplay && status != BrainBoxAudioPlaybackStatus.UNAVAILABLE && status != BrainBoxAudioPlaybackStatus.IDLE) {
            pendingAutoplay = true
            if (initialized) {
                pendingAutoplay = false
                speakCurrentChunk()
            }
        }
    }

    fun play() {
        val snapshot = snapshotState.value
        if (!snapshot.hasLoadedRequest || snapshot.status == BrainBoxAudioPlaybackStatus.UNAVAILABLE) {
            return
        }
        pendingAutoplay = true
        if (!initialized) {
            updateSnapshot(snapshot.copy(status = BrainBoxAudioPlaybackStatus.LOADING, updatedAtEpochMs = System.currentTimeMillis()))
            return
        }
        speakCurrentChunk()
    }

    fun pause() {
        if (!snapshotState.value.hasLoadedRequest) return
        pendingAutoplay = false
        tts?.stop()
        val snapshot = snapshotState.value.copy(
            status = BrainBoxAudioPlaybackStatus.PAUSED,
            currentChunkElapsedMs = currentChunkElapsedMs(),
            updatedAtEpochMs = System.currentTimeMillis()
        )
        updateSnapshot(snapshot)
    }

    fun stop(resetToStart: Boolean = true) {
        pendingAutoplay = false
        tts?.stop()
        val request = currentRequest
        val (chunkIndex, charOffset) = if (resetToStart && request != null) {
            request.startChunkIndex to request.startCharOffset
        } else {
            snapshotState.value.currentChunkIndex to snapshotState.value.currentCharOffset
        }
        updateSnapshot(
            snapshotState.value.copy(
                status = if (request == null) BrainBoxAudioPlaybackStatus.IDLE else BrainBoxAudioPlaybackStatus.READY,
                currentChunkIndex = chunkIndex,
                currentCharOffset = charOffset,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    fun seekToChunk(chunkIndex: Int) {
        val request = currentRequest ?: return
        val clampedIndex = chunkIndex.coerceIn(0, (request.chunks.size - 1).coerceAtLeast(0))
        pendingAutoplay = snapshotState.value.status == BrainBoxAudioPlaybackStatus.PLAYING
        tts?.stop()
        updateSnapshot(
            snapshotState.value.copy(
                currentChunkIndex = clampedIndex,
                currentCharOffset = 0,
                currentChunkElapsedMs = 0L,
                status = if (pendingAutoplay) BrainBoxAudioPlaybackStatus.PLAYING else BrainBoxAudioPlaybackStatus.READY,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        if (pendingAutoplay && initialized) {
            pendingAutoplay = false
            speakCurrentChunk()
        }
    }

    fun setSpeechRate(rate: Float) {
        val normalizedRate = rate.coerceIn(0.25f, 3.0f)
        tts?.setSpeechRate(normalizedRate)
        val request = currentRequest?.copy(speechRate = normalizedRate)
        if (request != null) {
            currentRequest = request
        }
        updateSnapshot(
            snapshotState.value.copy(
                request = request,
                speechRate = normalizedRate,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    fun release() {
        pendingAutoplay = false
        currentRequest = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
        updateSnapshot(
            BrainBoxAudioSnapshot(
                status = BrainBoxAudioPlaybackStatus.IDLE,
                updatedAtEpochMs = System.currentTimeMillis()
            ),
            persist = false
        )
    }

    fun hasPlayableRequest(): Boolean = snapshotState.value.hasLoadedRequest

    fun currentSnapshot(): BrainBoxAudioSnapshot = snapshotState.value

    private fun speakCurrentChunk() {
        val request = currentRequest ?: return
        val ttsInstance = tts ?: run {
            pendingAutoplay = true
            updateSnapshot(snapshotState.value.copy(status = BrainBoxAudioPlaybackStatus.LOADING, updatedAtEpochMs = System.currentTimeMillis()))
            return
        }

        if (request.offlineOnly && !isOfflineVoiceAvailable(request)) {
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.UNAVAILABLE,
                    offlineVoiceAvailable = false,
                    errorMessage = "Offline TTS voice is not available on this device.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
            return
        }

        val chunk = request.chunks.getOrNull(snapshotState.value.currentChunkIndex) ?: run {
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ENDED,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            return
        }

        val localOffset = snapshotState.value.currentCharOffset.coerceAtLeast(chunk.startCharIndex)
        val relativeOffset = (localOffset - chunk.startCharIndex).coerceIn(0, chunk.text.length)
        val remainingText = chunk.text.substring(relativeOffset)
        if (remainingText.isBlank()) {
            speakNextChunk()
            return
        }

        configureVoice(request, ttsInstance)
        ttsInstance.setSpeechRate(request.speechRate)
        currentChunkStartedAtElapsedRealtimeMs = SystemClock.elapsedRealtime()

        updateSnapshot(
            snapshotState.value.copy(
                status = BrainBoxAudioPlaybackStatus.PLAYING,
                speechRate = request.speechRate,
                offlineVoiceAvailable = snapshotState.value.offlineVoiceAvailable,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        val utteranceId = buildUtteranceId(request, chunk)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        ttsInstance.speak(remainingText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun speakNextChunk() {
        val request = currentRequest ?: return
        val nextIndex = snapshotState.value.currentChunkIndex + 1
        if (nextIndex >= request.chunks.size) {
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ENDED,
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
            return
        }
        updateSnapshot(
            snapshotState.value.copy(
                currentChunkIndex = nextIndex,
                currentCharOffset = request.chunks[nextIndex].startCharIndex,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        speakCurrentChunk()
    }

    private fun configureVoice(request: BrainBoxTtsRequest, ttsInstance: TextToSpeech) {
        val locale = request.languageTag?.takeIf { it.isNotBlank() }?.let(Locale::forLanguageTag) ?: Locale.getDefault()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val matchingVoice = request.voiceName?.let { voiceName ->
                ttsInstance.voices?.firstOrNull { it.name == voiceName }
            } ?: ttsInstance.voices?.firstOrNull { voice ->
                !voice.isNetworkConnectionRequired && voice.locale.language == locale.language
            }
            if (matchingVoice != null) {
                ttsInstance.voice = matchingVoice
                ttsInstance.language = matchingVoice.locale
            } else {
                ttsInstance.language = locale
            }
        } else {
            @Suppress("DEPRECATION")
            ttsInstance.language = locale
        }
    }

    private fun isOfflineVoiceAvailable(request: BrainBoxTtsRequest): Boolean {
        val ttsInstance = tts ?: return true
        if (!request.offlineOnly) return true

        val locale = request.languageTag?.takeIf { it.isNotBlank() }?.let(Locale::forLanguageTag) ?: Locale.getDefault()
        val matchingVoices = ttsInstance.voices ?: return false
        val requestedVoice = request.voiceName?.let { voiceName ->
            matchingVoices.firstOrNull { it.name == voiceName }
        }

        if (requestedVoice != null) {
            return !requestedVoice.isNetworkConnectionRequired
        }

        return matchingVoices.any { voice ->
            !voice.isNetworkConnectionRequired && voice.locale.language == locale.language
        }
    }

    private fun buildUtteranceId(request: BrainBoxTtsRequest, chunk: BrainBoxTtsChunk): String {
        return "${request.notebookId}:${chunk.id}:${snapshotState.value.currentCharOffset}"
    }

    private fun currentChunkElapsedMs(): Long {
        if (currentChunkStartedAtElapsedRealtimeMs == 0L) return snapshotState.value.currentChunkElapsedMs
        return SystemClock.elapsedRealtime() - currentChunkStartedAtElapsedRealtimeMs
    }

    private fun updateSnapshot(snapshot: BrainBoxAudioSnapshot, persist: Boolean = true) {
        snapshotState.value = snapshot
        onSnapshotChanged(snapshot)
        if (persist) {
            scope.launch {
                store.persistSnapshot(snapshot)
            }
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            val request = currentRequest ?: return
            val chunk = snapshotState.value.currentChunk ?: return
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.PLAYING,
                    currentChunkIndex = snapshotState.value.currentChunkIndex,
                    currentCharOffset = chunk.startCharIndex,
                    currentChunkElapsedMs = 0L,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            currentChunkStartedAtElapsedRealtimeMs = SystemClock.elapsedRealtime()
            if (request.offlineOnly && !snapshotState.value.offlineVoiceAvailable) {
                stop()
            }
        }

        override fun onDone(utteranceId: String?) {
            val request = currentRequest ?: return
            val chunk = snapshotState.value.currentChunk ?: return
            val spokenCharOffset = (chunk.startCharIndex + chunk.text.length).coerceAtLeast(chunk.startCharIndex)
            updateSnapshot(
                snapshotState.value.copy(
                    currentCharOffset = spokenCharOffset,
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            speakNextChunk()
            if (snapshotState.value.currentChunkIndex >= request.chunks.size - 1 && snapshotState.value.status == BrainBoxAudioPlaybackStatus.ENDED) {
                pendingAutoplay = false
            }
        }

        override fun onError(utteranceId: String?) {
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ERROR,
                    errorMessage = "TTS playback failed.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            val chunk = snapshotState.value.currentChunk ?: return
            val absoluteOffset = (chunk.startCharIndex + start).coerceAtLeast(chunk.startCharIndex)
            updateSnapshot(
                snapshotState.value.copy(
                    currentCharOffset = absoluteOffset,
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }
}
