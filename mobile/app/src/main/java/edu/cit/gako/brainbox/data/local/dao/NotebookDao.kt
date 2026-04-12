package edu.cit.gako.brainbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.cit.gako.brainbox.data.local.entity.NotebookEntity
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks WHERE uuid = :uuid LIMIT 1")
    fun observeNotebook(uuid: String): Flow<NotebookEntity?>

    @Query("SELECT * FROM notebooks WHERE uuid = :uuid LIMIT 1")
    suspend fun getNotebook(uuid: String): NotebookEntity?

    @Query("SELECT * FROM notebooks WHERE isAvailableOffline = 1 ORDER BY localUpdatedAt DESC")
    fun observeAvailableOfflineNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE isAvailableOffline = 1 ORDER BY localUpdatedAt DESC")
    suspend fun getAvailableOfflineNotebooksOnce(): List<NotebookEntity>

    @Query("SELECT * FROM notebooks ORDER BY updatedAt DESC, title ASC")
    fun observeAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks ORDER BY localUpdatedAt DESC, title ASC")
    suspend fun getAllNotebooksOnce(): List<NotebookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotebook(entity: NotebookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotebooks(entities: List<NotebookEntity>)

    @Query(
        "UPDATE notebooks SET isAvailableOffline = :isAvailableOffline, syncState = :syncState, localUpdatedAt = :updatedAt WHERE uuid = :uuid"
    )
    suspend fun updateOfflineState(
        uuid: String,
        isAvailableOffline: Boolean,
        syncState: NotebookSyncState,
        updatedAt: Long
    )

    @Query(
        "UPDATE notebooks SET syncState = :syncState, localUpdatedAt = :updatedAt WHERE uuid = :uuid"
    )
    suspend fun updateSyncState(
        uuid: String,
        syncState: NotebookSyncState,
        updatedAt: Long
    )

    @Query("DELETE FROM notebooks WHERE uuid = :uuid")
    suspend fun deleteNotebook(uuid: String)

    @Query("DELETE FROM notebooks")
    suspend fun clearAll()
}
