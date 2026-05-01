package edu.cit.gako.brainbox.app.infrastructure

import android.content.Context
import edu.cit.gako.brainbox.features.notebook.offline.BrainBoxOfflineRepository
import edu.cit.gako.brainbox.platform.connectivity.AndroidConnectivityMonitor
import edu.cit.gako.brainbox.platform.connectivity.ConnectivityMonitor
import edu.cit.gako.brainbox.platform.local.database.BrainBoxLocalDatabase
import edu.cit.gako.brainbox.platform.persistence.BrainBoxPreferencesStore
import edu.cit.gako.brainbox.platform.persistence.EncryptedSessionStore
import edu.cit.gako.brainbox.platform.persistence.SessionStore

data class BrainBoxLocalInfrastructure(
    val database: BrainBoxLocalDatabase,
    val offlineRepository: BrainBoxOfflineRepository,
    val sessionStore: SessionStore,
    val preferencesStore: BrainBoxPreferencesStore,
    val connectivityMonitor: ConnectivityMonitor
)

object BrainBoxLocalInfrastructureFactory {
    fun create(context: Context): BrainBoxLocalInfrastructure {
        val appContext = context.applicationContext
        val database = BrainBoxLocalDatabase.getInstance(appContext)
        val preferencesStore = BrainBoxPreferencesStore(appContext)
        return BrainBoxLocalInfrastructure(
            database = database,
            offlineRepository = BrainBoxOfflineRepository(database, preferencesStore),
            sessionStore = EncryptedSessionStore(appContext),
            preferencesStore = preferencesStore,
            connectivityMonitor = AndroidConnectivityMonitor(appContext)
        )
    }
}

