package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.data.BrainBoxRepository
import edu.cit.gako.brainbox.data.offline.BrainBoxLocalInfrastructureFactory
import edu.cit.gako.brainbox.data.offline.downloadOfflinePack
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.network.models.QuizSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.StudyCard
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Ink3
import kotlinx.coroutines.launch

@Composable
internal fun QuizzesScreen(
    quizzes: List<QuizSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenQuiz: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context.applicationContext) {
        BrainBoxRepository(RetrofitClient.apiService, SessionManager(context.applicationContext))
    }
    val localInfrastructure = remember(context.applicationContext) {
        BrainBoxLocalInfrastructureFactory.create(context.applicationContext)
    }
    val offlinePacks by localInfrastructure.offlineRepository.observeActiveOfflinePacks().collectAsState(initial = emptyList())
    val offlinePackMap = remember(offlinePacks) { offlinePacks.associateBy { it.notebookUuid } }
    var offlineMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var activeOfflineNotebookUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = quizzes.filter {
        query.isBlank() ||
            it.title.contains(other = query, ignoreCase = true) ||
            (it.notebookTitle ?: "").contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Quizzes",
        description = "Quiz cards mirror the web treatment, now stacked for thumb-friendly study.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        offlineMessage?.let {
            SyncNoticeBanner(it, "Offline quiz packs stay linked to their parent notebook.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        BrandedSearchField(query, { query = it }, "Search quizzes")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("No quizzes yet", "Quiz sets created on the web will appear here once the API sync completes.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { quiz ->
                    val packNotebookUuid = quiz.notebookUuid
                    val isOffline = !packNotebookUuid.isNullOrBlank() && offlinePackMap.containsKey(packNotebookUuid)
                    val isWorking = activeOfflineNotebookUuid == packNotebookUuid && !packNotebookUuid.isNullOrBlank()
                    StudyCard(
                        title = quiz.title,
                        description = quiz.description,
                        kicker = quiz.difficulty ?: "Quiz",
                        meta = listOf(
                            "${quiz.questionCount} questions",
                            quiz.estimatedTime ?: "Quick run",
                            quiz.notebookTitle ?: "BrainBox"
                        ),
                        progress = quiz.bestScore,
                        action = "Start quiz",
                        footer = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val targetNotebookUuid = packNotebookUuid
                                        if (targetNotebookUuid.isNullOrBlank()) {
                                            offlineMessage = "We couldn't save that quiz offline because it isn't linked to a notebook yet."
                                            return@OutlinedButton
                                        }
                                        scope.launch {
                                            activeOfflineNotebookUuid = targetNotebookUuid
                                            if (isOffline) {
                                                localInfrastructure.offlineRepository.removeOfflinePack(targetNotebookUuid)
                                                offlineMessage = "\"${quiz.title}\" and its notebook pack were removed from offline storage."
                                            } else {
                                                downloadOfflinePack(
                                                    notebookUuid = targetNotebookUuid,
                                                    repository = repository,
                                                    offlineRepository = localInfrastructure.offlineRepository
                                                ).onSuccess {
                                                    offlineMessage = "\"${quiz.title}\" is now available offline with its notebook pack."
                                                }.onFailure {
                                                    offlineMessage = "We couldn't download that quiz for offline use right now."
                                                }
                                            }
                                            activeOfflineNotebookUuid = null
                                        }
                                    },
                                    enabled = !isWorking,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        when {
                                            isWorking && isOffline -> "Removing..."
                                            isWorking -> "Downloading..."
                                            isOffline -> "Remove offline"
                                            else -> "Make available offline"
                                        }
                                    )
                                }
                                Text(
                                    text = when {
                                        packNotebookUuid.isNullOrBlank() -> "Notebook required"
                                        isOffline -> "Available offline"
                                        else -> "Online only"
                                    },
                                    color = if (isOffline) Accent else Ink3
                                )
                            }
                        }
                    ) {
                        onOpenQuiz(quiz.uuid)
                    }
                }
            }
        }
    }
}


