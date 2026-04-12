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
        startService(BrainBoxAudioContract.ACTION_LOAD) {
            putExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE, BrainBoxAudioCodec.encodeRequest(request))
        }
    }

    override fun play(request: BrainBoxTtsRequest) {
        startService(BrainBoxAudioContract.ACTION_LOAD_AND_PLAY) {
            putExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE, BrainBoxAudioCodec.encodeRequest(request))
        }
    }

    override fun resume() {
        startService(BrainBoxAudioContract.ACTION_PLAY)
    }

    override fun pause() {
        startService(BrainBoxAudioContract.ACTION_PAUSE)
    }

    override fun stop() {
        startService(BrainBoxAudioContract.ACTION_STOP)
    }

    override fun seekToChunk(chunkIndex: Int) {
        startService(BrainBoxAudioContract.ACTION_SEEK_TO_CHUNK) {
            putExtra(BrainBoxAudioContract.EXTRA_CHUNK_INDEX, chunkIndex)
        }
    }

    override fun setSpeechRate(rate: Float) {
        startService(BrainBoxAudioContract.ACTION_SET_SPEECH_RATE) {
            putExtra(BrainBoxAudioContract.EXTRA_SPEECH_RATE, rate)
        }
    }

    override fun clearSession() {
        startService(BrainBoxAudioContract.ACTION_CLEAR_SESSION)
    }

    private fun startService(action: String, extras: Intent.() -> Unit = {}) {
        val intent = Intent(appContext, BrainBoxAudioService::class.java).apply {
            this.action = action
            extras()
        }
        ContextCompat.startForegroundService(appContext, intent)
    }
}
