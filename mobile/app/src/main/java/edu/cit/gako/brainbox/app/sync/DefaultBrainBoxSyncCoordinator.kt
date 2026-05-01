package edu.cit.gako.brainbox.app.sync

import com.google.gson.Gson
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.notebook.data.NotebookMutationResult
import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.platform.local.database.BrainBoxLocalDatabase
import edu.cit.gako.brainbox.platform.local.entity.NotebookEntity
import edu.cit.gako.brainbox.platform.local.model.ConflictDraft
import edu.cit.gako.brainbox.platform.local.model.NotebookSyncState
import edu.cit.gako.brainbox.platform.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.platform.local.model.PendingMutationStatus
import edu.cit.gako.brainbox.platform.local.toEntity
import edu.cit.gako.brainbox.features.notebook.offline.BrainBoxOfflineRepository
import edu.cit.gako.brainbox.platform.persistence.BrainBoxPreferencesStore
import edu.cit.gako.brainbox.platform.sync.BrainBoxSyncCoordinator
import edu.cit.gako.brainbox.platform.sync.SyncBatchResult
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import java.util.UUID

internal data class NotebookMutationPayload(
    val title: String? = null,
    val content: String? = null,
    val categoryId: Long? = null
)

internal data class QuizAttemptMutationPayload(
    val score: Int
)

internal data class FlashcardAttemptMutationPayload(
    val mastery: Int
)

class DefaultBrainBoxSyncCoordinator(
    private val notebookRepository: NotebookRepository,
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository,
    private val database: BrainBoxLocalDatabase,
    private val offlineRepository: BrainBoxOfflineRepository,
    private val preferencesStore: BrainBoxPreferencesStore,
    private val gson: Gson = Gson()
) : BrainBoxSyncCoordinator {
    override suspend fun syncPendingMutations(): SyncBatchResult {
        val pendingMutationDao = database.pendingMutationDao()
        val notebookDao = database.notebookDao()
        pendingMutationDao.resetInProgressMutations()
        val mutations = pendingMutationDao.getPendingMutationsOnce(System.currentTimeMillis())

        var succeeded = 0
        var failed = 0
        var shouldRetry = false

        mutations.forEach { mutation ->
            val now = System.currentTimeMillis()
            pendingMutationDao.markMutationAttempt(
                clientMutationId = mutation.clientMutationId,
                status = PendingMutationStatus.IN_PROGRESS,
                lastAttemptAt = now,
                nextRetryAt = null
            )

            val result = runCatching {
                when (mutation.operation) {
                    PendingMutationOperation.UPDATE -> {
                        val payload = gson.fromJson(mutation.payloadJson, NotebookMutationPayload::class.java)
                        syncNotebookUpdate(mutation.entityUuid, mutation.clientMutationId, mutation.baseVersion, payload)
                    }

                    PendingMutationOperation.MARK_REVIEWED -> {
                        notebookRepository.markNotebookReviewed(
                            uuid = mutation.entityUuid,
                            baseVersion = mutation.baseVersion,
                            clientMutationId = mutation.clientMutationId
                        )
                    }

                    PendingMutationOperation.DELETE -> {
                        notebookRepository.deleteNotebook(mutation.entityUuid, mutation.baseVersion, mutation.clientMutationId)
                    }

                    PendingMutationOperation.CREATE -> {
                        syncNotebookCreate(mutation.entityUuid, mutation.payloadJson)
                    }

                    PendingMutationOperation.RECORD_QUIZ_ATTEMPT -> {
                        val payload = gson.fromJson(mutation.payloadJson, QuizAttemptMutationPayload::class.java)
                        quizRepository.recordQuizAttempt(mutation.entityUuid, payload.score)
                        NotebookMutationResult.Success(null)
                    }

                    PendingMutationOperation.RECORD_FLASHCARD_ATTEMPT -> {
                        val payload = gson.fromJson(mutation.payloadJson, FlashcardAttemptMutationPayload::class.java)
                        flashcardRepository.recordFlashcardAttempt(mutation.entityUuid, payload.mastery)
                        NotebookMutationResult.Success(null)
                    }
                }
            }.getOrElse {
                NotebookMutationResult.Failure(it.message ?: "Sync failed.")
            }

            when (result) {
                is NotebookMutationResult.Success -> {
                    val syncedNotebook = result.notebook
                    if (syncedNotebook != null && mutation.operation == PendingMutationOperation.CREATE) {
                        migrateCreatedNotebook(
                            localUuid = mutation.entityUuid,
                            syncedNotebook = syncedNotebook
                        )
                    } else if (syncedNotebook != null) {
                        val existing = notebookDao.getNotebook(mutation.entityUuid)
                        notebookDao.upsertNotebook(syncedNotebook.toLocalEntity(existing))
                    } else if (mutation.operation == PendingMutationOperation.DELETE) {
                        notebookDao.deleteNotebook(mutation.entityUuid)
                    }
                    pendingMutationDao.deletePendingMutation(mutation.clientMutationId)
                    succeeded += 1
                }

                is NotebookMutationResult.Conflict -> {
                    val latest = result.latestNotebook
                    if (latest != null) {
                        val existing = notebookDao.getNotebook(mutation.entityUuid)
                        notebookDao.upsertNotebook(
                            latest.toLocalEntity(existing).copy(syncState = NotebookSyncState.CONFLICT)
                        )
                        offlineRepository.storeConflictDraft(
                            ConflictDraft(
                                draftId = UUID.randomUUID().toString(),
                                notebookUuid = mutation.entityUuid,
                                baseVersion = mutation.baseVersion ?: 0L,
                                serverVersion = latest.version ?: 0L,
                                localTitle = gson.fromJson(mutation.payloadJson, NotebookMutationPayload::class.java).title
                                    ?: existing?.title
                                    ?: latest.title,
                                serverTitle = latest.title,
                                localContentHtml = gson.fromJson(mutation.payloadJson, NotebookMutationPayload::class.java).content
                                    ?: existing?.contentHtml
                                    ?: latest.content,
                                serverContentHtml = latest.content,
                                reason = result.message,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    }
                    pendingMutationDao.markMutationAttempt(
                        clientMutationId = mutation.clientMutationId,
                        status = PendingMutationStatus.CANCELLED,
                        lastAttemptAt = now,
                        nextRetryAt = null
                    )
                    failed += 1
                }

                is NotebookMutationResult.Failure -> {
                    val nextRetryAt = now + retryDelayForAttempt(mutation.attemptCount)
                    pendingMutationDao.markMutationAttempt(
                        clientMutationId = mutation.clientMutationId,
                        status = PendingMutationStatus.FAILED,
                        lastAttemptAt = now,
                        nextRetryAt = nextRetryAt
                    )
                    failed += 1
                    shouldRetry = true
                }
            }
        }

        preferencesStore.setLastSyncAtMillis(System.currentTimeMillis())

        return SyncBatchResult(
            processedCount = mutations.size,
            succeededCount = succeeded,
            failedCount = failed,
            shouldRetry = shouldRetry,
            message = if (failed > 0) "Some queued notebook changes still need attention." else null
        )
    }

    private suspend fun syncNotebookUpdate(
        uuid: String,
        clientMutationId: String,
        baseVersion: Long?,
        payload: NotebookMutationPayload
    ): NotebookMutationResult {
        var workingVersion = baseVersion
        if (payload.title != null || payload.categoryId != null) {
            when (
                val metadataResult = notebookRepository.updateNotebook(
                    uuid = uuid,
                    title = payload.title,
                    categoryId = payload.categoryId,
                    baseVersion = workingVersion,
                    clientMutationId = clientMutationId
                )
            ) {
                is NotebookMutationResult.Success -> {
                    workingVersion = metadataResult.notebook?.version ?: workingVersion
                }

                is NotebookMutationResult.Conflict -> return metadataResult
                is NotebookMutationResult.Failure -> return metadataResult
            }
        }

        return if (payload.content != null) {
            notebookRepository.saveNotebookContent(
                uuid = uuid,
                content = payload.content,
                baseVersion = workingVersion,
                clientMutationId = clientMutationId
            )
        } else {
            NotebookMutationResult.Success(notebookRepository.getNotebook(uuid))
        }
    }

    private suspend fun syncNotebookCreate(
        localUuid: String,
        payloadJson: String
    ): NotebookMutationResult {
        val payload = gson.fromJson(payloadJson, NotebookMutationPayload::class.java)
        val createResult = notebookRepository.createNotebook(
            title = payload.title.orEmpty().ifBlank { "Untitled notebook" },
            categoryId = payload.categoryId,
            content = payload.content.orEmpty()
        )

        return when (createResult) {
            is NotebookMutationResult.Success -> createResult
            is NotebookMutationResult.Conflict -> createResult
            is NotebookMutationResult.Failure -> {
                val existing = database.notebookDao().getNotebook(localUuid)
                if (existing == null) {
                    NotebookMutationResult.Failure(createResult.message)
                } else {
                    createResult
                }
            }
        }
    }

    private suspend fun migrateCreatedNotebook(
        localUuid: String,
        syncedNotebook: NotebookDetail
    ) {
        val notebookDao = database.notebookDao()
        val existing = notebookDao.getNotebook(localUuid)
        notebookDao.upsertNotebook(syncedNotebook.toLocalEntity(existing))
        if (localUuid != syncedNotebook.uuid) {
            notebookDao.deleteNotebook(localUuid)
        }
    }

    private fun retryDelayForAttempt(attemptCount: Int): Long {
        val multiplier = 1L.shl(attemptCount.coerceIn(0, 5))
        return 60_000L * multiplier
    }

    private fun NotebookDetail.toLocalEntity(existing: NotebookEntity?): NotebookEntity {
        return edu.cit.gako.brainbox.platform.local.model.BrainBoxNotebookDocument(
            uuid = uuid,
            title = title,
            categoryId = categoryId,
            categoryName = categoryName,
            contentHtml = content,
            wordCount = wordCount,
            version = version ?: existing?.version ?: 0L,
            lastReviewedAt = lastReviewedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isAvailableOffline = existing?.isAvailableOffline ?: false,
            syncState = NotebookSyncState.CLEAN,
            localUpdatedAt = System.currentTimeMillis(),
            remoteUpdatedAt = System.currentTimeMillis()
        ).toEntity()
    }
}
