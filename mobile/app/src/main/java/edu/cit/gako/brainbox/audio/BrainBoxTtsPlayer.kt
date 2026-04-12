package edu.cit.gako.brainbox.audio

import android.content.Context
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class BrainBoxTtsPlayer(
    context: Context,
    private val engine: BrainBoxTtsEngine
) : SimpleBasePlayer(Looper.getMainLooper()) {

    private val appContext = context.applicationContext

    fun refreshState() {
        invalidateState()
    }

    fun hasPlayableRequest(): Boolean = engine.hasPlayableRequest()

    fun loadRequest(request: BrainBoxTtsRequest, autoplay: Boolean = false) {
        engine.load(request, autoplay)
        invalidateState()
    }

    fun restore(snapshot: BrainBoxAudioSnapshot) {
        engine.restore(snapshot)
        invalidateState()
    }

    fun clearSession() {
        engine.stop(resetToStart = true)
        invalidateState()
    }

    fun setSpeechRate(rate: Float) {
        engine.setSpeechRate(rate)
        invalidateState()
    }

    fun seekToChunk(chunkIndex: Int) {
        engine.seekToChunk(chunkIndex)
        invalidateState()
    }

    override fun getState(): State {
        val snapshot = engine.currentSnapshot()
        val request = snapshot.request
        val stateBuilder = State.Builder()
            .setPlaybackState(snapshot.status.toPlayerState())
            .setPlayWhenReady(
                snapshot.status == BrainBoxAudioPlaybackStatus.PLAYING,
                Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
            )
            .setCurrentMediaItemIndex(
                if (snapshot.hasLoadedRequest) {
                    snapshot.currentChunkIndex.coerceAtLeast(0)
                } else {
                    C.INDEX_UNSET
                }
            )
            .setContentPositionMs(snapshot.currentChunkElapsedMs.coerceAtLeast(0L))
            .setContentBufferedPositionMs { snapshot.currentChunkElapsedMs.coerceAtLeast(0L) }
            .setAvailableCommands(buildAvailableCommands())

        if (request != null && request.chunks.isNotEmpty()) {
            stateBuilder
                .setPlaylist(buildPlaylist(request))
                .setPlaylistMetadata(
                    MediaMetadata.Builder()
                        .setTitle(request.notebookTitle)
                        .setArtist("BrainBox")
                        .build()
                )
        }

        return stateBuilder.build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<Any> {
        if (playWhenReady) {
            engine.play()
        } else {
            engine.pause()
        }
        invalidateState()
        return Futures.immediateFuture(Any())
    }

    override fun handleStop(): ListenableFuture<Any> {
        engine.stop(resetToStart = true)
        invalidateState()
        return Futures.immediateFuture(Any())
    }

    override fun handleRelease(): ListenableFuture<*> {
        engine.release()
        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<Any> {
        when (seekCommand) {
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> engine.seekToChunk((engine.currentSnapshot().currentChunkIndex + 1))
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> engine.seekToChunk((engine.currentSnapshot().currentChunkIndex - 1))
            else -> {
                if (mediaItemIndex != C.INDEX_UNSET) {
                    engine.seekToChunk(mediaItemIndex)
                } else if (positionMs <= 0L) {
                    engine.seekToChunk(engine.currentSnapshot().currentChunkIndex)
                }
            }
        }
        invalidateState()
        return Futures.immediateFuture(Any())
    }

    private fun buildAvailableCommands(): Player.Commands {
        return Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_STOP,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_MEDIA_ITEM,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_GET_TIMELINE,
                Player.COMMAND_GET_METADATA,
                Player.COMMAND_RELEASE
            )
            .build()
    }

    private fun buildPlaylist(request: BrainBoxTtsRequest): List<SimpleBasePlayer.MediaItemData> {
        return request.chunks.mapIndexed { index, chunk ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(chunk.id)
                .setMediaMetadata(
                MediaMetadata.Builder()
                        .setTitle("Chunk ${index + 1}")
                        .setArtist(request.notebookTitle)
                        .build()
                )
                .build()

            SimpleBasePlayer.MediaItemData.Builder(chunk.id)
                .setMediaItem(mediaItem)
                .build()
        }
    }
}
