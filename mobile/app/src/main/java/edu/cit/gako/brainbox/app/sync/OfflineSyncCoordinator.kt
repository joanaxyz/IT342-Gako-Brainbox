package edu.cit.gako.brainbox.app.sync

import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.OfflineSyncState
import edu.cit.gako.brainbox.app.infrastructure.BrainBoxLocalInfrastructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class OfflineSyncCoordinator(
    private val localInfrastructure: BrainBoxLocalInfrastructure,
    private val getState: () -> AppState,
    private val setState: (AppState) -> Unit,
    private val formatSyncLabel: (Long) -> String,
    private val refreshHome: suspend () -> Unit
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            var previousSyncState: OfflineSyncState? = null
            combine(
                localInfrastructure.offlineRepository.observePendingMutations(),
                localInfrastructure.preferencesStore.preferences
            ) { pendingMutations, preferences ->
                OfflineSyncState(
                    pendingMutationCount = pendingMutations.size,
                    hasPendingMutations = pendingMutations.isNotEmpty(),
                    lastSyncAtMillis = preferences.lastSyncAtMillis,
                    lastSyncLabel = preferences.lastSyncAtMillis?.let(formatSyncLabel)
                )
            }.collect { offlineSyncState ->
                val currentState = getState()
                val hadPendingMutations = previousSyncState?.hasPendingMutations == true
                val syncTimestampChanged =
                    previousSyncState?.lastSyncAtMillis != offlineSyncState.lastSyncAtMillis

                setState(
                    currentState.copy(
                        offlineSyncState = offlineSyncState,
                        homeData = currentState.homeData.copy(syncedAtLabel = offlineSyncState.lastSyncLabel)
                    )
                )
                previousSyncState = offlineSyncState

                val nextState = getState()
                val shouldRefreshHome =
                    nextState.isAuthenticated &&
                        syncTimestampChanged &&
                        (hadPendingMutations || offlineSyncState.hasPendingMutations)

                if (shouldRefreshHome) {
                    refreshHome()
                }
            }
        }
    }
}
