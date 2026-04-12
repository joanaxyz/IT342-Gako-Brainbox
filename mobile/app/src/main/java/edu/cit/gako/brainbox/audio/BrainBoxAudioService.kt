package edu.cit.gako.brainbox.audio

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BrainBoxAudioService : MediaSessionService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var appGraph: BrainBoxAppGraph
    private lateinit var store: BrainBoxAudioStore
    private var playerRef: BrainBoxTtsPlayer? = null
    private var mediaSession: MediaSession? = null
    private val noisyAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                playerRef?.pause()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        appGraph = BrainBoxAppGraph.from(this)
        store = appGraph.audioStore

        val engine = BrainBoxTtsEngine(this, serviceScope) {
            playerRef?.refreshState()
        }

        val player = BrainBoxTtsPlayer(this, engine)
        playerRef = player
        mediaSession = MediaSession.Builder(this, player).build()

        serviceScope.launch {
            val restored = store.snapshotFlow.first()
            if (restored.hasLoadedRequest) {
                player.restore(restored)
            } else {
                player.refreshState()
            }
        }

        registerReceiver(
            noisyAudioReceiver,
            IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            BrainBoxAudioContract.ACTION_LOAD -> {
                intent.decodeRequest()?.let { request ->
                    playerRef?.loadRequest(request, autoplay = false)
                }
            }
            BrainBoxAudioContract.ACTION_LOAD_AND_PLAY -> {
                intent.decodeRequest()?.let { request ->
                    playerRef?.loadRequest(request, autoplay = true)
                }
            }
            BrainBoxAudioContract.ACTION_PLAY -> {
                playerRef?.play()
            }
            BrainBoxAudioContract.ACTION_PAUSE -> {
                playerRef?.pause()
            }
            BrainBoxAudioContract.ACTION_STOP -> {
                playerRef?.stop()
            }
            BrainBoxAudioContract.ACTION_SEEK_TO_CHUNK -> {
                val index = intent.getIntExtra(BrainBoxAudioContract.EXTRA_CHUNK_INDEX, 0)
                playerRef?.seekToChunk(index)
            }
            BrainBoxAudioContract.ACTION_SET_SPEECH_RATE -> {
                val rate = intent.getFloatExtra(BrainBoxAudioContract.EXTRA_SPEECH_RATE, 1.0f)
                playerRef?.setSpeechRate(rate)
            }
            BrainBoxAudioContract.ACTION_CLEAR_SESSION -> {
                playerRef?.clearSession()
                serviceScope.launch {
                    store.clear()
                }
            }
        }
        return if (playerRef?.hasPlayableRequest() == true) START_STICKY else START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (playerRef?.hasPlayableRequest() != true) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(noisyAudioReceiver) }
        mediaSession?.release()
        mediaSession = null
        playerRef?.release()
        playerRef = null
        serviceScope.coroutineContext.cancelChildren()
        super.onDestroy()
    }

    private fun Intent.decodeRequest(): BrainBoxTtsRequest? {
        return BrainBoxAudioCodec.decodeRequest(getStringExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE))
    }
}
