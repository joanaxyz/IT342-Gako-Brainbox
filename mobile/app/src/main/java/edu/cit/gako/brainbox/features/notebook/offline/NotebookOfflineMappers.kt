package edu.cit.gako.brainbox.features.notebook.offline

import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.platform.local.model.BrainBoxNotebookDocument
import edu.cit.gako.brainbox.platform.local.model.NotebookSyncState

internal fun BrainBoxNotebookDocument.toNotebookDetail(): NotebookDetail {
    return NotebookDetail(
        uuid = uuid,
        title = title,
        content = contentHtml,
        wordCount = wordCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastReviewedAt = lastReviewedAt,
        version = version,
        categoryId = categoryId,
        categoryName = categoryName
    )
}

internal fun NotebookDetail.toNotebookDocument(
    existingOffline: Boolean,
    syncState: NotebookSyncState
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
        localUpdatedAt = System.currentTimeMillis(),
        remoteUpdatedAt = System.currentTimeMillis()
    )
}
