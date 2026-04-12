package edu.cit.gako.brainbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import edu.cit.gako.brainbox.data.local.model.ConflictDraftStatus

@Entity(
    tableName = "conflict_drafts",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["notebookUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["notebookUuid"]),
        Index(value = ["status"])
    ]
)
data class ConflictDraftEntity(
    @PrimaryKey val draftId: String,
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

