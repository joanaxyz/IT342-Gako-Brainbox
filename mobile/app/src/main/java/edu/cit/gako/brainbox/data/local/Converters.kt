package edu.cit.gako.brainbox.data.local

import androidx.room.TypeConverter
import edu.cit.gako.brainbox.data.local.model.ConflictDraftStatus
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState
import edu.cit.gako.brainbox.data.local.model.OfflineEntityType
import edu.cit.gako.brainbox.data.local.model.OfflinePackState
import edu.cit.gako.brainbox.data.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.data.local.model.PendingMutationStatus

class BrainBoxConverters {
    @TypeConverter
    fun notebookSyncStateToString(value: NotebookSyncState): String = value.name

    @TypeConverter
    fun stringToNotebookSyncState(value: String): NotebookSyncState = NotebookSyncState.valueOf(value)

    @TypeConverter
    fun offlinePackStateToString(value: OfflinePackState): String = value.name

    @TypeConverter
    fun stringToOfflinePackState(value: String): OfflinePackState = OfflinePackState.valueOf(value)

    @TypeConverter
    fun entityTypeToString(value: OfflineEntityType): String = value.name

    @TypeConverter
    fun stringToEntityType(value: String): OfflineEntityType = OfflineEntityType.valueOf(value)

    @TypeConverter
    fun mutationOperationToString(value: PendingMutationOperation): String = value.name

    @TypeConverter
    fun stringToMutationOperation(value: String): PendingMutationOperation =
        PendingMutationOperation.valueOf(value)

    @TypeConverter
    fun mutationStatusToString(value: PendingMutationStatus): String = value.name

    @TypeConverter
    fun stringToMutationStatus(value: String): PendingMutationStatus =
        PendingMutationStatus.valueOf(value)

    @TypeConverter
    fun conflictStatusToString(value: ConflictDraftStatus): String = value.name

    @TypeConverter
    fun stringToConflictStatus(value: String): ConflictDraftStatus =
        ConflictDraftStatus.valueOf(value)
}

