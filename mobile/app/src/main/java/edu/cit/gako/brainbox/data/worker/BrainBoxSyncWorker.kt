package edu.cit.gako.brainbox.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrainBoxSyncWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val result = BrainBoxAppGraph.from(applicationContext).syncCoordinator.syncPendingMutations()
            if (result.shouldRetry) {
                Result.retry()
            } else {
                Result.success()
            }
        }.getOrElse {
            Result.retry()
        }
    }
}
