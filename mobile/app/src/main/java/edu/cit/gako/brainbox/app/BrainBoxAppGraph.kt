package edu.cit.gako.brainbox.app

import android.content.Context
import edu.cit.gako.brainbox.audio.BrainBoxAudioStore
import edu.cit.gako.brainbox.data.BrainBoxRepository
import edu.cit.gako.brainbox.data.offline.BrainBoxLocalInfrastructure
import edu.cit.gako.brainbox.data.offline.BrainBoxLocalInfrastructureFactory
import edu.cit.gako.brainbox.data.sync.BrainBoxSyncCoordinatorRegistry
import edu.cit.gako.brainbox.data.sync.DefaultBrainBoxSyncCoordinator
import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.network.SessionManager

class BrainBoxAppGraph private constructor(context: Context) {
    private val appContext = context.applicationContext

    val sessionManager: SessionManager by lazy { SessionManager(appContext) }
    val apiService: ApiService by lazy { RetrofitClient.getOrCreate(sessionManager) }
    val repository: BrainBoxRepository by lazy { BrainBoxRepository(apiService, sessionManager) }
    val localInfrastructure: BrainBoxLocalInfrastructure by lazy {
        BrainBoxLocalInfrastructureFactory.create(appContext)
    }
    val audioStore: BrainBoxAudioStore by lazy { BrainBoxAudioStore(appContext) }
    val syncCoordinator: DefaultBrainBoxSyncCoordinator by lazy {
        DefaultBrainBoxSyncCoordinator(
            repository = repository,
            database = localInfrastructure.database,
            offlineRepository = localInfrastructure.offlineRepository,
            preferencesStore = localInfrastructure.preferencesStore
        )
    }

    fun installIntoRuntime() {
        BrainBoxSyncCoordinatorRegistry.install(syncCoordinator)
    }

    companion object {
        @Volatile
        private var instance: BrainBoxAppGraph? = null

        fun from(context: Context): BrainBoxAppGraph {
            return instance ?: synchronized(this) {
                instance ?: BrainBoxAppGraph(context).also { graph ->
                    graph.installIntoRuntime()
                    instance = graph
                }
            }
        }
    }
}
