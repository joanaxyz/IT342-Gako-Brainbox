package edu.cit.gako.brainbox.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey val uuid: String,
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

