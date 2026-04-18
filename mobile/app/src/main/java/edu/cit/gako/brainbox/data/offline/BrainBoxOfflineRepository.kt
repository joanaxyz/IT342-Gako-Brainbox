package edu.cit.gako.brainbox.data.offline

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.FlashcardDeckSummary
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.network.models.OfflineNotebookBundleItem
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.network.models.QuizDetail
import edu.cit.gako.brainbox.network.models.QuizSummary
import java.util.UUID

class BrainBoxOfflineRepository(
    private val database: BrainBoxLocalDatabase,
    private val preferencesStore: BrainBoxPreferencesStore,
    private val gson: Gson = Gson()
) {
    private val notebookDao = database.notebookDao()
    private val offlinePackDao = database.offlinePackDao()
    private val pendingMutationDao = database.pendingMutationDao()
    private val conflictDraftDao = database.conflictDraftDao()
    private val quizListType = object : TypeToken<List<QuizDetail>>() {}.type
    private val flashcardListType = object : TypeToken<List<FlashcardDeckDetail>>() {}.type
    private val playlistListType = object : TypeToken<List<PlaylistSummary>>() {}.type

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

    suspend fun saveOfflineBundle(
        bundleItem: OfflineNotebookBundleItem,
        pinned: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            notebookDao.upsertNotebook(
                bundleItem.notebook.toOfflineDocument(
                    existingOffline = true,
                    syncState = NotebookSyncState.CLEAN,
                    localUpdatedAt = now
                ).toEntity()
            )
            offlinePackDao.upsertOfflinePack(
                OfflinePackEntity(
                    notebookUuid = bundleItem.notebook.uuid,
                    state = OfflinePackState.AVAILABLE,
                    downloadedAt = now,
                    lastAccessedAt = now,
                    isPinned = pinned,
                    includeQuizData = bundleItem.quizzes.isNotEmpty(),
                    includeFlashcardData = bundleItem.flashcards.isNotEmpty(),
                    includePlaylistData = bundleItem.playlists.isNotEmpty(),
                    lastServerVersion = bundleItem.notebook.version,
                    quizPayloadJson = serializeQuizDetails(bundleItem.quizzes),
                    flashcardPayloadJson = serializeFlashcardDecks(bundleItem.flashcards),
                    playlistPayloadJson = serializePlaylists(bundleItem.playlists)
                )
            )
        }
    }

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
            val existingPack = offlinePackDao.getOfflinePack(document.uuid)
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
                    lastServerVersion = document.version,
                    quizPayloadJson = existingPack?.quizPayloadJson,
                    flashcardPayloadJson = existingPack?.flashcardPayloadJson,
                    playlistPayloadJson = existingPack?.playlistPayloadJson
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

    suspend fun getOfflineQuiz(uuid: String, notebookUuidHint: String? = null): QuizDetail? {
        val candidatePacks = candidateOfflinePacks(notebookUuidHint)
        candidatePacks.forEach { pack ->
            val match = deserializeQuizDetails(pack.quizPayloadJson)
                .firstOrNull { it.uuid == uuid }
            if (match != null) {
                touchOfflinePack(pack.notebookUuid)
                return match
            }
        }
        return null
    }

    suspend fun getOfflineFlashcardDeck(uuid: String, notebookUuidHint: String? = null): FlashcardDeckDetail? {
        val candidatePacks = candidateOfflinePacks(notebookUuidHint)
        candidatePacks.forEach { pack ->
            val match = deserializeFlashcardDecks(pack.flashcardPayloadJson)
                .firstOrNull { it.uuid == uuid }
            if (match != null) {
                touchOfflinePack(pack.notebookUuid)
                return match
            }
        }
        return null
    }

    suspend fun getOfflineStudyCollections(): OfflineStudyCollections {
        val activePacks = offlinePackDao.getActiveOfflinePacks()
        return OfflineStudyCollections(
            quizzes = activePacks
                .flatMap { deserializeQuizDetails(it.quizPayloadJson) }
                .distinctBy { it.uuid }
                .map { it.toSummary() },
            flashcards = activePacks
                .flatMap { deserializeFlashcardDecks(it.flashcardPayloadJson) }
                .distinctBy { it.uuid }
                .map { it.toSummary() },
            playlists = activePacks
                .flatMap { deserializePlaylists(it.playlistPayloadJson) }
                .distinctBy { it.uuid }
        )
    }

    private suspend fun candidateOfflinePacks(notebookUuidHint: String?): List<OfflinePackEntity> {
        if (!notebookUuidHint.isNullOrBlank()) {
            return listOfNotNull(offlinePackDao.getOfflinePack(notebookUuidHint))
        }
        return offlinePackDao.getActiveOfflinePacks()
    }

    private fun serializeQuizDetails(quizzes: List<QuizDetail>): String? {
        return quizzes.takeIf { it.isNotEmpty() }?.let(gson::toJson)
    }

    private fun serializeFlashcardDecks(decks: List<FlashcardDeckDetail>): String? {
        return decks.takeIf { it.isNotEmpty() }?.let(gson::toJson)
    }

    private fun serializePlaylists(playlists: List<PlaylistSummary>): String? {
        return playlists.takeIf { it.isNotEmpty() }?.let(gson::toJson)
    }

    private fun deserializeQuizDetails(payloadJson: String?): List<QuizDetail> {
        if (payloadJson.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            gson.fromJson<List<QuizDetail>>(payloadJson, quizListType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun deserializeFlashcardDecks(payloadJson: String?): List<FlashcardDeckDetail> {
        if (payloadJson.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            gson.fromJson<List<FlashcardDeckDetail>>(payloadJson, flashcardListType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun deserializePlaylists(payloadJson: String?): List<PlaylistSummary> {
        if (payloadJson.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            gson.fromJson<List<PlaylistSummary>>(payloadJson, playlistListType).orEmpty()
        }.getOrDefault(emptyList())
    }
}

data class OfflineStudyCollections(
    val quizzes: List<QuizSummary> = emptyList(),
    val flashcards: List<FlashcardDeckSummary> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList()
)

private fun NotebookDetail.toOfflineDocument(
    existingOffline: Boolean,
    syncState: NotebookSyncState,
    localUpdatedAt: Long
): BrainBoxNotebookDocument {
    return BrainBoxNotebookDocument(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = content,
        wordCount = wordCount,
        version = version ?: 0L,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = existingOffline,
        syncState = syncState,
        localUpdatedAt = localUpdatedAt,
        remoteUpdatedAt = System.currentTimeMillis()
    )
}

private fun QuizDetail.toSummary(): QuizSummary {
    return QuizSummary(
        uuid = uuid,
        title = title,
        description = description,
        difficulty = difficulty,
        notebookUuid = notebookUuid,
        notebookTitle = notebookTitle,
        questionCount = questionCount,
        estimatedTime = estimatedTime,
        bestScore = bestScore,
        attempts = attempts,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun FlashcardDeckDetail.toSummary(): FlashcardDeckSummary {
    return FlashcardDeckSummary(
        uuid = uuid,
        title = title,
        description = description,
        notebookUuid = notebookUuid,
        notebookTitle = notebookTitle,
        cardCount = cardCount,
        bestMastery = bestMastery,
        attempts = attempts,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
