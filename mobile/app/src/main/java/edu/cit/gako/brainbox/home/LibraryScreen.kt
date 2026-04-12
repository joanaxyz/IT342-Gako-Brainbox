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
import edu.cit.gako.brainbox.data.local.model.BrainBoxNotebookDocument
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState
import edu.cit.gako.brainbox.data.offline.BrainBoxLocalInfrastructureFactory
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.network.models.NotebookDetail
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotebookCard(notebook, "Open editor") {
                            onOpenNotebook(notebook.uuid)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val existingPack = offlinePackMap[notebook.uuid]
                                        if (existingPack != null) {
                                            localInfrastructure.offlineRepository.removeOfflinePack(notebook.uuid)
                                            offlineMessage = "\"${notebook.title}\" was removed from offline storage."
                                        } else {
                                            runCatching {
                                                repository.getOfflineBundle(listOf(notebook.uuid))
                                            }.onSuccess { bundle ->
                                                val item = bundle.notebooks.firstOrNull()
                                                if (item != null) {
                                                    localInfrastructure.offlineRepository.saveNotebookSnapshot(
                                                        document = item.notebook.toNotebookDocument(),
                                                        pinned = true
                                                    )
                                                    offlineMessage = "\"${notebook.title}\" is now available offline."
                                                }
                                            }.onFailure {
                                                offlineMessage = "We couldn't download that notebook for offline use right now."
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (offlinePackMap.containsKey(notebook.uuid)) {
                                        "Remove offline"
                                    } else {
                                        "Make available offline"
                                    }
                                )
                            }
                            Text(
                                text = offlinePackMap[notebook.uuid]?.state?.name?.replace('_', ' ') ?: "Online only",
                                color = if (offlinePackMap.containsKey(notebook.uuid)) Accent else Ink3
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun NotebookSummary.toNotebookDocument(): BrainBoxNotebookDocument {
    return BrainBoxNotebookDocument(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = "",
        wordCount = wordCount,
        version = version ?: 0L,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = true,
        syncState = NotebookSyncState.CLEAN,
        localUpdatedAt = System.currentTimeMillis(),
        remoteUpdatedAt = System.currentTimeMillis()
    )
}

private fun NotebookDetail.toNotebookDocument(): BrainBoxNotebookDocument {
    return BrainBoxNotebookDocument(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = content,
        wordCount = wordCount,
        version = version ?: 0L,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = true,
        syncState = NotebookSyncState.CLEAN,
        localUpdatedAt = System.currentTimeMillis(),
        remoteUpdatedAt = System.currentTimeMillis()
    )
}


