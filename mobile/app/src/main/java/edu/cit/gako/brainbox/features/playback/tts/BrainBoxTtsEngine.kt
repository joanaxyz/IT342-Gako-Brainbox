package edu.cit.gako.brainbox.features.playback.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioStore
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsChunk
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

private const val TTS_PROGRESS_UPDATE_MS = 180L
private const val TTS_RANGE_FALLBACK_GRACE_MS = 1_400L
private const val TTS_CHUNK_COMPLETION_FALLBACK_GRACE_MS = 900L
private const val TTS_CHUNK_ESTIMATED_COMPLETION_GRACE_MS = 1_800L
private const val TTS_SENTENCE_STALL_RECOVERY_GRACE_MS = 1_200L
private const val TTS_SENTENCE_BOUNDARY_STALL_RECOVERY_GRACE_MS = 4_500L
private const val TTS_NEXT_CHUNK_DELAY_MS = 50L
private const val TTS_RESTART_AFTER_STOP_MS = 90L
private const val ESTIMATED_CHARS_PER_SECOND = 15f

class BrainBoxTtsEngine(
    context: Context,
    private val store: BrainBoxAudioStore,
    private val scope: CoroutineScope,
    private val onSnapshotChanged: (BrainBoxAudioSnapshot) -> Unit = {}
) {
    private val appContext = context.applicationContext
    private val offlineAudioCache = BrainBoxOfflineAudioCache(appContext)

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var preparedAudioPlayer: MediaPlayer? = null

    @Volatile
    private var initialized = false

    @Volatile
    private var pendingAutoplay = false

    @Volatile
    private var currentRequest: BrainBoxTtsRequest? = null

    @Volatile
    private var currentChunkStartedAtElapsedRealtimeMs = 0L

    @Volatile
    private var currentTtsRelativeStartOffset = 0

    @Volatile
    private var lastTtsRangeUpdateElapsedRealtimeMs = 0L

    @Volatile
    private var lastTtsRangeStartedAtElapsedRealtimeMs = 0L

    @Volatile
    private var lastTtsRangeStartRelativeOffset = 0

    @Volatile
    private var lastTtsRangeEndRelativeOffset = 0

    private val playbackGeneration = AtomicLong(0L)

    @Volatile
    private var activeTtsUtteranceId: String? = null

    private var playbackProgressJob: Job? = null
    private var nextChunkJob: Job? = null

    private val snapshotState = kotlinx.coroutines.flow.MutableStateFlow(BrainBoxAudioSnapshot())
    val snapshotFlow = snapshotState

    init {
        scope.launch(Dispatchers.Main.immediate) {
            tts = TextToSpeech(appContext) { status ->
                initialized = status == TextToSpeech.SUCCESS
                if (initialized) {
                    tts?.setOnUtteranceProgressListener(progressListener)
                } else if (currentRequest?.let(::hasPreparedOfflineAudio) != true) {
                    updateSnapshot(
                        snapshotState.value.copy(
                            status = BrainBoxAudioPlaybackStatus.ERROR,
                            errorMessage = "TextToSpeech failed to initialize.",
                            updatedAtEpochMs = System.currentTimeMillis()
                        )
                    )
                }

                if ((initialized || currentRequest?.let(::hasPreparedOfflineAudio) == true) &&
                    pendingAutoplay &&
                    currentRequest != null
                ) {
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
        invalidateActivePlayback()
        stopProgressTracking()
        releasePreparedAudioPlayer()
        tts?.stop()
        val normalizedRequest = request.copy(
            speechRate = request.speechRate.coerceIn(0.25f, 3.0f),
            startChunkIndex = request.startChunkIndex.coerceIn(0, (request.chunks.size - 1).coerceAtLeast(0)),
            startCharOffset = request.startCharOffset.coerceAtLeast(0)
        )
        currentRequest = normalizedRequest
        currentChunkStartedAtElapsedRealtimeMs = 0L
        resetTtsRangeTracking()

        val offlineAudioAvailable = hasPreparedOfflineAudio(normalizedRequest)
        val voiceAvailable = if (offlineAudioAvailable) {
            true
        } else if (tts == null) {
            !normalizedRequest.offlineOnly
        } else {
            isOfflineVoiceAvailable(normalizedRequest)
        }
        val status = when {
            normalizedRequest.offlineOnly && offlineAudioAvailable -> BrainBoxAudioPlaybackStatus.READY
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
                    "Offline audio is not available on this device yet."
                } else {
                    null
                },
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        if (autoplay && status != BrainBoxAudioPlaybackStatus.UNAVAILABLE && status != BrainBoxAudioPlaybackStatus.IDLE) {
            pendingAutoplay = true
            if (initialized || offlineAudioAvailable) {
                pendingAutoplay = false
                speakCurrentChunk()
            }
        }
    }

    fun play() {
        var snapshot = snapshotState.value
        if (!snapshot.hasLoadedRequest || snapshot.status == BrainBoxAudioPlaybackStatus.UNAVAILABLE) {
            return
        }
        if (snapshot.status == BrainBoxAudioPlaybackStatus.PLAYING) {
            return
        }

        if (snapshot.status == BrainBoxAudioPlaybackStatus.ENDED) {
            restartEndedSnapshotFromStart(snapshot)
            snapshot = snapshotState.value
        }

        if (preparedAudioPlayer != null && snapshot.status == BrainBoxAudioPlaybackStatus.PAUSED) {
            startPreparedAudioPlayback()
            return
        }

        pendingAutoplay = true
        if (snapshot.request?.let(::hasPreparedOfflineAudio) == true) {
            pendingAutoplay = false
            speakCurrentChunk()
            return
        }
        if (!initialized) {
            updateSnapshot(snapshot.copy(status = BrainBoxAudioPlaybackStatus.LOADING, updatedAtEpochMs = System.currentTimeMillis()))
            return
        }
        speakCurrentChunk()
    }

    fun pause() {
        val current = snapshotState.value
        if (!current.hasLoadedRequest) return
        pendingAutoplay = false
        if (preparedAudioPlayer != null) {
            val chunk = current.currentChunk
            val pausedCharOffset = chunk?.let(::currentPreparedAudioCharOffset) ?: current.currentCharOffset
            val pausedElapsedMs = currentChunkElapsedMs()
            stopProgressTracking()
            updateSnapshot(
                current.copy(
                    status = BrainBoxAudioPlaybackStatus.PAUSED,
                    currentCharOffset = pausedCharOffset,
                    currentChunkElapsedMs = pausedElapsedMs,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            runCatching { preparedAudioPlayer?.pause() }
            return
        }
        val elapsedMs = currentChunkElapsedMs()
        val pausedCharOffset = current.currentChunk
            ?.let { chunk -> current.currentCharOffset.coerceIn(chunk.startCharIndex, chunk.endCharIndex) }
            ?: current.currentCharOffset
        invalidateActivePlayback()
        stopProgressTracking()
        tts?.stop()
        currentChunkStartedAtElapsedRealtimeMs = 0L
        val snapshot = current.copy(
            status = BrainBoxAudioPlaybackStatus.PAUSED,
            currentCharOffset = pausedCharOffset,
            currentChunkElapsedMs = elapsedMs,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        updateSnapshot(snapshot)
    }

    fun stop(resetToStart: Boolean = true) {
        pendingAutoplay = false
        invalidateActivePlayback()
        releasePreparedAudioPlayer(stopFirst = true)
        tts?.stop()
        stopProgressTracking()
        resetTtsRangeTracking()
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
        invalidateActivePlayback()
        releasePreparedAudioPlayer(stopFirst = true)
        tts?.stop()
        stopProgressTracking()
        resetTtsRangeTracking()
        updateSnapshot(
            snapshotState.value.copy(
                currentChunkIndex = clampedIndex,
                currentCharOffset = request.chunks[clampedIndex].startCharIndex,
                currentChunkElapsedMs = 0L,
                status = if (pendingAutoplay) BrainBoxAudioPlaybackStatus.PLAYING else BrainBoxAudioPlaybackStatus.READY,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        if (pendingAutoplay && initialized) {
            pendingAutoplay = false
            speakCurrentChunk()
        } else if (pendingAutoplay && hasPreparedOfflineAudio(request)) {
            pendingAutoplay = false
            speakCurrentChunk()
        }
    }

    fun setSpeechRate(rate: Float) {
        val normalizedRate = rate.coerceIn(0.25f, 3.0f)
        val snapshot = snapshotState.value
        val shouldRestartTts = preparedAudioPlayer == null &&
            snapshot.status == BrainBoxAudioPlaybackStatus.PLAYING &&
            snapshot.hasLoadedRequest
        val restartCharOffset = snapshot.currentCharOffset
        val restartElapsedMs = currentChunkElapsedMs()
        if (shouldRestartTts) {
            invalidateActivePlayback()
            stopProgressTracking()
            tts?.stop()
            currentChunkStartedAtElapsedRealtimeMs = 0L
        }
        tts?.setSpeechRate(normalizedRate)
        preparedAudioPlayer?.let { player ->
            runCatching {
                player.playbackParams = player.playbackParams.setSpeed(normalizedRate)
            }
        }
        val request = currentRequest?.copy(speechRate = normalizedRate)
        if (request != null) {
            currentRequest = request
        }
        updateSnapshot(
            snapshotState.value.copy(
                request = request,
                speechRate = normalizedRate,
                currentCharOffset = if (shouldRestartTts) restartCharOffset else snapshotState.value.currentCharOffset,
                currentChunkElapsedMs = if (shouldRestartTts) restartElapsedMs else snapshotState.value.currentChunkElapsedMs,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        if (shouldRestartTts && request != null) {
            speakCurrentChunk()
        }
    }

    fun release() {
        pendingAutoplay = false
        currentRequest = null
        invalidateActivePlayback()
        resetTtsRangeTracking()
        releasePreparedAudioPlayer(stopFirst = true)
        tts?.stop()
        stopProgressTracking()
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
        val chunk = request.chunks.getOrNull(snapshotState.value.currentChunkIndex) ?: run {
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ENDED,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            return
        }

        val cachedAudioFile = if (request.offlineOnly) {
            offlineAudioCache.chunkFile(request.notebookId, chunk.id).takeIf { it.isFile && it.length() > 0L }
        } else {
            null
        }
        if (cachedAudioFile != null) {
            playPreparedAudioChunk(request, chunk, cachedAudioFile)
            return
        }

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
                    errorMessage = "Offline audio is not available on this device yet.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
            return
        }

        val localOffset = snapshotState.value.currentCharOffset.coerceAtLeast(chunk.startCharIndex)
        val relativeOffset = (localOffset - chunk.startCharIndex).coerceIn(0, chunk.text.length)
        val remainingText = chunk.text.substring(relativeOffset)
        if (remainingText.isBlank()) {
            speakNextChunk()
            return
        }

        val generation = beginPlaybackGeneration()
        configureVoice(request, ttsInstance)
        ttsInstance.setSpeechRate(request.speechRate)
        currentChunkStartedAtElapsedRealtimeMs = SystemClock.elapsedRealtime()
        currentTtsRelativeStartOffset = relativeOffset
        lastTtsRangeUpdateElapsedRealtimeMs = 0L
        lastTtsRangeStartedAtElapsedRealtimeMs = 0L
        lastTtsRangeStartRelativeOffset = 0
        lastTtsRangeEndRelativeOffset = 0

        updateSnapshot(
            snapshotState.value.copy(
                status = BrainBoxAudioPlaybackStatus.PLAYING,
                speechRate = request.speechRate,
                offlineVoiceAvailable = snapshotState.value.offlineVoiceAvailable,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        startTtsProgressTracking(
            chunk = chunk,
            relativeStartOffset = relativeOffset,
            speechRate = request.speechRate,
            generation = generation
        )

        val utteranceId = buildUtteranceId(request, chunk, generation)
        activeTtsUtteranceId = utteranceId
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        val speakResult = ttsInstance.speak(remainingText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (speakResult == TextToSpeech.ERROR && isCurrentTtsUtterance(utteranceId)) {
            activeTtsUtteranceId = null
            stopProgressTracking()
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ERROR,
                    errorMessage = "TTS playback failed.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
        }
    }

    private fun speakNextChunk() {
        val request = currentRequest ?: return
        val nextIndex = snapshotState.value.currentChunkIndex + 1
        if (nextIndex >= request.chunks.size) {
            stopProgressTracking()
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

    private fun beginPlaybackGeneration(): Long {
        nextChunkJob?.cancel()
        nextChunkJob = null
        val generation = playbackGeneration.incrementAndGet()
        activeTtsUtteranceId = null
        return generation
    }

    private fun invalidateActivePlayback() {
        nextChunkJob?.cancel()
        nextChunkJob = null
        playbackGeneration.incrementAndGet()
        activeTtsUtteranceId = null
    }

    private fun isCurrentTtsUtterance(utteranceId: String?): Boolean {
        return utteranceId != null &&
            utteranceId == activeTtsUtteranceId &&
            utteranceId.startsWith("${playbackGeneration.get()}:")
    }

    private fun isCurrentPreparedAudioPlayer(player: MediaPlayer, generation: Long): Boolean {
        return preparedAudioPlayer === player && playbackGeneration.get() == generation
    }

    private fun buildUtteranceId(
        request: BrainBoxTtsRequest,
        chunk: BrainBoxTtsChunk,
        generation: Long
    ): String {
        return "$generation:${request.notebookId}:${chunk.id}:${snapshotState.value.currentCharOffset}"
    }

    private fun currentChunkElapsedMs(): Long {
        preparedAudioPlayer?.let { player ->
            return runCatching { player.currentPosition.toLong() }.getOrDefault(snapshotState.value.currentChunkElapsedMs)
        }
        if (currentChunkStartedAtElapsedRealtimeMs == 0L) return snapshotState.value.currentChunkElapsedMs
        return SystemClock.elapsedRealtime() - currentChunkStartedAtElapsedRealtimeMs
    }

    private fun updateSnapshot(snapshot: BrainBoxAudioSnapshot, persist: Boolean = true) {
        snapshotState.value = snapshot
        onSnapshotChanged(snapshot)
        store.publishSnapshot(snapshot, persist = persist)
    }

    private fun restartEndedSnapshotFromStart(snapshot: BrainBoxAudioSnapshot) {
        val request = snapshot.request ?: return
        val startChunkIndex = request.startChunkIndex.coerceIn(0, (request.chunks.size - 1).coerceAtLeast(0))
        val startChunk = request.chunks.getOrNull(startChunkIndex)
        val startCharOffset = request.startCharOffset.coerceIn(
            startChunk?.startCharIndex ?: 0,
            startChunk?.endCharIndex ?: request.startCharOffset.coerceAtLeast(0)
        )

        invalidateActivePlayback()
        releasePreparedAudioPlayer(stopFirst = true)
        tts?.stop()
        stopProgressTracking()
        resetTtsRangeTracking()
        currentChunkStartedAtElapsedRealtimeMs = 0L
        updateSnapshot(
            snapshot.copy(
                status = BrainBoxAudioPlaybackStatus.READY,
                currentChunkIndex = startChunkIndex,
                currentCharOffset = startCharOffset,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    private fun hasPreparedOfflineAudio(request: BrainBoxTtsRequest): Boolean {
        return request.offlineOnly && offlineAudioCache.hasPreparedAudio(request)
    }

    private fun playPreparedAudioChunk(
        request: BrainBoxTtsRequest,
        chunk: BrainBoxTtsChunk,
        audioFile: java.io.File
    ) {
        val generation = beginPlaybackGeneration()
        releasePreparedAudioPlayer(stopFirst = true)
        tts?.stop()

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }
        preparedAudioPlayer = mediaPlayer

        mediaPlayer.setOnPreparedListener { player ->
            if (!isCurrentPreparedAudioPlayer(player, generation)) {
                return@setOnPreparedListener
            }
            runCatching {
                player.playbackParams = player.playbackParams.setSpeed(request.speechRate)
            }
            player.start()
            startProgressTracking(player, chunk)
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.PLAYING,
                    currentCharOffset = chunk.startCharIndex,
                    currentChunkElapsedMs = 0L,
                    speechRate = request.speechRate,
                    offlineVoiceAvailable = true,
                    errorMessage = null,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        mediaPlayer.setOnCompletionListener { player ->
            if (!isCurrentPreparedAudioPlayer(player, generation) ||
                snapshotState.value.status != BrainBoxAudioPlaybackStatus.PLAYING
            ) {
                return@setOnCompletionListener
            }
            val duration = runCatching { player.duration.toLong() }.getOrDefault(snapshotState.value.currentChunkElapsedMs)
            stopProgressTracking()
            releasePreparedAudioPlayer()
            updateSnapshot(
                snapshotState.value.copy(
                    currentCharOffset = chunk.endCharIndex.coerceAtLeast(chunk.startCharIndex),
                    currentChunkElapsedMs = duration,
                    updatedAtEpochMs = System.currentTimeMillis()
                ),
                persist = false
            )
            speakNextChunk()
        }
        mediaPlayer.setOnErrorListener { player, _, _ ->
            if (!isCurrentPreparedAudioPlayer(player, generation)) {
                return@setOnErrorListener true
            }
            stopProgressTracking()
            releasePreparedAudioPlayer()
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ERROR,
                    errorMessage = "Offline audio playback failed.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
            true
        }

        updateSnapshot(
            snapshotState.value.copy(
                status = BrainBoxAudioPlaybackStatus.LOADING,
                currentChunkElapsedMs = 0L,
                errorMessage = null,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        runCatching {
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.prepareAsync()
        }.onFailure {
            if (isCurrentPreparedAudioPlayer(mediaPlayer, generation)) {
                releasePreparedAudioPlayer()
                updateSnapshot(
                    snapshotState.value.copy(
                        status = BrainBoxAudioPlaybackStatus.ERROR,
                        errorMessage = "Offline audio playback failed.",
                        updatedAtEpochMs = System.currentTimeMillis()
                    )
                )
                pendingAutoplay = false
            }
        }
    }

    private fun startPreparedAudioPlayback() {
        val player = preparedAudioPlayer ?: return
        val chunk = snapshotState.value.currentChunk ?: return
        runCatching {
            player.start()
            startProgressTracking(player, chunk)
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.PLAYING,
                    currentCharOffset = currentPreparedAudioCharOffset(chunk),
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }.onFailure {
            releasePreparedAudioPlayer()
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ERROR,
                    errorMessage = "Offline audio playback failed.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    private fun startProgressTracking(player: MediaPlayer, chunk: BrainBoxTtsChunk) {
        stopProgressTracking()
        playbackProgressJob = scope.launch {
            while (isActive && preparedAudioPlayer === player) {
                val duration = runCatching { player.duration }.getOrDefault(0)
                val position = runCatching { player.currentPosition }.getOrDefault(0)
                updateSnapshot(
                    snapshotState.value.copy(
                        currentCharOffset = currentPreparedAudioCharOffset(chunk, position, duration),
                        currentChunkElapsedMs = position.toLong().coerceAtLeast(0L),
                        updatedAtEpochMs = System.currentTimeMillis()
                    ),
                    persist = false
                )
                if (!runCatching { player.isPlaying }.getOrDefault(false)) {
                    break
                }
                delay(180L)
            }
        }
    }

    private fun startTtsProgressTracking(
        chunk: BrainBoxTtsChunk,
        relativeStartOffset: Int,
        speechRate: Float,
        generation: Long
    ) {
        stopProgressTracking()
        currentChunkStartedAtElapsedRealtimeMs = SystemClock.elapsedRealtime()
        lastTtsRangeUpdateElapsedRealtimeMs = 0L
        lastTtsRangeStartedAtElapsedRealtimeMs = 0L
        lastTtsRangeStartRelativeOffset = 0
        lastTtsRangeEndRelativeOffset = 0
        playbackProgressJob = scope.launch {
            while (
                isActive &&
                playbackGeneration.get() == generation &&
                preparedAudioPlayer == null &&
                snapshotState.value.status == BrainBoxAudioPlaybackStatus.PLAYING &&
                snapshotState.value.currentChunk?.id == chunk.id
            ) {
                val elapsedMs = currentChunkElapsedMs().coerceAtLeast(0L)
                val estimatedAdvance = (
                    elapsedMs.toFloat() / 1000f *
                        ESTIMATED_CHARS_PER_SECOND *
                        speechRate.coerceAtLeast(0.25f)
                    ).roundToInt()
                val estimatedRelativeOffset = (relativeStartOffset + estimatedAdvance)
                    .coerceIn(0, chunk.text.length)
                val estimatedAbsoluteOffset = (chunk.startCharIndex + estimatedRelativeOffset)
                    .coerceIn(chunk.startCharIndex, chunk.endCharIndex)

                val current = snapshotState.value
                if (current.status != BrainBoxAudioPlaybackStatus.PLAYING || current.currentChunk?.id != chunk.id) {
                    break
                }
                val now = SystemClock.elapsedRealtime()
                val hasFreshRangeCallback = lastTtsRangeUpdateElapsedRealtimeMs > 0L &&
                    now - lastTtsRangeUpdateElapsedRealtimeMs <= TTS_RANGE_FALLBACK_GRACE_MS
                val finalRangeLength = (lastTtsRangeEndRelativeOffset - lastTtsRangeStartRelativeOffset)
                    .coerceAtLeast(0)
                val isWaitingAtSentenceBoundary = isLikelySentenceBoundaryPause(
                    chunk.text,
                    lastTtsRangeEndRelativeOffset
                )
                val recoveryGraceMs = if (isWaitingAtSentenceBoundary) {
                    TTS_SENTENCE_BOUNDARY_STALL_RECOVERY_GRACE_MS
                } else {
                    TTS_SENTENCE_STALL_RECOVERY_GRACE_MS
                }
                val finalRangeShouldCompleteChunk = lastTtsRangeStartedAtElapsedRealtimeMs > 0L &&
                    isAtEndOfSpeakableText(chunk.text, lastTtsRangeEndRelativeOffset) &&
                    now - lastTtsRangeStartedAtElapsedRealtimeMs >=
                    estimatedTtsDurationMs(finalRangeLength, speechRate) + TTS_CHUNK_COMPLETION_FALLBACK_GRACE_MS
                val rangeShouldRecoverWithinChunk = lastTtsRangeStartedAtElapsedRealtimeMs > 0L &&
                    !isAtEndOfSpeakableText(chunk.text, lastTtsRangeEndRelativeOffset) &&
                    now - lastTtsRangeStartedAtElapsedRealtimeMs >=
                    estimatedTtsDurationMs(finalRangeLength, speechRate) + recoveryGraceMs
                val remainingChunkLength = (chunk.text.length - relativeStartOffset).coerceAtLeast(0)
                val estimatedChunkShouldComplete = !hasFreshRangeCallback &&
                    estimatedRelativeOffset >= chunk.text.length &&
                    elapsedMs >= estimatedTtsDurationMs(remainingChunkLength, speechRate) +
                    TTS_CHUNK_ESTIMATED_COMPLETION_GRACE_MS
                val nextCharOffset = if (hasFreshRangeCallback) {
                    current.currentCharOffset
                } else {
                    maxOf(current.currentCharOffset, estimatedAbsoluteOffset)
                }

                updateSnapshot(
                    current.copy(
                        currentCharOffset = nextCharOffset.coerceIn(chunk.startCharIndex, chunk.endCharIndex),
                        currentChunkElapsedMs = elapsedMs,
                        updatedAtEpochMs = System.currentTimeMillis()
                    ),
                    persist = false
                )
                if (finalRangeShouldCompleteChunk &&
                    completeCurrentTtsChunkIfCurrent(chunk, generation, currentChunkElapsedMs())
                ) {
                    break
                }
                if (estimatedChunkShouldComplete &&
                    completeCurrentTtsChunkIfCurrent(chunk, generation, currentChunkElapsedMs())
                ) {
                    break
                }
                if (rangeShouldRecoverWithinChunk &&
                    restartCurrentTtsChunkIfCurrent(
                        chunk = chunk,
                        generation = generation,
                        relativeOffset = lastTtsRangeEndRelativeOffset,
                        elapsedMs = currentChunkElapsedMs()
                    )
                ) {
                    break
                }
                delay(TTS_PROGRESS_UPDATE_MS)
            }
        }
    }

    private fun completeCurrentTtsChunkIfCurrent(
        chunk: BrainBoxTtsChunk,
        generation: Long,
        elapsedMs: Long
    ): Boolean {
        val current = snapshotState.value
        if (playbackGeneration.get() != generation ||
            preparedAudioPlayer != null ||
            current.status != BrainBoxAudioPlaybackStatus.PLAYING ||
            current.currentChunk?.id != chunk.id
        ) {
            return false
        }

        activeTtsUtteranceId = null
        stopProgressTracking()
        resetTtsRangeTracking()
        updateSnapshot(
            current.copy(
                currentCharOffset = chunk.endCharIndex.coerceAtLeast(chunk.startCharIndex),
                currentChunkElapsedMs = elapsedMs.coerceAtLeast(0L),
                updatedAtEpochMs = System.currentTimeMillis()
            ),
            persist = false
        )
        tts?.stop()
        scheduleSpeakNextChunk(generation)
        return true
    }

    private fun restartCurrentTtsChunkIfCurrent(
        chunk: BrainBoxTtsChunk,
        generation: Long,
        relativeOffset: Int,
        elapsedMs: Long
    ): Boolean {
        val current = snapshotState.value
        if (playbackGeneration.get() != generation ||
            preparedAudioPlayer != null ||
            current.status != BrainBoxAudioPlaybackStatus.PLAYING ||
            current.currentChunk?.id != chunk.id
        ) {
            return false
        }

        val nextRelativeOffset = nextSpeakableRelativeOffset(chunk.text, relativeOffset)
        if (nextRelativeOffset >= chunk.text.length) {
            return completeCurrentTtsChunkIfCurrent(chunk, generation, elapsedMs)
        }

        invalidateActivePlayback()
        resetTtsRangeTracking()
        tts?.stop()
        val restartAbsoluteOffset = (chunk.startCharIndex + nextRelativeOffset)
            .coerceIn(chunk.startCharIndex, chunk.endCharIndex)
        updateSnapshot(
            current.copy(
                currentCharOffset = restartAbsoluteOffset,
                currentChunkElapsedMs = 0L,
                updatedAtEpochMs = System.currentTimeMillis()
            ),
            persist = false
        )
        nextChunkJob?.cancel()
        nextChunkJob = scope.launch {
            delay(TTS_RESTART_AFTER_STOP_MS)
            val latest = snapshotState.value
            if (latest.status == BrainBoxAudioPlaybackStatus.PLAYING &&
                latest.currentChunk?.id == chunk.id &&
                latest.currentCharOffset == restartAbsoluteOffset
            ) {
                speakCurrentChunk()
            }
        }
        return true
    }

    private fun scheduleSpeakNextChunk(generation: Long) {
        nextChunkJob?.cancel()
        nextChunkJob = scope.launch {
            delay(TTS_NEXT_CHUNK_DELAY_MS)
            if (playbackGeneration.get() == generation &&
                snapshotState.value.status == BrainBoxAudioPlaybackStatus.PLAYING
            ) {
                speakNextChunk()
            }
        }
    }

    private fun stopProgressTracking() {
        playbackProgressJob?.cancel()
        playbackProgressJob = null
    }

    private fun releasePreparedAudioPlayer(stopFirst: Boolean = false) {
        stopProgressTracking()
        val player = preparedAudioPlayer ?: return
        preparedAudioPlayer = null
        player.setOnPreparedListener(null)
        player.setOnCompletionListener(null)
        player.setOnErrorListener(null)
        player.let {
            if (stopFirst) {
                runCatching { it.stop() }
            }
            runCatching { it.reset() }
            it.release()
        }
    }

    private fun resetTtsRangeTracking() {
        currentTtsRelativeStartOffset = 0
        lastTtsRangeUpdateElapsedRealtimeMs = 0L
        lastTtsRangeStartedAtElapsedRealtimeMs = 0L
        lastTtsRangeStartRelativeOffset = 0
        lastTtsRangeEndRelativeOffset = 0
    }

    private fun currentPreparedAudioCharOffset(chunk: BrainBoxTtsChunk): Int {
        val player = preparedAudioPlayer ?: return chunk.startCharIndex
        val duration = runCatching { player.duration }.getOrDefault(0)
        val position = runCatching { player.currentPosition }.getOrDefault(0)
        return currentPreparedAudioCharOffset(chunk, position, duration)
    }

    private fun currentPreparedAudioCharOffset(
        chunk: BrainBoxTtsChunk,
        positionMs: Int,
        durationMs: Int
    ): Int {
        if (durationMs <= 0 || chunk.text.isBlank()) {
            return chunk.startCharIndex
        }
        val progress = positionMs.toFloat() / durationMs.toFloat()
        val relativeOffset = (chunk.text.length * progress)
            .toInt()
            .coerceIn(0, chunk.text.length)
        return (chunk.startCharIndex + relativeOffset).coerceIn(chunk.startCharIndex, chunk.endCharIndex)
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            if (!isCurrentTtsUtterance(utteranceId)) {
                return
            }
            val request = currentRequest ?: return
            val chunk = snapshotState.value.currentChunk ?: return
            lastTtsRangeUpdateElapsedRealtimeMs = 0L
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.PLAYING,
                    currentChunkIndex = snapshotState.value.currentChunkIndex,
                    currentCharOffset = snapshotState.value.currentCharOffset
                        .coerceIn(chunk.startCharIndex, chunk.endCharIndex),
                    currentChunkElapsedMs = 0L,
                    updatedAtEpochMs = System.currentTimeMillis()
                ),
                persist = false
            )
            currentChunkStartedAtElapsedRealtimeMs = SystemClock.elapsedRealtime()
            if (request.offlineOnly && !snapshotState.value.offlineVoiceAvailable) {
                stop()
            }
        }

        override fun onDone(utteranceId: String?) {
            if (!isCurrentTtsUtterance(utteranceId)) {
                return
            }
            val request = currentRequest ?: return
            val chunk = snapshotState.value.currentChunk ?: return
            val generation = playbackGeneration.get()
            val spokenCharOffset = (chunk.startCharIndex + chunk.text.length).coerceAtLeast(chunk.startCharIndex)
            activeTtsUtteranceId = null
            stopProgressTracking()
            updateSnapshot(
                snapshotState.value.copy(
                    currentCharOffset = spokenCharOffset,
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                ),
                persist = false
            )
            scheduleSpeakNextChunk(generation)
            if (snapshotState.value.currentChunkIndex >= request.chunks.size - 1 && snapshotState.value.status == BrainBoxAudioPlaybackStatus.ENDED) {
                pendingAutoplay = false
            }
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onError(utteranceId: String?) {
            handleTtsError(utteranceId)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            handleTtsError(utteranceId)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            if (!isCurrentTtsUtterance(utteranceId)) {
                return
            }
            val current = snapshotState.value
            val elapsedMs = currentChunkElapsedMs()
            activeTtsUtteranceId = null
            stopProgressTracking()
            currentChunkStartedAtElapsedRealtimeMs = 0L
            if (current.status == BrainBoxAudioPlaybackStatus.PLAYING) {
                updateSnapshot(
                    current.copy(
                        status = BrainBoxAudioPlaybackStatus.PAUSED,
                        currentChunkElapsedMs = elapsedMs,
                        updatedAtEpochMs = System.currentTimeMillis()
                    )
                )
            }
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            if (!isCurrentTtsUtterance(utteranceId)) {
                return
            }
            val chunk = snapshotState.value.currentChunk ?: return
            val now = SystemClock.elapsedRealtime()
            lastTtsRangeUpdateElapsedRealtimeMs = now
            lastTtsRangeStartedAtElapsedRealtimeMs = now
            lastTtsRangeStartRelativeOffset = (currentTtsRelativeStartOffset + start)
                .coerceIn(0, chunk.text.length)
            lastTtsRangeEndRelativeOffset = (currentTtsRelativeStartOffset + end)
                .coerceIn(lastTtsRangeStartRelativeOffset, chunk.text.length)
            val absoluteOffset = (chunk.startCharIndex + lastTtsRangeStartRelativeOffset)
                .coerceIn(chunk.startCharIndex, chunk.endCharIndex)
            val current = snapshotState.value
            updateSnapshot(
                current.copy(
                    currentCharOffset = absoluteOffset,
                    currentChunkElapsedMs = currentChunkElapsedMs(),
                    updatedAtEpochMs = System.currentTimeMillis()
                ),
                persist = false
            )
        }

        private fun handleTtsError(utteranceId: String?) {
            if (!isCurrentTtsUtterance(utteranceId)) {
                return
            }
            activeTtsUtteranceId = null
            stopProgressTracking()
            updateSnapshot(
                snapshotState.value.copy(
                    status = BrainBoxAudioPlaybackStatus.ERROR,
                    errorMessage = "TTS playback failed.",
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
            pendingAutoplay = false
        }
    }
}
