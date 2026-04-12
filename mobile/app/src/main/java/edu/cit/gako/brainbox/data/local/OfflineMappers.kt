package edu.cit.gako.brainbox.data.local

import edu.cit.gako.brainbox.data.local.entity.ConflictDraftEntity
import edu.cit.gako.brainbox.data.local.entity.NotebookEntity
import edu.cit.gako.brainbox.data.local.entity.OfflinePackEntity
import edu.cit.gako.brainbox.data.local.entity.PendingMutationEntity
import edu.cit.gako.brainbox.data.local.model.AppPlayerPreferences
import edu.cit.gako.brainbox.data.local.model.BrainBoxNotebookDocument
import edu.cit.gako.brainbox.data.local.model.ConflictDraft
import edu.cit.gako.brainbox.data.local.model.ConnectivitySnapshot
import edu.cit.gako.brainbox.data.local.model.OfflineEntityType
import edu.cit.gako.brainbox.data.local.model.OfflinePack
import edu.cit.gako.brainbox.data.local.model.PendingMutation

fun NotebookEntity.toDocument(): BrainBoxNotebookDocument {
    return BrainBoxNotebookDocument(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = contentHtml,
        wordCount = wordCount,
        version = version,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = isAvailableOffline,
        syncState = syncState,
        localUpdatedAt = localUpdatedAt,
        remoteUpdatedAt = remoteUpdatedAt
    )
}

fun BrainBoxNotebookDocument.toEntity(): NotebookEntity {
    return NotebookEntity(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = contentHtml,
        wordCount = wordCount,
        version = version,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = isAvailableOffline,
        syncState = syncState,
        localUpdatedAt = localUpdatedAt,
        remoteUpdatedAt = remoteUpdatedAt
    )
}

fun OfflinePackEntity.toModel(): OfflinePack {
    return OfflinePack(
        notebookUuid = notebookUuid,
        state = state,
        downloadedAt = downloadedAt,
        lastAccessedAt = lastAccessedAt,
        isPinned = isPinned,
        includeQuizData = includeQuizData,
        includeFlashcardData = includeFlashcardData,
        includePlaylistData = includePlaylistData,
        lastServerVersion = lastServerVersion,
        estimatedBytes = estimatedBytes
    )
}

fun OfflinePack.toEntity(): OfflinePackEntity {
    return OfflinePackEntity(
        notebookUuid = notebookUuid,
        state = state,
        downloadedAt = downloadedAt,
        lastAccessedAt = lastAccessedAt,
        isPinned = isPinned,
        includeQuizData = includeQuizData,
        includeFlashcardData = includeFlashcardData,
        includePlaylistData = includePlaylistData,
        lastServerVersion = lastServerVersion,
        estimatedBytes = estimatedBytes
    )
}

fun PendingMutationEntity.toModel(): PendingMutation {
    return PendingMutation(
        clientMutationId = clientMutationId,
        entityType = entityType,
        entityUuid = entityUuid,
        operation = operation,
        payloadJson = payloadJson,
        baseVersion = baseVersion,
        status = status,
        queuedAt = queuedAt,
        lastAttemptAt = lastAttemptAt,
        attemptCount = attemptCount,
        nextRetryAt = nextRetryAt,
        requiresConnectivity = requiresConnectivity,
        priority = priority
    )
}

fun PendingMutation.toEntity(): PendingMutationEntity {
    return PendingMutationEntity(
        clientMutationId = clientMutationId,
        entityType = entityType,
        entityUuid = entityUuid,
        operation = operation,
        payloadJson = payloadJson,
        baseVersion = baseVersion,
        status = status,
        queuedAt = queuedAt,
        lastAttemptAt = lastAttemptAt,
        attemptCount = attemptCount,
        nextRetryAt = nextRetryAt,
        requiresConnectivity = requiresConnectivity,
        priority = priority
    )
}

fun ConflictDraftEntity.toModel(): ConflictDraft {
    return ConflictDraft(
        draftId = draftId,
        notebookUuid = notebookUuid,
        baseVersion = baseVersion,
        serverVersion = serverVersion,
        localTitle = localTitle,
        serverTitle = serverTitle,
        localContentHtml = localContentHtml,
        serverContentHtml = serverContentHtml,
        reason = reason,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        resolvedAt = resolvedAt
    )
}

fun ConflictDraft.toEntity(): ConflictDraftEntity {
    return ConflictDraftEntity(
        draftId = draftId,
        notebookUuid = notebookUuid,
        baseVersion = baseVersion,
        serverVersion = serverVersion,
        localTitle = localTitle,
        serverTitle = serverTitle,
        localContentHtml = localContentHtml,
        serverContentHtml = serverContentHtml,
        reason = reason,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        resolvedAt = resolvedAt
    )
}

fun AppPlayerPreferences.copyFrom(
    offlineModeEnabled: Boolean = this.offlineModeEnabled,
    syncOnWifiOnly: Boolean = this.syncOnWifiOnly,
    preferredPlaybackSpeed: Float = this.preferredPlaybackSpeed,
    preferredPlaybackPitch: Float = this.preferredPlaybackPitch,
    preferredVoiceName: String? = this.preferredVoiceName,
    activeNotebookUuid: String? = this.activeNotebookUuid,
    queueNotebookUuid: String? = this.queueNotebookUuid,
    queueTitle: String? = this.queueTitle,
    lastSpokenOffsetMs: Long = this.lastSpokenOffsetMs,
    lastSyncAtMillis: Long? = this.lastSyncAtMillis
): AppPlayerPreferences {
    return AppPlayerPreferences(
        offlineModeEnabled = offlineModeEnabled,
        syncOnWifiOnly = syncOnWifiOnly,
        preferredPlaybackSpeed = preferredPlaybackSpeed,
        preferredPlaybackPitch = preferredPlaybackPitch,
        preferredVoiceName = preferredVoiceName,
        activeNotebookUuid = activeNotebookUuid,
        queueNotebookUuid = queueNotebookUuid,
        queueTitle = queueTitle,
        lastSpokenOffsetMs = lastSpokenOffsetMs,
        lastSyncAtMillis = lastSyncAtMillis
    )
}

fun ConnectivitySnapshot.isOffline(): Boolean = !isConnected || !isValidated

fun OfflineEntityType.isNotebookLike(): Boolean = this == OfflineEntityType.NOTEBOOK

