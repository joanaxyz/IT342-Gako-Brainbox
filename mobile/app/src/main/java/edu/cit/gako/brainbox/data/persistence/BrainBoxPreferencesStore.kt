package edu.cit.gako.brainbox.data.persistence

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import edu.cit.gako.brainbox.data.local.model.AppPlayerPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.brainBoxPreferencesDataStore by preferencesDataStore(name = "brainbox_app_player_prefs")

class BrainBoxPreferencesStore(context: Context) {
    private val dataStore = context.applicationContext.brainBoxPreferencesDataStore

    val preferences: Flow<AppPlayerPreferences> = dataStore.data.map { prefs ->
        prefs.toAppPlayerPreferences()
    }

    suspend fun setOfflineModeEnabled(enabled: Boolean) {
        update { it[Key.offlineModeEnabled] = enabled }
    }

    suspend fun setSyncOnWifiOnly(enabled: Boolean) {
        update { it[Key.syncOnWifiOnly] = enabled }
    }

    suspend fun setPreferredPlaybackSpeed(speed: Float) {
        update { it[Key.playbackSpeed] = speed }
    }

    suspend fun setPreferredPlaybackPitch(pitch: Float) {
        update { it[Key.playbackPitch] = pitch }
    }

    suspend fun setPreferredVoiceName(voiceName: String?) {
        update { mutablePrefs ->
            if (voiceName.isNullOrBlank()) {
                mutablePrefs.remove(Key.preferredVoiceName)
            } else {
                mutablePrefs[Key.preferredVoiceName] = voiceName
            }
        }
    }

    suspend fun setActiveNotebookUuid(uuid: String?) {
        update { mutablePrefs ->
            if (uuid.isNullOrBlank()) {
                mutablePrefs.remove(Key.activeNotebookUuid)
            } else {
                mutablePrefs[Key.activeNotebookUuid] = uuid
            }
        }
    }

    suspend fun setQueueNotebookUuid(uuid: String?) {
        update { mutablePrefs ->
            if (uuid.isNullOrBlank()) {
                mutablePrefs.remove(Key.queueNotebookUuid)
            } else {
                mutablePrefs[Key.queueNotebookUuid] = uuid
            }
        }
    }

    suspend fun setQueueTitle(title: String?) {
        update { mutablePrefs ->
            if (title.isNullOrBlank()) {
                mutablePrefs.remove(Key.queueTitle)
            } else {
                mutablePrefs[Key.queueTitle] = title
            }
        }
    }

    suspend fun setLastSpokenOffsetMs(offsetMs: Long) {
        update { it[Key.lastSpokenOffsetMs] = offsetMs }
    }

    suspend fun setLastSyncAtMillis(timestampMillis: Long?) {
        update { mutablePrefs ->
            if (timestampMillis == null) {
                mutablePrefs.remove(Key.lastSyncAtMillis)
            } else {
                mutablePrefs[Key.lastSyncAtMillis] = timestampMillis
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private suspend fun update(block: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private fun Preferences.toAppPlayerPreferences(): AppPlayerPreferences {
        return AppPlayerPreferences(
            offlineModeEnabled = this[Key.offlineModeEnabled] ?: false,
            syncOnWifiOnly = this[Key.syncOnWifiOnly] ?: false,
            preferredPlaybackSpeed = this[Key.playbackSpeed] ?: 1.0f,
            preferredPlaybackPitch = this[Key.playbackPitch] ?: 1.0f,
            preferredVoiceName = this[Key.preferredVoiceName],
            activeNotebookUuid = this[Key.activeNotebookUuid],
            queueNotebookUuid = this[Key.queueNotebookUuid],
            queueTitle = this[Key.queueTitle],
            lastSpokenOffsetMs = this[Key.lastSpokenOffsetMs] ?: 0L,
            lastSyncAtMillis = this[Key.lastSyncAtMillis]
        )
    }

    private object Key {
        val offlineModeEnabled = booleanPreferencesKey("offline_mode_enabled")
        val syncOnWifiOnly = booleanPreferencesKey("sync_on_wifi_only")
        val playbackSpeed = floatPreferencesKey("playback_speed")
        val playbackPitch = floatPreferencesKey("playback_pitch")
        val preferredVoiceName = stringPreferencesKey("preferred_voice_name")
        val activeNotebookUuid = stringPreferencesKey("active_notebook_uuid")
        val queueNotebookUuid = stringPreferencesKey("queue_notebook_uuid")
        val queueTitle = stringPreferencesKey("queue_title")
        val lastSpokenOffsetMs = longPreferencesKey("last_spoken_offset_ms")
        val lastSyncAtMillis = longPreferencesKey("last_sync_at_millis")
    }
}
