package edu.cit.gako.brainbox.data.local.model

data class BrainBoxNotebookDocument(
    val uuid: String,
    val title: String,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val contentHtml: String = "",
    val wordCount: Int? = null,
    val version: Long = 0L,
    val lastReviewedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isAvailableOffline: Boolean = false,
    val syncState: NotebookSyncState = NotebookSyncState.CLEAN,
    val localUpdatedAt: Long = 0L,
    val remoteUpdatedAt: Long? = null
)

enum class NotebookSyncState {
    CLEAN,
    DIRTY,
    SYNCING,
    CONFLICT,
    REMOVED
}

enum class OfflinePackState {
    QUEUED,
    DOWNLOADING,
    AVAILABLE,
    STALE,
    CONFLICT,
    REMOVED
}

data class OfflinePack(
    val notebookUuid: String,
    val state: OfflinePackState = OfflinePackState.QUEUED,
    val downloadedAt: Long? = null,
    val lastAccessedAt: Long? = null,
    val isPinned: Boolean = false,
    val includeQuizData: Boolean = true,
    val includeFlashcardData: Boolean = true,
    val includePlaylistData: Boolean = true,
    val lastServerVersion: Long? = null,
    val estimatedBytes: Long? = null
)

enum class OfflineEntityType {
    NOTEBOOK,
    QUIZ,
    FLASHCARD_DECK,
    PLAYLIST,
    UNKNOWN
}

enum class PendingMutationOperation {
    CREATE,
    UPDATE,
    DELETE,
    MARK_REVIEWED,
    RECORD_QUIZ_ATTEMPT,
    RECORD_FLASHCARD_ATTEMPT
}

enum class PendingMutationStatus {
    QUEUED,
    IN_PROGRESS,
    FAILED,
    SUCCEEDED,
    CANCELLED
}

data class PendingMutation(
    val clientMutationId: String,
    val entityType: OfflineEntityType,
    val entityUuid: String,
    val operation: PendingMutationOperation,
    val payloadJson: String,
    val baseVersion: Long? = null,
    val status: PendingMutationStatus = PendingMutationStatus.QUEUED,
    val queuedAt: Long,
    val lastAttemptAt: Long? = null,
    val attemptCount: Int = 0,
    val nextRetryAt: Long? = null,
    val requiresConnectivity: Boolean = true,
    val priority: Int = 0
)

enum class ConflictDraftStatus {
    OPEN,
    RESOLVED,
    DISCARDED
}

data class ConflictDraft(
    val draftId: String,
    val notebookUuid: String,
    val baseVersion: Long,
    val serverVersion: Long,
    val localTitle: String,
    val serverTitle: String,
    val localContentHtml: String,
    val serverContentHtml: String,
    val reason: String,
    val status: ConflictDraftStatus = ConflictDraftStatus.OPEN,
    val createdAt: Long,
    val updatedAt: Long,
    val resolvedAt: Long? = null
)

data class AppPlayerPreferences(
    val offlineModeEnabled: Boolean = false,
    val syncOnWifiOnly: Boolean = false,
    val preferredPlaybackSpeed: Float = 1.0f,
    val preferredPlaybackPitch: Float = 1.0f,
    val preferredVoiceName: String? = null,
    val activeNotebookUuid: String? = null,
    val queueNotebookUuid: String? = null,
    val queueTitle: String? = null,
    val lastSpokenOffsetMs: Long = 0L,
    val lastSyncAtMillis: Long? = null
)

data class ConnectivitySnapshot(
    val isConnected: Boolean,
    val isValidated: Boolean,
    val isMetered: Boolean,
    val transportLabel: String? = null
)

