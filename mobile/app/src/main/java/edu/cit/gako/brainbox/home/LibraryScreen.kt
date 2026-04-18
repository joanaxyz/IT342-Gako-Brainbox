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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
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
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.NotebookCard
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Ink3
import kotlinx.coroutines.launch

@Composable
internal fun LibraryScreen(
    notebooks: List<NotebookSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenNotebook: (String) -> Unit
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
    val filtered = notebooks.filter {
        query.isBlank() ||
            it.title.contains(other = query, ignoreCase = true) ||
            (it.categoryName ?: "").contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Library",
        description = "Browse notebooks with the same calm surfaces and warm hierarchy as the web.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        offlineMessage?.let {
            SyncNoticeBanner(it, "Offline packs are stored locally on this phone.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        BrandedSearchField(query, { query = it }, "Search notebooks")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("Nothing in the library yet", "When notebooks arrive from the web, you'll see them listed here.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { notebook ->
                    val isOffline = offlinePackMap.containsKey(notebook.uuid)
                    val isWorking = activeOfflineNotebookUuid == notebook.uuid
                    NotebookCard(
                        notebook = notebook,
                        action = "Open editor",
                        footer = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            activeOfflineNotebookUuid = notebook.uuid
                                            if (isOffline) {
                                                localInfrastructure.offlineRepository.removeOfflinePack(notebook.uuid)
                                                offlineMessage = "\"${notebook.title}\" was removed from offline storage."
                                            } else {
                                                downloadOfflinePack(
                                                    notebookUuid = notebook.uuid,
                                                    repository = repository,
                                                    offlineRepository = localInfrastructure.offlineRepository
                                                ).onSuccess { result ->
                                                    val details = buildList {
                                                        if (result.quizCount > 0) add("${result.quizCount} quizzes")
                                                        if (result.flashcardCount > 0) add("${result.flashcardCount} flashcards")
                                                    }
                                                    offlineMessage = if (details.isEmpty()) {
                                                        "\"${result.notebookTitle}\" is now available offline."
                                                    } else {
                                                        "\"${result.notebookTitle}\" is now available offline with ${details.joinToString(" and ")}."
                                                    }
                                                }.onFailure {
                                                    offlineMessage = "We couldn't download that notebook for offline use right now."
                                                }
                                            }
                                            activeOfflineNotebookUuid = null
                                        }
                                    },
                                    enabled = !isWorking
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
                                    text = if (isOffline) "Available offline" else "Online only",
                                    color = if (isOffline) Accent else Ink3
                                )
                            }
                        }
                    ) {
                            onOpenNotebook(notebook.uuid)
                        }
                }
            }
        }
    }
}


