package edu.cit.gako.brainbox.features.playback.audio

import android.content.Context
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BrainBoxAudioResumePoint(
    val notebookId: String,
    val currentChunkIndex: Int,
    val currentCharOffset: Int,
    val currentChunkElapsedMs: Long,
    val speechRate: Float,
    val updatedAtEpochMs: Long
)

class BrainBoxAudioStore(context: Context) {
    private val appContext = context.applicationContext
    private val sharedPreferences = appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val liveSnapshot = MutableStateFlow(BrainBoxAudioSnapshot())

    val snapshotFlow: StateFlow<BrainBoxAudioSnapshot> = liveSnapshot

    fun currentSnapshot(): BrainBoxAudioSnapshot = liveSnapshot.value

    fun resumePointFor(notebookId: String): BrainBoxAudioResumePoint? {
        val storedNotebookId = sharedPreferences.getString(KEY_NOTEBOOK_ID, null) ?: return null
        if (storedNotebookId != notebookId) {
            return null
        }

        val updatedAt = sharedPreferences.getLong(KEY_UPDATED_AT_EPOCH_MS, 0L)
        if (updatedAt <= 0L) {
            return null
        }

        return BrainBoxAudioResumePoint(
            notebookId = storedNotebookId,
            currentChunkIndex = sharedPreferences.getInt(KEY_CURRENT_CHUNK_INDEX, 0),
            currentCharOffset = sharedPreferences.getInt(KEY_CURRENT_CHAR_OFFSET, 0),
            currentChunkElapsedMs = sharedPreferences.getLong(KEY_CURRENT_CHUNK_ELAPSED_MS, 0L),
            speechRate = sharedPreferences.getFloat(KEY_SPEECH_RATE, 1.0f),
            updatedAtEpochMs = updatedAt
        )
    }

    fun publishSnapshot(snapshot: BrainBoxAudioSnapshot) {
        if (snapshot.updatedAtEpochMs >= liveSnapshot.value.updatedAtEpochMs) {
            liveSnapshot.value = snapshot
        }
        persistResumePoint(snapshot)
    }

    fun clear() {
        liveSnapshot.value = BrainBoxAudioSnapshot(updatedAtEpochMs = System.currentTimeMillis())
        sharedPreferences.edit().clear().apply()
    }

    private fun persistResumePoint(snapshot: BrainBoxAudioSnapshot) {
        val request = snapshot.request
        if (!snapshot.hasLoadedRequest || request == null) {
            return
        }

        when (snapshot.status) {
            BrainBoxAudioPlaybackStatus.PAUSED,
            BrainBoxAudioPlaybackStatus.READY -> {
                sharedPreferences.edit()
                    .putString(KEY_NOTEBOOK_ID, request.notebookId)
                    .putInt(KEY_CURRENT_CHUNK_INDEX, snapshot.currentChunkIndex)
                    .putInt(KEY_CURRENT_CHAR_OFFSET, snapshot.currentCharOffset)
                    .putLong(KEY_CURRENT_CHUNK_ELAPSED_MS, snapshot.currentChunkElapsedMs)
                    .putFloat(KEY_SPEECH_RATE, snapshot.speechRate)
                    .putLong(KEY_UPDATED_AT_EPOCH_MS, snapshot.updatedAtEpochMs)
                    .apply()
            }
            BrainBoxAudioPlaybackStatus.ENDED,
            BrainBoxAudioPlaybackStatus.IDLE -> {
                sharedPreferences.edit().clear().apply()
            }
            else -> Unit
        }
    }

    private companion object {
        const val FILE_NAME = "brainbox_audio_resume"
        const val KEY_NOTEBOOK_ID = "notebook_id"
        const val KEY_CURRENT_CHUNK_INDEX = "current_chunk_index"
        const val KEY_CURRENT_CHAR_OFFSET = "current_char_offset"
        const val KEY_CURRENT_CHUNK_ELAPSED_MS = "current_chunk_elapsed_ms"
        const val KEY_SPEECH_RATE = "speech_rate"
        const val KEY_UPDATED_AT_EPOCH_MS = "updated_at_epoch_ms"
    }
}
