package edu.cit.gako.brainbox.data.sync

import java.util.concurrent.atomic.AtomicReference

data class SyncBatchResult(
    val processedCount: Int = 0,
    val succeededCount: Int = 0,
    val failedCount: Int = 0,
    val shouldRetry: Boolean = false,
    val message: String? = null
)

interface BrainBoxSyncCoordinator {
    suspend fun syncPendingMutations(): SyncBatchResult
}

object NoOpBrainBoxSyncCoordinator : BrainBoxSyncCoordinator {
    override suspend fun syncPendingMutations(): SyncBatchResult = SyncBatchResult()
}

object BrainBoxSyncCoordinatorRegistry {
    private val coordinator = AtomicReference<BrainBoxSyncCoordinator>(NoOpBrainBoxSyncCoordinator)

    fun install(value: BrainBoxSyncCoordinator) {
        coordinator.set(value)
    }

    fun current(): BrainBoxSyncCoordinator = coordinator.get()
}

