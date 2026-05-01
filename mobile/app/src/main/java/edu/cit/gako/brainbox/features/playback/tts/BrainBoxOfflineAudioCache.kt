package edu.cit.gako.brainbox.features.playback.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class OfflineAudioPreparationResult(
    val notebookId: String,
    val chunkCount: Int,
    val audioDirectory: File
)

class BrainBoxOfflineAudioCache(context: Context) {
    private val appContext = context.applicationContext
    private val rootDirectory = File(appContext.filesDir, "offline-audio").apply { mkdirs() }

    fun hasPreparedAudio(request: BrainBoxTtsRequest): Boolean {
        if (request.chunks.isEmpty()) {
            return false
        }
        return request.chunks.all { chunk ->
            chunkFile(request.notebookId, chunk.id).let { file ->
                file.isFile && file.length() > 0L
            }
        }
    }

    fun hasChunkAudio(notebookId: String, chunkId: String): Boolean {
        return chunkFile(notebookId, chunkId).let { file ->
            file.isFile && file.length() > 0L
        }
    }

    fun chunkFile(notebookId: String, chunkId: String): File {
        return File(notebookDirectory(notebookId), "${safeFileName(chunkId)}.wav")
    }

    fun deleteNotebookAudio(notebookId: String) {
        notebookDirectory(notebookId).deleteRecursively()
    }

    suspend fun prepare(request: BrainBoxTtsRequest): Result<OfflineAudioPreparationResult> {
        return runCatching {
            prepareOrThrow(request)
        }.onFailure {
            deleteNotebookAudio(request.notebookId)
        }
    }

    private suspend fun prepareOrThrow(request: BrainBoxTtsRequest): OfflineAudioPreparationResult {
        val notebookDirectory = notebookDirectory(request.notebookId)
        withContext(Dispatchers.IO) {
            notebookDirectory.deleteRecursively()
            notebookDirectory.mkdirs()
        }

        if (request.chunks.isEmpty()) {
            return OfflineAudioPreparationResult(
                notebookId = request.notebookId,
                chunkCount = 0,
                audioDirectory = notebookDirectory
            )
        }

        val synthesizer = OfflineAudioSynthesizer(appContext)
        try {
            request.chunks.forEachIndexed { index, chunk ->
                synthesizer.synthesizeChunk(
                    text = chunk.text,
                    request = request,
                    outputFile = chunkFile(request.notebookId, chunk.id),
                    utteranceId = "${request.notebookId}:offline:${index}"
                )
            }
        } finally {
            synthesizer.shutdown()
        }

        return OfflineAudioPreparationResult(
            notebookId = request.notebookId,
            chunkCount = request.chunks.size,
            audioDirectory = notebookDirectory
        )
    }

    private fun notebookDirectory(notebookId: String): File {
        return File(rootDirectory, safeFileName(notebookId))
    }

    private fun safeFileName(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}

private class OfflineAudioSynthesizer(
    private val appContext: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private var pendingUtteranceId: String? = null
    private var pendingResult: CompletableDeferred<Unit>? = null

    suspend fun synthesizeChunk(
        text: String,
        request: BrainBoxTtsRequest,
        outputFile: File,
        utteranceId: String
    ) {
        val tts = ensureTextToSpeech()
        withContext(Dispatchers.IO) {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }

        pendingUtteranceId = utteranceId
        pendingResult = CompletableDeferred()

        withContext(Dispatchers.Main.immediate) {
            configureVoice(tts, request)
            tts.setSpeechRate(request.speechRate)
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            val status = tts.synthesizeToFile(text, params, outputFile, utteranceId)
            if (status != TextToSpeech.SUCCESS) {
                pendingResult?.completeExceptionally(
                    IllegalStateException("TextToSpeech could not prepare offline audio.")
                )
            }
        }

        pendingResult?.await()
        pendingUtteranceId = null
        pendingResult = null

        withContext(Dispatchers.IO) {
            if (!outputFile.exists() || outputFile.length() <= 0L) {
                throw IllegalStateException("Offline audio file was not created.")
            }
        }
    }

    suspend fun shutdown() {
        withContext(Dispatchers.Main.immediate) {
            pendingUtteranceId = null
            pendingResult = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        }
    }

    private suspend fun ensureTextToSpeech(): TextToSpeech {
        textToSpeech?.let { return it }

        return withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val holder = arrayOfNulls<TextToSpeech>(1)
                holder[0] = TextToSpeech(appContext) { status ->
                    val tts = holder[0]
                    if (status == TextToSpeech.SUCCESS && tts != null) {
                        tts.setOnUtteranceProgressListener(progressListener)
                        textToSpeech = tts
                        continuation.resume(tts)
                    } else {
                        tts?.shutdown()
                        continuation.resumeWithException(
                            IllegalStateException("TextToSpeech failed to initialize.")
                        )
                    }
                }

                continuation.invokeOnCancellation {
                    holder[0]?.shutdown()
                }
            }
        }
    }

    private fun configureVoice(
        tts: TextToSpeech,
        request: BrainBoxTtsRequest
    ) {
        val locale = request.languageTag
            ?.takeIf { it.isNotBlank() }
            ?.let(Locale::forLanguageTag)
            ?: Locale.getDefault()

        val requestedVoice = request.voiceName?.let { voiceName ->
            tts.voices?.firstOrNull { it.name == voiceName }
        }
        val localeVoice = tts.voices?.firstOrNull { voice ->
            voice.locale.language == locale.language
        }

        val targetVoice = requestedVoice ?: localeVoice
        if (targetVoice != null) {
            tts.voice = targetVoice
            tts.language = targetVoice.locale
        } else {
            tts.language = locale
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) {
            if (utteranceId == pendingUtteranceId) {
                pendingResult?.complete(Unit)
            }
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onError(utteranceId: String?) {
            if (utteranceId == pendingUtteranceId) {
                pendingResult?.completeExceptionally(
                    IllegalStateException("TextToSpeech failed while preparing offline audio.")
                )
            }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            if (utteranceId == pendingUtteranceId) {
                pendingResult?.completeExceptionally(
                    IllegalStateException("TextToSpeech failed while preparing offline audio.")
                )
            }
        }
    }
}
