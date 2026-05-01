package edu.cit.gako.brainbox.features.playback.audio

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.brainBoxAudioDataStore by preferencesDataStore(name = "brainbox_audio_playback")

class BrainBoxAudioStore(context: Context) {
    private val dataStore = context.applicationContext.brainBoxAudioDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val liveSnapshot = MutableStateFlow(BrainBoxAudioSnapshot())

    val snapshotFlow: StateFlow<BrainBoxAudioSnapshot> = liveSnapshot

    init {
        scope.launch {
            val restored = restoreSnapshot()
            if (restored.updatedAtEpochMs >= liveSnapshot.value.updatedAtEpochMs) {
                liveSnapshot.value = restored
            }
        }
    }

    suspend fun restoreSnapshot(): BrainBoxAudioSnapshot {
        return decodeSnapshot(dataStore.data.first())
    }

    fun currentSnapshot(): BrainBoxAudioSnapshot = liveSnapshot.value

    fun publishSnapshot(snapshot: BrainBoxAudioSnapshot, persist: Boolean = false) {
        if (snapshot.updatedAtEpochMs >= liveSnapshot.value.updatedAtEpochMs) {
            liveSnapshot.value = snapshot
        }

        if (persist) {
            scope.launch {
                persistSnapshot(snapshot)
            }
        }
    }

    fun clear() {
        liveSnapshot.value = BrainBoxAudioSnapshot(updatedAtEpochMs = System.currentTimeMillis())
        scope.launch {
            dataStore.edit { it.clear() }
        }
    }

    private suspend fun persistSnapshot(snapshot: BrainBoxAudioSnapshot) {
        dataStore.edit { preferences ->
            snapshot.request?.let { request ->
                preferences[Keys.REQUEST_WIRE] = BrainBoxAudioCodec.encodeRequest(request)
            } ?: run {
                preferences.remove(Keys.REQUEST_WIRE)
            }
            preferences[Keys.STATUS] = snapshot.status.name
            preferences[Keys.CURRENT_CHUNK_INDEX] = snapshot.currentChunkIndex
            preferences[Keys.CURRENT_CHAR_OFFSET] = snapshot.currentCharOffset
            preferences[Keys.CURRENT_CHUNK_ELAPSED_MS] = snapshot.currentChunkElapsedMs
            preferences[Keys.SPEECH_RATE] = snapshot.speechRate
            preferences[Keys.OFFLINE_VOICE_AVAILABLE] = snapshot.offlineVoiceAvailable
            snapshot.errorMessage?.let { preferences[Keys.ERROR_MESSAGE] = it } ?: run {
                preferences.remove(Keys.ERROR_MESSAGE)
            }
            preferences[Keys.UPDATED_AT_EPOCH_MS] = snapshot.updatedAtEpochMs
        }
    }

    private fun decodeSnapshot(preferences: Preferences): BrainBoxAudioSnapshot {
        return BrainBoxAudioSnapshot(
            request = BrainBoxAudioCodec.decodeRequest(preferences[Keys.REQUEST_WIRE]),
            status = preferences[Keys.STATUS]?.let { statusName ->
                runCatching { BrainBoxAudioPlaybackStatus.valueOf(statusName) }
                    .getOrDefault(BrainBoxAudioPlaybackStatus.IDLE)
            } ?: BrainBoxAudioPlaybackStatus.IDLE,
            currentChunkIndex = preferences[Keys.CURRENT_CHUNK_INDEX] ?: 0,
            currentCharOffset = preferences[Keys.CURRENT_CHAR_OFFSET] ?: 0,
            currentChunkElapsedMs = preferences[Keys.CURRENT_CHUNK_ELAPSED_MS] ?: 0L,
            speechRate = preferences[Keys.SPEECH_RATE] ?: 1.0f,
            offlineVoiceAvailable = preferences[Keys.OFFLINE_VOICE_AVAILABLE] ?: true,
            errorMessage = preferences[Keys.ERROR_MESSAGE],
            updatedAtEpochMs = preferences[Keys.UPDATED_AT_EPOCH_MS] ?: 0L
        )
    }

    private object Keys {
        val REQUEST_WIRE = stringPreferencesKey("request_wire")
        val STATUS = stringPreferencesKey("status")
        val CURRENT_CHUNK_INDEX = intPreferencesKey("current_chunk_index")
        val CURRENT_CHAR_OFFSET = intPreferencesKey("current_char_offset")
        val CURRENT_CHUNK_ELAPSED_MS = longPreferencesKey("current_chunk_elapsed_ms")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val OFFLINE_VOICE_AVAILABLE = booleanPreferencesKey("offline_voice_available")
        val ERROR_MESSAGE = stringPreferencesKey("error_message")
        val UPDATED_AT_EPOCH_MS = longPreferencesKey("updated_at_epoch_ms")
    }
}
