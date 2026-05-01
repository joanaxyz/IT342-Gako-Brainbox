package edu.cit.gako.brainbox.features.playback.audio

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.tts.BrainBoxTtsEngine
import edu.cit.gako.brainbox.features.playback.tts.BrainBoxTtsPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrainBoxAudioService : MediaSessionService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var appGraph: BrainBoxAppGraph
    private lateinit var store: BrainBoxAudioStore
    private lateinit var notificationHelper: BrainBoxAudioNotification
    private var playerRef: BrainBoxTtsPlayer? = null
    private var mediaSession: MediaSession? = null
    private var foregroundPlaybackSession = false
    private var isInForeground = false
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
        notificationHelper = BrainBoxAudioNotification(this)

        val engine = BrainBoxTtsEngine(this, store, serviceScope) { snapshot ->
            playerRef?.refreshState()
            syncForegroundState(snapshot)
        }

        val player = BrainBoxTtsPlayer(this, engine)
        playerRef = player
        mediaSession = MediaSession.Builder(this, player).build()

        serviceScope.launch {
            val restored = store.restoreSnapshot()
            if (restored.hasLoadedRequest) {
                foregroundPlaybackSession = restored.status in setOf(
                    BrainBoxAudioPlaybackStatus.LOADING,
                    BrainBoxAudioPlaybackStatus.READY,
                    BrainBoxAudioPlaybackStatus.PLAYING,
                    BrainBoxAudioPlaybackStatus.PAUSED
                )
                player.restore(restored)
            } else {
                player.refreshState()
            }
            syncForegroundState(player.currentSnapshot())
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
        var syncNow = true
        when (intent?.action) {
            BrainBoxAudioContract.ACTION_PREPARE -> {
                playerRef?.refreshState()
            }
            BrainBoxAudioContract.ACTION_LOAD -> {
                syncNow = false
                loadEncodedRequest(intent, autoplay = false)
            }
            BrainBoxAudioContract.ACTION_LOAD_AND_PLAY -> {
                foregroundPlaybackSession = true
                ensureForegroundForPlaybackStart()
                syncNow = false
                loadEncodedRequest(intent, autoplay = true)
            }
            BrainBoxAudioContract.ACTION_PLAY -> {
                foregroundPlaybackSession = true
                ensureForegroundForPlaybackStart()
                playerRef?.play()
            }
            BrainBoxAudioContract.ACTION_PAUSE -> {
                playerRef?.pause()
            }
            BrainBoxAudioContract.ACTION_STOP -> {
                foregroundPlaybackSession = false
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
                foregroundPlaybackSession = false
                playerRef?.clearSession()
                store.clear()
            }
        }

        if (syncNow) {
            syncForegroundState(playerRef?.currentSnapshot() ?: store.currentSnapshot())
        }
        return if (playerRef?.hasPlayableRequest() == true || foregroundPlaybackSession) {
            START_STICKY
        } else {
            START_NOT_STICKY
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (playerRef?.hasPlayableRequest() != true) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(noisyAudioReceiver) }
        if (isInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        notificationHelper.cancel()
        mediaSession?.release()
        mediaSession = null
        playerRef?.release()
        playerRef = null
        serviceScope.coroutineContext.cancelChildren()
        super.onDestroy()
    }

    private fun ensureForegroundForPlaybackStart() {
        val snapshot = playerRef?.currentSnapshot()?.let { current ->
            if (current.hasLoadedRequest) {
                current
            } else {
                current.copy(
                    status = BrainBoxAudioPlaybackStatus.LOADING,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            }
        } ?: BrainBoxAudioSnapshot(
            status = BrainBoxAudioPlaybackStatus.LOADING,
            updatedAtEpochMs = System.currentTimeMillis()
        )

        if (!isInForeground) {
            notificationHelper.startForeground(this, snapshot)
            isInForeground = true
        } else {
            notificationHelper.update(snapshot)
        }
    }

    private fun syncForegroundState(snapshot: BrainBoxAudioSnapshot) {
        val shouldStayForeground = foregroundPlaybackSession && snapshot.hasLoadedRequest
        if (shouldStayForeground) {
            if (!isInForeground) {
                notificationHelper.startForeground(this, snapshot)
                isInForeground = true
            } else {
                notificationHelper.update(snapshot)
            }
            return
        }

        if (isInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isInForeground = false
        }
        notificationHelper.cancel()
    }

    private fun loadEncodedRequest(intent: Intent, autoplay: Boolean) {
        val encoded = intent.getStringExtra(BrainBoxAudioContract.EXTRA_REQUEST_WIRE)
        serviceScope.launch {
            val request = withContext(Dispatchers.Default) {
                BrainBoxAudioCodec.decodeRequest(encoded)
            }
            request?.let { playerRef?.loadRequest(it, autoplay = autoplay) }
            syncForegroundState(playerRef?.currentSnapshot() ?: store.currentSnapshot())
        }
    }
}
