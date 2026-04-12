package edu.cit.gako.brainbox.data.offline

import edu.cit.gako.brainbox.data.local.database.BrainBoxLocalDatabase
import edu.cit.gako.brainbox.data.local.model.AppPlayerPreferences
import edu.cit.gako.brainbox.data.local.model.BrainBoxNotebookDocument
import edu.cit.gako.brainbox.data.local.model.ConflictDraft
import edu.cit.gako.brainbox.data.local.model.ConnectivitySnapshot
import edu.cit.gako.brainbox.data.local.model.OfflineEntityType
import edu.cit.gako.brainbox.data.local.model.OfflinePack
import edu.cit.gako.brainbox.data.local.model.OfflinePackState
import edu.cit.gako.brainbox.data.local.model.PendingMutation
import edu.cit.gako.brainbox.data.local.model.PendingMutationStatus
import edu.cit.gako.brainbox.data.local.toDocument
import edu.cit.gako.brainbox.data.local.toEntity
import edu.cit.gako.brainbox.data.local.toModel
import edu.cit.gako.brainbox.data.persistence.BrainBoxPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import edu.cit.gako.brainbox.data.local.entity.OfflinePackEntity
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState
import java.util.UUID

class BrainBoxOfflineRepository(
    private val database: BrainBoxLocalDatabase,
    private val preferencesStore: BrainBoxPreferencesStore
) {
    private val notebookDao = database.notebookDao()
    private val offlinePackDao = database.offlinePackDao()
    private val pendingMutationDao = database.pendingMutationDao()
    private val conflictDraftDao = database.conflictDraftDao()

    fun observeNotebook(uuid: String): Flow<BrainBoxNotebookDocument?> {
        return notebookDao.observeNotebook(uuid).map { it?.toDocument() }
    }

    fun observeAvailableOfflineNotebooks(): Flow<List<BrainBoxNotebookDocument>> {
        return notebookDao.observeAvailableOfflineNotebooks().map { entities ->
            entities.map { it.toDocument() }
        }
    }

    fun observeOfflinePack(notebookUuid: String): Flow<OfflinePack?> {
        return offlinePackDao.observeOfflinePack(notebookUuid).map { it?.toModel() }
    }

    fun observeActiveOfflinePacks(): Flow<List<OfflinePack>> {
        return offlinePackDao.observeActiveOfflinePacks().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun observePendingMutations(): Flow<List<PendingMutation>> {
        return pendingMutationDao.observePendingMutations().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun observeConflictDrafts(): Flow<List<ConflictDraft>> {
        return conflictDraftDao.observeOpenDrafts().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun observePlayerPreferences(): Flow<AppPlayerPreferences> = preferencesStore.preferences

    fun observeConnectivity(snapshot: ConnectivitySnapshot): Boolean = !snapshot.isConnected || !snapshot.isValidated

    suspend fun saveNotebookSnapshot(
        document: BrainBoxNotebookDocument,
        packState: OfflinePackState = OfflinePackState.AVAILABLE,
        pinned: Boolean = false,
        includeQuizData: Boolean = true,
        includeFlashcardData: Boolean = true,
        includePlaylistData: Boolean = true
    ) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            notebookDao.upsertNotebook(
                document.copy(
                    isAvailableOffline = packState == OfflinePackState.AVAILABLE,
                    syncState = NotebookSyncState.CLEAN,
                    localUpdatedAt = now
                ).toEntity()
            )
            offlinePackDao.upsertOfflinePack(
                OfflinePackEntity(
                    notebookUuid = document.uuid,
                    state = packState,
                    downloadedAt = now,
                    lastAccessedAt = now,
                    isPinned = pinned,
                    includeQuizData = includeQuizData,
                    includeFlashcardData = includeFlashcardData,
                    includePlaylistData = includePlaylistData,
                    lastServerVersion = document.version
                )
            )
        }
    }

    suspend fun markNotebookDirty(uuid: String) {
        notebookDao.updateSyncState(
            uuid = uuid,
            syncState = NotebookSyncState.DIRTY,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun markNotebookAvailableOffline(uuid: String, available: Boolean = true) {
        val now = System.currentTimeMillis()
        notebookDao.updateOfflineState(
            uuid = uuid,
            isAvailableOffline = available,
            syncState = if (available) NotebookSyncState.CLEAN else NotebookSyncState.REMOVED,
            updatedAt = now
        )
    }

    suspend fun touchOfflinePack(uuid: String, state: OfflinePackState = OfflinePackState.AVAILABLE) {
        offlinePackDao.updateState(
            notebookUuid = uuid,
            state = state,
            lastAccessedAt = System.currentTimeMillis()
        )
    }

    suspend fun queueMutation(mutation: PendingMutation) {
        pendingMutationDao.upsertPendingMutation(
            mutation.copy(
                status = PendingMutationStatus.QUEUED
            ).toEntity()
        )
    }

    suspend fun queueNotebookMutation(
        notebookUuid: String,
        operation: edu.cit.gako.brainbox.data.local.model.PendingMutationOperation,
        payloadJson: String,
        baseVersion: Long? = null,
        priority: Int = 0
    ) {
        queueMutation(
            PendingMutation(
                clientMutationId = UUID.randomUUID().toString(),
                entityType = OfflineEntityType.NOTEBOOK,
                entityUuid = notebookUuid,
                operation = operation,
                payloadJson = payloadJson,
                baseVersion = baseVersion,
                queuedAt = System.currentTimeMillis(),
                priority = priority
            )
        )
    }

    suspend fun storeConflictDraft(draft: ConflictDraft) {
        conflictDraftDao.upsertConflictDraft(draft.toEntity())
    }

    suspend fun removeOfflinePack(notebookUuid: String) {
        offlinePackDao.deleteOfflinePack(notebookUuid)
        notebookDao.updateOfflineState(
            uuid = notebookUuid,
            isAvailableOffline = false,
            syncState = NotebookSyncState.REMOVED,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updatePlayerPreferences(block: suspend BrainBoxPreferencesStore.() -> Unit) {
        preferencesStore.block()
    }
}
