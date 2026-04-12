package edu.cit.gako.brainbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.cit.gako.brainbox.data.local.entity.PendingMutationEntity
import edu.cit.gako.brainbox.data.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.data.local.model.PendingMutationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingMutationDao {
    @Query(
        "SELECT * FROM pending_mutations WHERE status IN ('QUEUED', 'FAILED') ORDER BY priority DESC, queuedAt ASC"
    )
    fun observePendingMutations(): Flow<List<PendingMutationEntity>>

    @Query(
        "SELECT * FROM pending_mutations WHERE status = 'QUEUED' OR (status = 'FAILED' AND (nextRetryAt IS NULL OR nextRetryAt <= :now)) ORDER BY priority DESC, queuedAt ASC"
    )
    suspend fun getPendingMutationsOnce(now: Long): List<PendingMutationEntity>

    @Query(
        "UPDATE pending_mutations SET status = 'QUEUED', nextRetryAt = NULL WHERE status = 'IN_PROGRESS'"
    )
    suspend fun resetInProgressMutations()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingMutation(entity: PendingMutationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingMutations(entities: List<PendingMutationEntity>)

    @Query(
        "UPDATE pending_mutations SET status = :status, attemptCount = attemptCount + 1, lastAttemptAt = :lastAttemptAt, nextRetryAt = :nextRetryAt WHERE clientMutationId = :clientMutationId"
    )
    suspend fun markMutationAttempt(
        clientMutationId: String,
        status: PendingMutationStatus,
        lastAttemptAt: Long,
        nextRetryAt: Long?
    )

    @Query("DELETE FROM pending_mutations WHERE clientMutationId = :clientMutationId")
    suspend fun deletePendingMutation(clientMutationId: String)

    @Query(
        "SELECT * FROM pending_mutations WHERE entityUuid = :entityUuid AND operation = :operation AND status IN ('QUEUED', 'FAILED', 'IN_PROGRESS') ORDER BY queuedAt ASC LIMIT 1"
    )
    suspend fun findMutationForEntity(
        entityUuid: String,
        operation: PendingMutationOperation
    ): PendingMutationEntity?

    @Query("DELETE FROM pending_mutations")
    suspend fun clearAll()
}
