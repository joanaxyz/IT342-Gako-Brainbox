package edu.cit.gako.brainbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import edu.cit.gako.brainbox.data.local.model.OfflinePackState

@Entity(
    tableName = "offline_packs",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["notebookUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["notebookUuid"], unique = true),
        Index(value = ["state"])
    ]
)
data class OfflinePackEntity(
    @PrimaryKey val notebookUuid: String,
    val state: OfflinePackState = OfflinePackState.QUEUED,
    val downloadedAt: Long? = null,
    val lastAccessedAt: Long? = null,
    val isPinned: Boolean = false,
    val includeQuizData: Boolean = true,
    val includeFlashcardData: Boolean = true,
    val includePlaylistData: Boolean = true,
    val lastServerVersion: Long? = null,
    val estimatedBytes: Long? = null,
    val quizPayloadJson: String? = null,
    val flashcardPayloadJson: String? = null,
    val playlistPayloadJson: String? = null
)

