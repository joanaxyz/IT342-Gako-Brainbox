package edu.cit.gako.brainbox.platform.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import edu.cit.gako.brainbox.platform.local.model.OfflineEntityType
import edu.cit.gako.brainbox.platform.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.platform.local.model.PendingMutationStatus

@Entity(
    tableName = "pending_mutations",
    indices = [
        Index(value = ["entityUuid"]),
        Index(value = ["status"]),
        Index(value = ["queuedAt"])
    ]
)
data class PendingMutationEntity(
    @PrimaryKey val clientMutationId: String,
    val entityType: OfflineEntityType = OfflineEntityType.UNKNOWN,
    val entityUuid: String,
    val operation: PendingMutationOperation,
    val payloadJson: String,
    val baseVersion: Long? = null,
    val status: PendingMutationStatus = PendingMutationStatus.QUEUED,
    val queuedAt: Long,
    val lastAttemptAt: Long? = null,
    val attemptCount: Int = 0,
    val nextRetryAt: Long? = null,
    val requiresConnectivity: Boolean = true,
    val priority: Int = 0
)

