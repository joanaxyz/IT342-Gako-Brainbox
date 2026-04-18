package edu.cit.gako.brainbox.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

interface BrainBoxAudioApi {
    fun load(request: BrainBoxTtsRequest)
    fun play(request: BrainBoxTtsRequest)
    fun resume()
    fun pause()
    fun stop()
    fun seekToChunk(chunkIndex: Int)
    fun setSpeechRate(rate: Float)
    fun clearSession()
}

class BrainBoxAudioClient(context: Context) : BrainBoxAudioApi {
    private val appContext = context.applicationContext

    override fun load(request: BrainBoxTtsRequest) {
        startForegroundService(BrainBoxAudioContract.ACTION_LOAD) {
            putExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE, BrainBoxAudioCodec.encodeRequest(request))
        }
    }

    override fun play(request: BrainBoxTtsRequest) {
        startForegroundService(BrainBoxAudioContract.ACTION_LOAD_AND_PLAY) {
            putExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE, BrainBoxAudioCodec.encodeRequest(request))
        }
    }

    override fun resume() {
        startForegroundService(BrainBoxAudioContract.ACTION_PLAY)
    }

    override fun pause() {
        sendCommand(BrainBoxAudioContract.ACTION_PAUSE)
    }

    override fun stop() {
        sendCommand(BrainBoxAudioContract.ACTION_STOP)
    }

    override fun seekToChunk(chunkIndex: Int) {
        sendCommand(BrainBoxAudioContract.ACTION_SEEK_TO_CHUNK) {
            putExtra(BrainBoxAudioContract.EXTRA_CHUNK_INDEX, chunkIndex)
        }
    }

    override fun setSpeechRate(rate: Float) {
        sendCommand(BrainBoxAudioContract.ACTION_SET_SPEECH_RATE) {
            putExtra(BrainBoxAudioContract.EXTRA_SPEECH_RATE, rate)
        }
    }

    override fun clearSession() {
        sendCommand(BrainBoxAudioContract.ACTION_CLEAR_SESSION)
    }

    private fun startForegroundService(action: String, extras: Intent.() -> Unit = {}) {
        val intent = Intent(appContext, BrainBoxAudioService::class.java).apply {
            this.action = action
            extras()
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    // For control commands that don't initiate playback — avoids ForegroundServiceDidNotStartInTimeException
    // when the service is stopped or TTS has disconnected. If the service isn't running, the
    // command is a no-op (START_NOT_STICKY), which is the correct behaviour.
    private fun sendCommand(action: String, extras: Intent.() -> Unit = {}) {
        val intent = Intent(appContext, BrainBoxAudioService::class.java).apply {
            this.action = action
            extras()
        }
        appContext.startService(intent)
    }
}
