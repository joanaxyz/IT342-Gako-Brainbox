package edu.cit.gako.brainbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.cit.gako.brainbox.data.local.entity.OfflinePackEntity
import edu.cit.gako.brainbox.data.local.model.OfflinePackState
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePackDao {
    @Query("SELECT * FROM offline_packs WHERE notebookUuid = :notebookUuid LIMIT 1")
    fun observeOfflinePack(notebookUuid: String): Flow<OfflinePackEntity?>

    @Query("SELECT * FROM offline_packs WHERE notebookUuid = :notebookUuid LIMIT 1")
    suspend fun getOfflinePack(notebookUuid: String): OfflinePackEntity?

    @Query("SELECT * FROM offline_packs WHERE state IN ('QUEUED', 'DOWNLOADING', 'AVAILABLE', 'STALE', 'CONFLICT') ORDER BY lastAccessedAt DESC, downloadedAt DESC")
    fun observeActiveOfflinePacks(): Flow<List<OfflinePackEntity>>

    @Query("SELECT * FROM offline_packs WHERE state IN ('QUEUED', 'DOWNLOADING', 'AVAILABLE', 'STALE', 'CONFLICT') ORDER BY lastAccessedAt DESC, downloadedAt DESC")
    suspend fun getActiveOfflinePacks(): List<OfflinePackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflinePack(entity: OfflinePackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflinePacks(entities: List<OfflinePackEntity>)

    @Query("UPDATE offline_packs SET state = :state, lastAccessedAt = :lastAccessedAt WHERE notebookUuid = :notebookUuid")
    suspend fun updateState(
        notebookUuid: String,
        state: OfflinePackState,
        lastAccessedAt: Long
    )

    @Query("DELETE FROM offline_packs WHERE notebookUuid = :notebookUuid")
    suspend fun deleteOfflinePack(notebookUuid: String)

    @Query("DELETE FROM offline_packs")
    suspend fun clearAll()
}

