package edu.cit.gako.brainbox.app

data class OfflineSyncState(
    val pendingMutationCount: Int = 0,
    val hasPendingMutations: Boolean = false,
    val lastSyncAtMillis: Long? = null,
    val lastSyncLabel: String? = null
)
