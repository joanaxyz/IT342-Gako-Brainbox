package edu.cit.gako.brainbox.data.offline

import android.content.Context
import edu.cit.gako.brainbox.data.connectivity.AndroidConnectivityMonitor
import edu.cit.gako.brainbox.data.connectivity.ConnectivityMonitor
import edu.cit.gako.brainbox.data.local.database.BrainBoxLocalDatabase
import edu.cit.gako.brainbox.data.persistence.BrainBoxPreferencesStore
import edu.cit.gako.brainbox.data.persistence.EncryptedSessionStore
import edu.cit.gako.brainbox.data.persistence.SessionStore

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

