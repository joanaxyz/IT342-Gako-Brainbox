package edu.cit.gako.brainbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.cit.gako.brainbox.data.local.entity.ConflictDraftEntity
import edu.cit.gako.brainbox.data.local.model.ConflictDraftStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictDraftDao {
    @Query("SELECT * FROM conflict_drafts WHERE notebookUuid = :notebookUuid ORDER BY updatedAt DESC LIMIT 1")
    fun observeLatestDraft(notebookUuid: String): Flow<ConflictDraftEntity?>

    @Query("SELECT * FROM conflict_drafts WHERE status = 'OPEN' ORDER BY updatedAt DESC")
    fun observeOpenDrafts(): Flow<List<ConflictDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConflictDraft(entity: ConflictDraftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConflictDrafts(entities: List<ConflictDraftEntity>)

    @Query(
        "UPDATE conflict_drafts SET status = :status, resolvedAt = :resolvedAt, updatedAt = :updatedAt WHERE draftId = :draftId"
    )
    suspend fun updateStatus(
        draftId: String,
        status: ConflictDraftStatus,
        resolvedAt: Long?,
        updatedAt: Long
    )

    @Query("DELETE FROM conflict_drafts WHERE draftId = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Query("DELETE FROM conflict_drafts")
    suspend fun clearAll()
}

